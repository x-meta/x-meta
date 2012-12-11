package org.xmeta;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 装载一个事物时有可能会遍历所有的事物管理器，把Category缓存起来可以减少遍历的次数。
 * 
 * @author zhangyuxiang
 *
 */
public class CategoryCache {
	List<WeakReference<Category>> categorys = new ArrayList<WeakReference<Category>>();
	
	/**
	 * 添加一个目录。
	 * 
	 * @param category
	 */
	public void addCategory(Category category){
		WeakReference<Category> rf = new WeakReference<Category>(category);
		categorys.add(rf);		
	}
	
	/**
	 * 获取事物。
	 * 
	 * @param thingName
	 * @return
	 */
	public synchronized Thing getThing(String thingName){
		for(int i=0; i<categorys.size(); i++){
			WeakReference<Category> rf = categorys.get(i);
			Category category = rf.get();
			if(category == null){
				categorys.remove(i);
				i--;
			}
			
			Thing thing = category.getThing(thingName);
			if(thing != null){
				return thing;
			}
		}
		
		return null;
	}
}
