package org.xmeta.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xmeta.Category;
import org.xmeta.Index;
import org.xmeta.Thing;
import org.xmeta.ThingManager;
import org.xmeta.World;
import org.xmeta.XMetaException;

public class RefactorUtil {
	private static RefactorUtil instance = new RefactorUtil();
	
	private RefactorUtil(){		
	}
	
	/**
	 * 在X-Meta的Groovy脚本中，如果使用线程直接访问RefactorUtil的静态方法，会出现类装载器
	 * 不一致而导致类型转换错误，故使用此方法在启动线程前获得类。
	 * 
	 * @return
	 */
	public static RefactorUtil getInstance(){
		return instance;
	}
	
	/**
	 * 重构，重构目录或事物，必须是目录重构为目录，事物重构为事物，不支持子事物的重构。<p/>
	 * 
	 * 重构会检查所有事物以便确保更新所有引用，因此会比较耗时。<p/>
	 * 
	 * 如果重构出错需手工处理，现在还不支持回滚操作。<p/>
	 * 
	 * 重构的流程是：<br>
     *     1. 计算要移动和更新的事物总数，事物总数也是操作数量的总数。<br/>
     *     2. 进行拷贝操作，把原事物拷贝到目标事物。<br/>
     *     3. 更新所有的事物对原目标的引用到目标事物，操作可能是更新或未更新。<br/>
     *     4. 删除原事物。<br/>
     *     
	 * 
	 * @param sourcePath 原路径，目录或事物
	 * @param targetPath 目标路径，目录或事物
	 * @param listener 
	 */
	public void refactor(String sourcePath, String targetPath, RefactorListener listener){
		try{
			World world = World.getInstance();
			String sourcePaths[] = sourcePath.split("[:]");
			String targetPaths[] = targetPath.split("[:]");
			ThingManager sourceThingManager = world.getThingManager(sourcePaths[0]);
			ThingManager targetThingManager = world.getThingManager(targetPaths[0]);
			if(sourceThingManager == null){
				throw new XMetaException("Source ThingManager is null, nmae=" + sourcePaths[0]);
			}
			if(targetThingManager == null){
				throw new XMetaException("Target ThingManager is nul, name=" + targetPaths[0]);
			}
			
			Object source = sourceThingManager.getThing(sourcePaths[1]);
			if(source == null){
				source = sourceThingManager.getCategory(sourcePaths[1]);
			}
			if(source == null){
				throw new XMetaException("Source is not exists, path=" + sourcePath);
			}
			
			int count = 0;

			if(source instanceof Thing){
				Thing thing = (Thing) source;
				Object target = targetThingManager.getThing(targetPaths[1]);
				if(target != null){
					throw new XMetaException("Target '" + targetPath + "' is already exists.");
				}
				
				count = 2;
				//计算world中所有事物的总量
				Index index = Index.getInstance();
				count += getThingCount(index, new HashMap<String, String>());
				
				if(listener != null){
					listener.onStart(count);
				}
				
				//拷贝
				String targetCategory = targetPaths[1];
				if(targetCategory.lastIndexOf(".") != -1){
					targetCategory = targetCategory.substring(0, targetCategory.lastIndexOf("."));
				}
				thing.copyTo(targetPaths[0], targetCategory);
				if(listener != null){
					listener.onCopy(sourcePaths[0], targetPaths[1]);
				}
				
				//更新引用
				replaceAll(sourcePaths[1], targetPaths[1], listener);
				//兼容就的名称规则
				
				
				//删除原有事物
				thing.remove();
				if(listener != null){
					listener.onDelete(sourcePath);
				}
			}else {
				//目录
				Category srcCategory = (Category) source;
				//Category tagCategory = null;
				Object target = targetThingManager.getCategory(targetPaths[1]);
				if(target != null){
					throw new XMetaException("Target category '" + targetPath + "' already exists.");
				}
				//world.createCategory(targetPath);
				
				//计算要移动的所有事物数量
				for(Iterator<Thing> iter = srcCategory.iterator(true); iter.hasNext();){
					count ++;
					iter.next();
				}
				//count = count; //拷贝和删除两次操作
				
				//计算world中所有事物的总量
				Index index = Index.getInstance();
				count += getThingCount(index, new HashMap<String, String>());
				
				if(listener != null){
					listener.onStart(count);
				}
				
				//拷贝
				for(Iterator<Thing> iter = srcCategory.iterator(true); iter.hasNext();){
					Thing tarThing = iter.next();
					//子目录
					String srcThingCategory = tarThing.getMetadata().getCategory().getName();
					String tarThingCategory = srcThingCategory.substring(sourcePaths[1].length(), srcThingCategory.length());
					if(tarThingCategory.startsWith(".")){
						tarThingCategory = tarThingCategory.substring(1, tarThingCategory.length());
					}
					if(!"".equals(tarThingCategory)){
						if(!"".equals(targetPaths[1])){
							tarThingCategory = targetPaths[1] + "." + tarThingCategory;
						}
					}else{
						tarThingCategory = targetPaths[1];
					}
					
			    	tarThing.copyTo(targetPaths[0], tarThingCategory);
					if(listener != null){
						listener.onCopy(tarThing.getMetadata().getPath(), tarThingCategory + "." + tarThing.getMetadata().getName());
					}
				}
				
				//更新引用
				replaceAll(sourcePaths[1], targetPaths[1], listener);
				
				//删除原有事物
				for(Iterator<Thing> iter = srcCategory.iterator(true); iter.hasNext();){
					Thing thing = iter.next();
					thing.remove();
					
					if(listener != null){
						listener.onDelete(thing.getMetadata().getPath());
					}
				}
				srcCategory.getThingManager().removeCategory(srcCategory.getName());
			}
		}finally{
			if(listener != null){
				listener.finish();
			}
		}
	}
	
	/**
	 * 返回所有事物的总数。
	 * 
	 * @return
	 */
	private static int getThingCount(Index index, Map<String, String> context){
		int count = 0;
		if(index.getType().equals(Index.TYPE_THING)){
			return 1;
		}else{
			if(index.getType().equals(Index.TYPE_THINGMANAGER)){
				if(context.get(index.getName()) != null){
					return 0;
				}else{
					context.put(index.getName(), index.getName());
				}
			}
			
			for(Index child : index.getChilds()){
				count += getThingCount(child, context);
			}
		}
		
		return count;
	}
	
	/**
	 * 用目标字符串替换所有事物的所有属性的字符串中包含源字符串。
	 * 
	 * @param replaceFor
	 * @param replaceWidth
	 */
	public static void replaceAll(String replaceFor, String replaceWidth, RefactorListener listener) {
		World world = World.getInstance();
		try{
			for(ThingManager thingManager : world.getThingManagers()){
				listener.notMidify(thingManager.getName());
				replaceThingManager(thingManager, replaceFor, replaceWidth, listener);
			}
		}finally{
			if(listener != null){
				listener.finish();
			}
		}
		//for (Project project : world.getAllProjects()) {
		//	replaceProject(project, replaceFor, replaceWidth, listener);
		//}
	}
	
	private static void replaceThingManager(ThingManager tm, String replaceFor,
			String replaceWidth, RefactorListener listener) {
		tm.refresh();
		for (Iterator<Thing> iter = tm.iterator("", true); iter.hasNext();) {
			Thing thing = iter.next();

			if(thing.getMetadata().getPath().startsWith(replaceFor)){
				//continue;
			}
			
			if(replaceThing(thing, replaceFor, replaceWidth, listener)){
				thing.getMetadata().setLastModified(System.currentTimeMillis());
				thing.save();
				
				if(listener != null){
					listener.onUpdated(thing.getMetadata().getPath());
				}
			}else{
				if(listener != null){
					listener.notMidify(thing.getMetadata().getPath());
				}
			}
		}
	}
	
	private static boolean replaceThing(Thing thing, String replaceFor,
			String replaceWith, RefactorListener listener) {
		Map<String, Object> attrs = thing.getAttributes();
		boolean updated = false;

		for (String key : attrs.keySet()) {
			Object value = attrs.get(key);
			if (value != null && value instanceof String) {
				String str = (String) value;
				boolean have = str.indexOf(replaceFor) != -1;
				updated = updated | have;
				if(have){
					thing.getAttributes().put(key, str.replaceAll("(" + replaceFor + ")", replaceWith));
				}
			}
		}

		for (Thing child : thing.getChilds()) {
			updated = updated | replaceThing(child, replaceFor, replaceWith, listener);
		}
		
		return updated;
	}
	
	/**
	 * 把旧的路径命名规则改为新的路径命名规则。
	 * 
	 */
	public static void changeOldPathToNewRule( RefactorListener listener){
		World world = World.getInstance();
		
		//首先获取所有事物的路径
		List<Index> thingIndexs = new ArrayList<Index>();
		getAllThingIndexs(thingIndexs, Index.getInstance());
		
		for(Index index : thingIndexs){
			String path = index.getPath();
			Thing thing = world.getThing(path);
			if(changeOldNameToNew(thing)){
				thing.save();
			}
			
		}
	}
	
	private static boolean changeOldNameToNew(Thing thing){
		Map<String, Object> attrs = thing.getAttributes();
		boolean updated = false;

		for (String key : attrs.keySet()) {
			Object value = attrs.get(key);
			if (value != null && value instanceof String) {
				String newValue = null;
				String str = (String) value;
				for(String line : str.split("[\n]")){
					String sp[] = line.split("[:]");
					String newLine = line;
					if(sp.length >= 3){
						if(sp[0].equals("jdbc") || sp[0].equals("http") || sp[1].equals("mm") || sp[1].indexOf(" ") != -1 || sp[1].equals("//") || "".equals(sp[2])){
							
						}else{
							for(int i=0; i<sp.length; i++){
								if(i ==0){
									newLine = sp[i];
								}else if(i < 3){
									newLine = newLine + "." + sp[i];
								}else{
									newLine = newLine + sp[i];
								}
							}
							System.out.println(line + " => " + newLine);
							updated = true;
						}
					}
					
					if(newValue == null){
						newValue = newLine;
					}else{
						newValue = newValue + "\n" + newLine;
					}
					
				}
				
				if(updated){
					attrs.put(key, newValue);
				}
			}
		}

		for (Thing child : thing.getChilds()) {
			updated = updated | changeOldNameToNew(child);
		}
		
		return updated;
	}
	
	public static void getAllThingIndexs(List<Index> thingIndexs, Index index){
		if(index.getType().equals(Index.TYPE_THING)){
			thingIndexs.add(index);
		}else{
			for(Index child : index.getChilds()){
				getAllThingIndexs(thingIndexs, child);
			}
		}
	}
}
