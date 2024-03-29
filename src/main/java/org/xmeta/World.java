/*******************************************************************************
* Copyright 2007-2013 See AUTHORS file.
 * 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
******************************************************************************/
package org.xmeta;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xmeta.annotation.ThingAnnotationUtils;
import org.xmeta.cache.ThingCache;
import org.xmeta.cache.ThingEntry;
import org.xmeta.codes.DmlThingCoder;
import org.xmeta.codes.DmwThingCoder;
import org.xmeta.codes.JsonThingCoder;
import org.xmeta.codes.PropertyThingCoder;
import org.xmeta.codes.TxtThingCoder;
import org.xmeta.codes.XerThingCoder;
import org.xmeta.codes.XmlThingCoder;
import org.xmeta.thingManagers.ClassThingManager;
import org.xmeta.thingManagers.FileMonitor;
import org.xmeta.thingManagers.FileThingManager;
import org.xmeta.thingManagers.JarThingManager;
import org.xmeta.thingManagers.TransientThingManager;
import org.xmeta.util.JarThingManagerIniter;
import org.xmeta.util.ThingClassLoader;
import org.xmeta.util.UtilFile;
import org.xmeta.util.UtilJava;

/**
 * <p>世界是存放事物的容器。</p>
 * 
 * <p>可以通过事物的路径访问这个世界下的所有事物。</p>
 *
 * 事物的路径规则参看{@link org.xmeta.Path}}
 * 
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 * 
 */
public class World {
	/** 日志 */
	//private static Logger log = LoggerFactory.getLogger(World.class);
	private static final Logger log = Logger.getLogger(World.class.getName());
	/** 编程模式，默认 */
	public static byte MODE_PROGRAMING = 0;
	/** 工作模式，一般JavaAction等发生变更时不重新编译 */
	public static byte MODE_WORKING = 1;
	/** 在WEB模式下，会把webroot作为一个事物管理器 */
	public static String WEBROOT_TEMP_THINGMANAGER = "xworker_webroot_tmpthingmanager";
	
	/** 世界的唯一单态实例 */
	private final static World worldInstance = new World();
	//private ThreadLocal<Stack<String>> loadingStack = new ThreadLocal<Stack<String>>();
	//private ThreadLocal<List<DelayInitTask>> delayInitTasks = new ThreadLocal<List<DelayInitTask>>();

	/** 瞬态事物的管理者 */
	private final TransientThingManager transientThingManager = new TransientThingManager();
	private final ClassThingManager classThingManager = new ClassThingManager();
		
	/** 元事物 */
	public Thing metaThing = null;//new MetaThing();

	/** 全局上下文 */
	protected List<ThingEntry> globalContexts = new CopyOnWriteArrayList<>();

	/** 世界的路径，项目、目录和存放事物的根目录 */
	private String worldPath = ".";

	/** 用户数据 */
	private final Map<String, Object> userDatas = new HashMap<>();
	
	/** 绑定到线程上的数据 */
	protected volatile ThreadLocal<Map<String, Object>> threadLocalDatas = null;//

	/** 公共事物管理这的监听者注册列表 */
	private final Map<String, List<ThingManagerListener>> thingManagerListeners = new ConcurrentHashMap<>();

	/** 世界级的类装载器 */
	private ThingClassLoader worldClassLoader = null;
	
	/** 动作监听器  */
	private ActionListener actionListener = null;
	
	/** 帮助列表，运行时对用户解决问题可能有帮助的内容，可以在任何一个事物的帮助菜单中查看 */
	private final List<Help> helps = new ArrayList<>();
	/** 帮助列表的最大个数 */
	public int helpSize = 100;
	/** 是否打印装载信息 */
	private boolean verbose = false;
	/** 是否已初始化 */
	private boolean inited = false;
	/** 执行模式, MODE_PROGRAMING或MODE_WORKING，当时WORKING时如果Java等动作发生变更时一般不重新编译 */
	private byte mode = MODE_PROGRAMING;
	/** 打印verbose的缓存，相同的不需要再次打印 */
	private Map<String, String> verboseThingCache = null;
	/** 事物管理器列表 */
	private final List<ThingManager> thingManagers = new CopyOnWriteArrayList<>();
	/** 事物编码器 */
	private final List<ThingCoder> thingCoders = new ArrayList<>();
	/* 目录缓存 */
	//private Map<String, CategoryCache> categoryCaches = new HashMap<String, CategoryCache>();
	/** 初始化发送生错的事物管理器会放在这里，避免重复初始化  */
	private final List<String> failureThingManangers = new ArrayList<>();
	private final List<String> libList = new ArrayList<>();

	/**
	 * 运行时所有的事物基本都通过World获取，为提交性能增加路径缓存。
	 * 
	 * 路径缓存1和路径缓存2，未避免系统长期使用而导致路径缓存无限增长，每隔一段时间情况pathCache2，然后pathCach1和pahCach2互换
	 * 
	 * 经测试，使用缓存保存路径效果和不使用Cache没有数量级上的优势，故取消。
	 * */
	//private Map<String, Path> pathCache1 = new ConcurrentHashMap<String, Path>(5000);
	//private Map<String, Path> pathCache2 = new ConcurrentHashMap<String, Path>(5000);

	private String OS;
	private String PROCESSOR_ARCHITECTURE;
	/**  web文件根路径 */
	private String webFileRoot = null;
	
	/**
	 * 私有构造方法，目前系统中只允许存在一个世界。
	 * 
	 */
	private World() {
		//long start = System.currentTimeMillis();
		//默认事物编码
		//thingCoders.clear();		
		ThingCoder txtThingCoder = new TxtThingCoder();
		ThingCoder xmlThingCoder = new XmlThingCoder();
		ThingCoder propertyCoder = new PropertyThingCoder();
		ThingCoder dmlThingCoder = new DmlThingCoder(xmlThingCoder, txtThingCoder, propertyCoder);
		ThingCoder dmwThingCoder = new DmwThingCoder(xmlThingCoder, txtThingCoder, propertyCoder);
		thingCoders.add(dmlThingCoder);
		thingCoders.add(dmwThingCoder);
		thingCoders.add(txtThingCoder);
		thingCoders.add(xmlThingCoder);
		thingCoders.add(new XerThingCoder());
		try{
			
			thingCoders.add(new JsonThingCoder());
		}catch(Throwable e){
			log.log(Level.FINE, "JsonThingCoder init error, if need json, need Jackson, " + e.getMessage());
		}
		//System.out.println("newworld init coders: " + (System.currentTimeMillis() - start));
		
		new Thread(new Runnable(){
			public void run(){
				try{
					UtilJava.invokeMethod("org.xmeta.util.ThingOgnlAccessor", "init", new Class<?>[0], new Object[0]);
					///ThingOgnlAccessor.init();
				}catch(Throwable ignored){
				}
			}
		}).start();

		String worldClassPath = System.getenv("xworker.class.path");
		if(worldClassPath != null){
			for(String path :worldClassPath.split("[" + File.pathSeparator + "]")){
				this.addLib(path);
			}
		}
		//System.out.println("newworld init ognl: " + (System.currentTimeMillis() - start));
	}
	
	/**
	 * 取得世界的实例。
	 * 
	 * @return 世界的实例
	 */
	public static World getInstance() {
		return worldInstance;
	}
    /*
	protected void pushLoading(String path) {
		Stack<String> stack = loadingStack.get();
		if(stack == null) {
			stack = new Stack<String>();
			loadingStack.set(stack);
		}
		
		stack.push(path);
	}
	
	protected void popLoading() {
		Stack<String> stack = loadingStack.get();
		if(stack != null) {
			stack.pop();
			if(stack.size() == 0) {
				List<DelayInitTask> initTasks = delayInitTasks.get();
				if(initTasks != null) {
					for(DelayInitTask initTask : initTasks) {
						initTask.run();
					}
				}
				
				delayInitTasks.set(null);
			}
		}
	}
	
	protected void addDelayInitTask(DelayInitTask initTask) {
		List<DelayInitTask> initTasks = delayInitTasks.get();
		if(initTasks == null) {
			initTasks = new ArrayList<DelayInitTask>();
			delayInitTasks.set(initTasks);
		}
		
		if(!initTasks.contains(initTask)) {
			initTasks.add(initTask);
		}
	}
	
	protected boolean isLoading(String path) {
		if(path == null || "".equals(path)) {
			return false;
		}
		
		Stack<String> stack = loadingStack.get();
		if(stack != null) {
			int count = 0;
			for(String p : stack) {
				if(path.equals(p)) {
					count ++;
					if(count == 10) {
						//说明存在递归
						return true;
					}
				}
			}
		}
		
		return false;
	}*/
	
	public void setMode(byte mode){
		this.mode = mode;
	}
	
	public byte getMode(){
		return this.mode;
	}
	
	public  List<String> getLibList(){
		return libList;
	}
	
	public void addLib(String lib) {
		libList.add(lib);
	}
	/**
	 * 创建一个ThingManager，必须是全路径&lt;projectName&gt;:&lt;thingManagerName&gt;。
	 * 
	 * @param name 名称
	 * @param link 连接
	 * @return 事物管理器
	 */
	public ThingManager createThingManager(String name, String link){
		File managerRootFile = new File(this.getPath() + "/projects/" + name);
		
		return createThingManager(name, managerRootFile, link);
	}
	
	public ThingManager createThingManager(String name, File managerRootFile, String link){
		if(managerRootFile.exists()){
			throw new XMetaException("ThingManager already exists, name=" + name);
		}
		managerRootFile.mkdirs();
		FileOutputStream fout = null;
		try{
			fout = new FileOutputStream(new File(managerRootFile, "config.properties"));
			fout.write(("link=" + link).getBytes());
		}catch(Exception e){
			throw new XMetaException("create config.properties error, thingManager=" + name, e);
		}finally{
			if(fout != null){
				try {
					fout.close();
				} catch (IOException e) {
					e.printStackTrace();
				}					
			}
		}
	
		return initThingManager(managerRootFile);
	}
		
	/**
	 * 获取加载和初始化失败的事物管理器名称的列表。
	 * @return 事物管理器列表
	 */
	public List<String> getFailureThingManangers() {
		return failureThingManangers;
	}
	
	/**
	 * 返回指定的事物管理器是否是加载或初始化失败。
	 * 
	 * @param thingManager 事物管理器
	 * @return 是否失败
	 */
	public boolean isFailureThingManager(String thingManager){
		return failureThingManangers.contains(thingManager);
	}
	
	/**
	 * 从加载或初始化失败的事物管理器列表中移除指定的事物管理器。
	 * 
	 * @param thingManager 事物管理器名
	 */
	public void removeFailureThingManager(String thingManager){
		failureThingManangers.remove(thingManager);
	}

	/**
	 * 添加全局上下文。动作上下文已改名为动作监听器，使用addGlobalActionListener。
	 * 
	 * @param contextThing 事物
	 * @param index 位置
	 * 
	 * @deprecated
	 */
	public void addGlobalContext(Thing contextThing, int index) {
		addGlobalActionListener(contextThing, index);
	}

	/**
	 * 添加全局动作监听器。全局动作监听器可以监听除了全局监听器的动作之外的所有动作的执行。
	 * 
	 * @param listener 监听器
	 */
	public void addGlobalActionListener(Thing listener) {
		addGlobalActionListener(listener, -1);
	}
			
	/**
	 * 添加全局动作监听器。全局动作监听器可以监听除了全局监听器的动作之外的所有动作的执行。
	 * 
	 * @param listener 监听器
	 * @param index 位置
	 */
	public void addGlobalActionListener(Thing listener, int index) {
		boolean have = false;
		for (ThingEntry entry : globalContexts) {
			Thing thing = entry.getThing();
			if (thing != null
					&& thing.getMetadata().getPath().equals(
							listener.getMetadata().getPath())) {
				have = true;
				break;
			}
		}

		if (!have) {
			ThingEntry entry = new ThingEntry(listener);
			if (index == -1) {
				globalContexts.add(entry);
			} else {
				globalContexts.add(index, entry);
			}
		}
	}
	
	/**
	 * 移除一个全局上下文。
	 * 
	 * @param contextThing 事物
	 * 
	 * @deprecated
	 */
	public void removeGlobalContext(Thing contextThing) {
		removeGlobalActionListener(contextThing);
	}

	/**
	 * 移除一个全局动作监听器。
	 * 
	 * @param listener
	 */
	public void removeGlobalActionListener(Thing listener) {
		ThingEntry forRemoved = null;
		for (ThingEntry entry : globalContexts) {
			Thing thing = entry.getThing();
			if (thing != null
					&& thing.getMetadata().getPath().equals(
							listener.getMetadata().getPath())) {
				forRemoved = entry;
				break;
			}
		}

		if (forRemoved != null) {
			globalContexts.remove(forRemoved);
		}
	}
	
	/**
	 * 通过路径来获得事物，可能会返回项目、事物管理者、目录、事物或者事物的列表。
	 * 
	 * @param pathStr  路径
	 * @return 路径所对应的事物
	 */
	public Object get(String pathStr) {
		if(pathStr == null || "".equals(pathStr)){
			return this;
		}
		
		//if("xworker.lang.actions.Action".equals(pathStr)) {
		//	System.out.println();
		//}
//		if("Test".equals(pathStr)) {
//			System.out.println();
//		}
		
		Path path = new Path(pathStr); 
		/*
				pathCache1.get(pathStr);
		if(path == null){
			path = pathCache2.get(pathStr);
			if(path == null){
				path = new Path(pathStr);
			}
			pathCache1.put(pathStr, path);
		}*/
		
		Thing thing = ThingCache.get(path.getPath());
		if(thing != null && thing.getMetadata().isRemoved()){ //如事物的文件被外部改动或事物已给标记为删除，需要重新读取
			thing = null;
		}
		if(thing == null){
			if("MetaThing".equals(path.getPath())){
				//MetaThing这个名字永远分配给元事物
				thing = metaThing;
			}else{
				//先从目录缓存中读取，然后再遍历每一个事物管理器
				/*
				String categoryPath = path.getPath();
				//String thingName;
				int index = categoryPath.lastIndexOf(".");
				if(index != -1){
					//thingName = categoryPath.substring(index + 1, categoryPath.length());
					categoryPath = categoryPath.substring(0, index);					
				}else{
					//thingName = categoryPath;
					categoryPath = null;
				}
				CategoryCache  categoryCache = categoryCaches.get(categoryPath);
				if(categoryCache != null){
					thing = categoryCache.getThing(path.getPath());
				}*/
				
				//按照事务管理器列表，获取第一个事物
				String thingPath = path.getPath();
				//try {
					//this.pushLoading(thingPath);
					for(int i=0; i<thingManagers.size(); i++){
						ThingManager thingManager = thingManagers.get(i);
						thing = thingManager.getThing(thingPath);
						if(thing != null){
							path.setType(Path.TYPE_THING);
							break;
						}
					}
				//}finally {
					//this.popLoading();
				//}
				
				if(thing != null){
					ThingCache.put(path.getPath(), thing);
					/*
					if(categoryCache == null){
						categoryCache = new CategoryCache();
						categoryCache.addCategory(thing.getMetadata().getCategory());
						categoryCaches.put(categoryPath, categoryCache);
					}
					*/
				}else{
					//查找是否是目录
					/*
					categoryCache = categoryCaches.get(path.getPath());
					if(categoryCache != null){
						return categoryCache.getCategory();
					}*/
					
					Category category = null;
					for(int i=0; i<thingManagers.size(); i++){
						ThingManager thingManager = thingManagers.get(i);
						category = thingManager.getCategory(path.getPath());
						if(category != null){
							path.setType(Path.TYPE_CATEGORY);
							/*
							categoryCache = categoryCaches.get(path.getPath());
							if(categoryCache == null){
								categoryCache = new CategoryCache();
								categoryCache.addCategory(category);
								categoryCaches.put(path.getPath(), categoryCache);
							}*/
							return category;
						}
					}					
				}
			}
		}
		
		if(thing == null){
			String thingPath = path.getPath();
			//try {
			//	this.pushLoading(thingPath);
				thing = loadThingFromClasspath(thingPath);
				if(thing != null) {
					ThingCache.put(thingPath, thing);
				}
			//}finally {
			//	this.popLoading();
			//}
		}
		if(thing != null){
			return thing.get(path);
		}else{
			return null;
		}
	}
	
	@SuppressWarnings("unused")
	private void printVerbose(String thingPath){
		if(verboseThingCache == null){
			verboseThingCache = new HashMap<String, String>();
		}
		if(verboseThingCache.get(thingPath) == null){
			verboseThingCache.put(thingPath, thingPath);
			System.out.println("[Loaded " + thingPath + " from X-Meta");
		}
	}
	
	/**
	 * 从类的路径中装载事物。
	 * 
	 * @param thingPath 事物路径
	 * @return 事物
	 */
	public Thing loadThingFromClasspath(String thingPath){		
		Thing thing = null;
		for(ThingCoder coder : this.thingCoders){
			//加前缀/在eclipse项目中找不到，等打包后试试
			//String resource = "/" + thingPath.replace('.', '/') + "." + coder.getType();
			String resource = thingPath.replace('.', '/') + "." + coder.getType();
			URL url = this.getClassLoader().getResource(resource);
			if(url == null){
				url = this.getClassLoader().getResource("/" + resource);
			}
			
			if(url != null){
				thing = new Thing(null, null, null, false);
				ThingMetadata metadata = thing.getMetadata();
				metadata.setPath(thingPath);
				String categoryPath = thingPath;
				if(thingPath.lastIndexOf(".") != -1){
					categoryPath = thingPath.substring(0, thingPath.lastIndexOf("."));
				}
				Category category = classThingManager.getCategory(categoryPath);
				metadata.setCategory(category);

				try {
					InputStream fin = url.openStream();
					try{
						thing.beginModify();

						long lastModified = System.currentTimeMillis();
						String fileName = url.getFile();
						if(fileName != null){
							File file = new File(fileName);
							if(file.exists()){
								lastModified = file.lastModified();
							}
						}
						coder.decode(thing, fin, lastModified);
					}catch(Exception e){
						System.out.println("load thing error, thingPath=" + thingPath);
						e.printStackTrace();
						return null;
					}finally{
						thing.endModify(false);

						if(fin != null){
							try {
								fin.close();
							} catch (IOException ignored) {
							}
						}
					}

					//如果是文件，加入到文件监控，文件变化了可以重新读取事物，可用于在Java项目手工编程时
					if(url.getProtocol().toLowerCase().equals("file")){
						FileMonitor.getInstance().addFile(thingPath, thing, new File(url.getFile()), false);
					}

					//放到缓存中
					ThingCache.put(metadata.getPath(), thing);

					return thing;
				} catch (IOException e) {
					//e.printStackTrace();
					log.log(Level.WARNING, "load thing from classpath error", e);
				}
			}
		}
		
		//按照类来加载
		try {
			Class<?> cls = this.worldClassLoader.loadClass(thingPath);
			if(cls != null) {
				thing = ThingAnnotationUtils.parse(cls);
				if(thing != null) {
					String pkg = cls.getName();
					int index = pkg.lastIndexOf(".");
					if(index > 0) {
						pkg = pkg.substring(0, index);
					}
					thing.getMetadata().setCategory(classThingManager.getCategory(pkg));
					thing.getMetadata().setPath(thingPath);
					
					//放到缓存中
					ThingCache.put(thing.metadata.getPath(), thing);
					
					return thing;
				}
			}
		}catch(Throwable e) {			
		}
		return null;
	}

	/**
	 * 通过路径获取资源。
	 * 
	 * @param path 路径
	 * @return 资源，没找到返回null
	 * 
	 * @throws IOException 异常
	 */
	public URL getResource(String path) throws IOException{
		if(path == null){
			return null;
		}
		
		path = path.replace('\\', '/');
		if(path.startsWith("world|")){
			//相对于World的路径
			path = World.getInstance().getPath() + "/" + path.substring(6);
			
			File file = new File(path);
			if(file.exists()){
				return file.toURI().toURL();
			}else{
				return this.getClassLoader().getResource("/" + path.substring(6));
			}
		}else if(path.startsWith("project|") || path.contains(":")){
			//基于项目的路径，旧的路径规则，已不再使用
			int index = path.indexOf(":");
			String projectName = null;
			if(path.startsWith("project|")){
				projectName = path.substring(8, index);
			}else{
				projectName = path.substring(0, index);
			}
			path = path.substring(index + 1);
			path = "/" + projectName + "/" + path;
			for(ThingManager thingManager : getThingManagers()){
				URL in = thingManager.findResource(path);
				if(in != null){
					return in;
				}
			}
			
			return getClassLoader().getResource(path);
		}else{
			//直接从文件系统中找
			File file = new File(path);
			if(file.exists()){
				return file.toURI().toURL();
			}else{
				//从webRoot中找
				file = new File(getPath() + "/webroot/" + path);
				if(file.exists()){
					return file.toURI().toURL();
				}
				
				//先从ThingManager中找
				if(!path.startsWith("/")){
					path = "/" + path;
					
				}
				for(ThingManager thingManager : getThingManagers()){
					URL in = thingManager.findResource(path);
					if(in != null){
						return in;
					}
				}
				
				URL in = getClassLoader().getResource(path);
				if(in == null){
					if(path.startsWith("/") || path.startsWith("\\")){
						in = getClassLoader().getResource(path.substring(1));
					}else{
						in = getClassLoader().getResource("/" + path);
					}
				}
				
				
				return in;
			}
		}		
	}
	
	/**
	 * 以流的形式返回资源，如果没有返回null。首先从文件系统中找，其次从classpath下寻找资源。
	 * 
	 * @param path 路径
	 * @return 输入流
	 * @throws IOException IO异常
	 */
	public InputStream getResourceAsStream(String path) throws IOException{
		if(path == null){
			return null;
		}
		
		path = path.replace('\\', '/');
		if(path.startsWith("world|")){
			//相对于World的路径
			path = World.getInstance().getPath() + "/" + path.substring(6);
			
			File file = new File(path);
			if(file.exists()){
				return new FileInputStream(file);
			}else{
				return this.getClassLoader().getResourceAsStream("/" + path.substring(6));
			}
		}else if(path.startsWith("project|") || path.contains(":")){
			//基于项目的路径，旧的路径规则，已不再使用
			int index = path.indexOf(":");
			String projectName = null;
			if(path.startsWith("project|")){
				projectName = path.substring(8, index);
			}else{
				projectName = path.substring(0, index);
			}
			path = path.substring(index + 1);
			path = "/" + projectName + "/" + path;
			for(ThingManager thingManager : getThingManagers()){
				InputStream in = thingManager.getResourceAsStream(path);
				if(in != null){
					return in;
				}
			}
			
			return getClassLoader().getResourceAsStream(path);
		}else{
			//直接从文件系统中找
			File file = new File(path);
			if(file.exists()){
				return new FileInputStream(file);
			}else{
				//从webRoot中找
				file = new File(getPath() + "/webroot/" + path);
				if(file.exists()){
					return new FileInputStream(file);
				}
				
				//先从ThingManager中找
				if(!path.startsWith("/")){
					path = "/" + path;
					
				}
				for(ThingManager thingManager : getThingManagers()){
					InputStream in = thingManager.getResourceAsStream(path);
					if(in != null){
						return in;
					}
				}
				
				InputStream in = getClassLoader().getResourceAsStream(path);
				if(in == null){
					if(path.startsWith("/") || path.startsWith("\\")){
						in = getClassLoader().getResourceAsStream(path.substring(1, path.length()));
					}else{
						in = getClassLoader().getResourceAsStream("/" + path);
					}
				}
				
				
				return in;
			}
		}		
	}
	
	/**
	 * 通过动作事物的路径获得动作。
	 * 
	 * @param actionPath
	 *            动作路径
	 * @return Action
	 */
	public Action getAction(String actionPath) {
		Thing actionThing = getThing(actionPath);
		if (actionThing != null) {
			return actionThing.getAction();
		} else {
			return null;
		}
	}
	
	/**
	 * 返回动作类，通常是动作事物对应的类，有些动作可能没有对应类。
	 * 
	 * @param actionPath 动作路径
	 * @param actionContext 变量上下文
	 * @return 类
	 */
	@SuppressWarnings({ "rawtypes" })
	public Class getActionClass(String actionPath, ActionContext actionContext){
		return getActionClass(getThing(actionPath), actionContext);
	}
	
	/**
	 * 返回动作类，通常是动作事物对应的类，有些动作可能没有对应类。
	 * 
	 * @param actionThing 动作事物
	 * @param actionContext 变量上下文
	 * @return 类
	 */
	@SuppressWarnings({"rawtypes" })
	public Class getActionClass(Thing actionThing, ActionContext actionContext){
		if(actionThing != null){
			return actionThing.getAction().getActionClass(actionContext);
		}else{
			return null;
		}
	}

	/**
	 * 通过事物获得一个动作。
	 * 
	 * @param actionThing
	 *            定义动作的事物
	 * @return 动作
	 */
	public Action getAction(Thing actionThing) {
		return actionThing.getAction();
	}

	/**
	 * 获取一个用户数据。
	 * 
	 * @param key 键
	 * @return 值
	 */
	@SuppressWarnings("unchecked")
	public <T> T getData(String key) {
		return (T) userDatas.get(key);
	}
	
	/**
	 * 设置绑定到当前事物的ThreadLocal的数据。
	 */
	public void setThreadData(String key, Object data) {
		if(key == null) {
			return;
		}
		
		if(threadLocalDatas == null) {
			synchronized(this){
				if(threadLocalDatas == null){
					threadLocalDatas = new ThreadLocal<Map<String, Object>>();
				}	
			}
		}
		
		Map<String, Object> dataMap = threadLocalDatas.get();
		if(dataMap == null) {
			dataMap = new HashMap<String, Object>();
			threadLocalDatas.set(dataMap);
		}
		
		dataMap.put(key, data);
	}
	
	/**
	 * 返回绑定到本事物的ThradLocal中的数据。
	 * 
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getThreadData(String key) {
		if(key == null) {
			return null;
		}
		
		if(threadLocalDatas == null) {
			return null;
		}
		
		Map<String, Object> dataMap = threadLocalDatas.get();
		if(dataMap == null) {
			return null;
		}
		
		return (T) dataMap.get(key);
	}

	/**
	 * 获得当前世界的路径。
	 * 
	 * @return 世界的路径
	 */
	public String getPath() {
		return this.worldPath;
	}

	/**
	 * 返回所有的插件列表。
	 * 
	 * @return 插件列表
	 */
	/*
	public List<Project> getPlugIns() {
		List<Project> projs = new ArrayList<Project>();
		for (Project project : projects) {
			if (project.isPlugIn()) {
				projs.add(project);
			}
		}

		return projs;
	}
	*/

	/**
	 * 执行一个已经设置了断点的动作，可以把该方法的调用嵌入到代码中，从而可以
	 * 实现在代码中的调试断点。
	 * 
	 * @param actionContext 变量上下文
	 */
	public void debug(ActionContext actionContext){
		Thing debugAction = this.getThing("xworker.ide.debug.action.DebugAction");
		if(debugAction != null){
			debugAction.doAction("debug", actionContext);
		}
	}
	
	/**
	 * 通过事物的路径来访问具体的事物。
	 * 
	 * @param path
	 *            事物的路径
	 * @return 事物，不存在则返回null
	 */
	public Thing getThing(String path) {		
		Object obj = get(path);
		if (obj instanceof Thing) {
			// thingCaches.put(path, (Thing) obj);
			return (Thing) obj;
		} else {
			return null;
		}
	}
	
	/**
	 * 通过事物的路径获取指定的事物，如果存在那么使用给定的descriptor创建事物，并保存到指定的thingManager下。
	 * 
	 * @param path 事物的路径
	 * @param thingManager 事物管理器
	 * @param descriptor 描述者
	 * @return 事物
	 */
	public Thing getThing(String path, String thingManager, String descriptor){
		Thing thing = getThing(path);
		if(thing == null){
			thing = new Thing(descriptor);
			thing.initDefaultValue();
			thing.saveAs(thingManager, path);
		}
		
		return thing;
	}

	/**
	 * 获取事物管理器列表。
	 * 
	 * @return 事物管理器列表
	 */
	public List<ThingManager> getThingManagers(){
		return thingManagers;
	}
	
	/**
	 * 获得注册的事物管理者的监听列表。
	 * 
	 * @param thingManagerName
	 *            事物管理者的名称
	 * @return 事物管理者的监听者列表
	 */
	public List<ThingManagerListener> getThingManagerListeners(
			String thingManagerName) {
		List<ThingManagerListener> ls = new ArrayList<ThingManagerListener>();

		List<ThingManagerListener> listeners = thingManagerListeners
				.get(thingManagerName);
		if (listeners != null) {
			ls.addAll(listeners);
		}

		listeners = thingManagerListeners.get("*");
		if (listeners != null) {
			ls.addAll(listeners);
		}

		return ls;
	}

	public ThingClassLoader getClassLoader() {
		return worldClassLoader;
	}

	/*
	private void initLibraryPath(){
		File f = new File(worldPath);
		
		String libraryPath = System.getProperty("java.library.path");
		String[] paths = libraryPath == null ? new String[] {} : libraryPath.split("[" + File.pathSeparator + "]");

		//设置world目录下的os/library/<OS>/为类库位置
		String path = new File(f, "os/library/" + OS).getAbsolutePath();
		boolean changed = false;
		if(isLibraryExists(paths, path) == false) {
			libraryPath = libraryPath + File.pathSeparator + path;
			addLibraryDir(path);
			changed = true;
		}
		
		
		//设置 world目下下的library/<OS>/<PROCESSOR_ARCHITECTURE>/为类库位置
		path = new File(f, "os/library/" + OS + "_" + PROCESSOR_ARCHITECTURE).getAbsolutePath();
		if(isLibraryExists(paths, path) == false) {
			libraryPath = libraryPath + File.pathSeparator + path;
			addLibraryDir(path);
			changed = true;
		}
		
		if(changed) {
			System.setProperty("java.library.path", libraryPath);
			//log.info("java.library.path changed=" + libraryPath);
		}
	}*/
	
	/**
	 * 判断动态库的路径是否已经设置过了。
	 */
	private boolean isLibraryExists(String paths[], String path) {
		File pathFile = new File(path);
		if(!pathFile.exists()) {
			return true;
		}
		
		for(int i=0; i<paths.length; i++) {
			File f = new File(paths[i]);
			if(!f.exists()) {
				continue;
			}
			
			try {
				if(f.getCanonicalPath().equals(pathFile.getCanonicalPath())) {
					return true;
				}
			}catch(Exception e) {				
			}
			
			if(i >= 1){
				break;
			}
		}
		
		return false;
	}
	
	private void initOsProperites(){
		try{
			try{
				OS = System.getenv("OS").toLowerCase();
			}catch(Exception e){
				OS = System.getProperty("os.name").toLowerCase();
			}
			PROCESSOR_ARCHITECTURE = "bit" + System.getProperty("sun.arch.data.model"); //System.getenv("PROCESSOR_ARCHITECTURE").toLowerCase();
			InputStream osin = World.class.getResourceAsStream("/xworker_os.properties");
			if(osin != null){
				Properties p = new Properties();
				p.load(osin);
				osin.close();
				
				String value = p.getProperty(OS);
				if(value != null && !"".equals(value)){
					OS = value;
				}
				value = p.getProperty(PROCESSOR_ARCHITECTURE);
				if(value != null && !"".equals(value)){
					PROCESSOR_ARCHITECTURE = value;
				}
			}			
			log.log(Level.FINE, "OS=" + OS + ", sun.arch.data.model=" + PROCESSOR_ARCHITECTURE);
		}catch(Exception e){
			log.log(Level.WARNING, "init os info error", e);
		}
	}
	
	public String getHome(String name){
		name = name.toUpperCase();
		String home = getHomeFromSystem(name);
		if(home == null){
			home = getHomeFromSystem(name.toLowerCase());
		}

		return home;
	}
	
	public String getHomeFromSystem(String name){
		String home = System.getProperty(name);
		if(home == null){
			home = System.getenv(name);
		}
		
		return home;
	}
	
	public String testFileHome(String filePath){
		if(new File(filePath).exists()){
			return filePath;
		}else{
			return null;
		}
	}
	
	public String getHomeFormSytsem(){
		//尝试从系统变量中获取
		String home = getHome("XMETA_HOME");
		if(home == null){
			home =  getHome("XWORKER_HOME");
		}
		if(home == null){
			home = System.getenv("xmeta_home");
		}
		if(home == null){
			home = System.getenv("XMETA_HOME");
		}
		if(home == null){
			home = testFileHome("/usr/local/xworker/");
		}
		if(home == null){
			home = ".";
		}
		
		return home;
	}
	
	public void init(String worldPath){
		init(worldPath, World.class.getClassLoader());
	}
	
	/**
	 * 通过给定事物的存放路径来初始化世界。
	 * 
	 * @param worldPath
	 *            世界的路径
	 */
	public void init(String worldPath, ClassLoader classLoader) {
		//long start = System.currentTimeMillis();
		// 设置事物的路径
		if(worldPath == null){
			//尝试从系统变量中获取
			worldPath = getHomeFormSytsem();
			
		}
		System.setProperty("XMETA_HOME", worldPath);
		ThingCache.clear();
		
		//System.out.println("world clear cache: " + (System.currentTimeMillis() - start));
		File f = new File(worldPath);

		//check _local projects
		File _local = new File(f, "projects/_local");
		if(!_local.exists()){
			_local.mkdirs();
		}

		this.worldPath = f.getAbsolutePath();

		//os和架构等变量的初始化
		initOsProperites();
		
		//System.out.println("world initOsProperites: " + (System.currentTimeMillis() - start));
		//初始化类库路径
		// 设置类装载器
		worldClassLoader = new ThingClassLoader(classLoader);
		
		//System.out.println("world worldClassLoader: " + (System.currentTimeMillis() - start));
		// 设置library path，Java目前不建议使用该方法，因此屏蔽先
		//initLibraryPath();
		
		//System.out.println("world initLibraryPath: " + (System.currentTimeMillis() - start));
		// 初始化项目等
		metaThing = new MetaThing();
		//System.out.println("world init metathing: " + (System.currentTimeMillis() - start));		
		//thingManagers.add(classThingManager);
		
		// 初始化子系统，此方法应该属于IDE的方法，于2014-10-30日由张玉祥取消
		//Thing config = getThing("xworker.lang.config.Project");
		//if(config != null){
		//	try{
		//		config.doAction("init");
		//	}catch(Exception e){
		//		log.warn("init prjects error", e);
		//	}
		//}
		
		//初始化通过.lib文件自定义的类库
		getClassLoader().initLibs();
		
		Thread.currentThread().setContextClassLoader(worldClassLoader);
		
		//添加World目录下的事物管理器
		thingManagers.clear();
		refresh();
		
		//System.out.println("world refresh: " + (System.currentTimeMillis() - start));
		thingManagers.add(transientThingManager);
		
		//System.out.println("world initLibs: " + (System.currentTimeMillis() - start));
		
        //重新设置元事物如果存在, 2015-03-18加入，因为一些事物已经使用这个xworker.lang.MetaThing
        Thing baseClass = getThing("xworker.lang.MetaThing");
        if(baseClass != null){
        	baseClass = baseClass.detach();
	        //保留元事物的路径
        	baseClass.getMetadata().setPath("xworker.lang.MetaThing");
        	baseClass.initChildPath();
	        this.metaThing = baseClass;
        }
        
       // System.out.println("world init metathing: " + (System.currentTimeMillis() - start));
        
		//设置状态为已初始化，避免其他地方重复初始化
		inited = true;
	}

	/**
	 * 返回是否已经初始化过。
	 * 
	 * @return 是否已经初始化
	 */
	public boolean isInited(){
		return inited;
	}

	/*
	public static void addLibraryDir(String s) {
		try {
			java.lang.reflect.Field field = ClassLoader.class
					.getDeclaredField("usr_paths");
			field.setAccessible(true);
			String[] paths = (String[]) field.get(null);
			for (int i = 0; i < paths.length; i++) {
				if (s.equals(paths[i])) {
					return;
				}
			}
			String[] tmp = new String[paths.length + 1];
			System.arraycopy(paths, 0, tmp, 0, paths.length);
			tmp[paths.length] = s;
			field.set(null, tmp);
		} catch (Exception e) {
			log.log(Level.FINER, "error on init library path", e);
		}
	}*/

	/**
	 * 刷新当前世界中项目。
	 * 
	 */
	public void refresh() {
		//long start = System.currentTimeMillis();
		
		Map<String, String> context = new HashMap<>();
		for(ThingManager manager : thingManagers){
			context.put(manager.getName(), manager.getName());
		}
		
		File projectsFiles = new File(worldPath, "projects");
		if(projectsFiles.exists()){
			
			for(File projectFile : projectsFiles.listFiles()){
				if(projectFile.isFile()){
					continue;
				}
				
				
				String name = projectFile.getName();
				if(context.get(name) != null){
					continue;
				}else{
					this.initThingManager(projectFile);
					context.put(name, name);
				}
				
				//System.out.println("Init thing manager " + name + ": " + (System.currentTimeMillis() - start));
			}
			//System.out.println("Init thing manager all : " + (System.currentTimeMillis() - start));
		}
		
		//初始化JarThingManager
		new JarThingManagerIniter().run();		
	}

	/**
	 * <p>注册事物管理器的监听者。</p>
	 * 
	 * <p>这里注册的监听者是所有事物管理者共有的，注册的键值是事物管理者的名称，不同项目下同名的事物管理者可以通过自身的名字获得相同
	 * 的事物管理者事件监听者列表，如果注册时事物管理者名称为*，那么表示此事件监听者监听所有事物管理者的事件。</p>
	 * 
	 * <p>虽然在World中注册了事物管理者监听事件，但在具体的事物管理者实现中要使用注册的监听者，那么需在触发 事件通过World取得相应的监听者列表。</p>
	 * 
	 * 如要监听具体的事物管理者，可获得具体的事物管理者然后调用事物管理者的addListener方法添加监听。
	 * 
	 * @param thingManagerName
	 *            事物管理者的名称
	 * @param listener
	 *            事物管理者事件监听者
	 */
	public void registThingManagerListener(String thingManagerName,
			ThingManagerListener listener) {
		List<ThingManagerListener> listeners = thingManagerListeners
				.get(thingManagerName);
		if (listeners == null) {
			listeners = new ArrayList<ThingManagerListener>();
			thingManagerListeners.put(thingManagerName, listeners);
		}

		if (listener != null && !listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void runActionAsync(String actionPath, ActionContext actionContext) {
		runActionAsync(actionPath, actionContext, null);
	}

	public void runActionAsync(String actionPath,
			final ActionContext actionContext,
			final Map<String, Object> parameters) {
		final Action action = getAction(actionPath);
		if (action == null) {
			log.info("can not find the action : " + actionPath);
		} else {
			new Thread(new Runnable() {
				public void run() {
					action.run(actionContext, parameters);
				}
			}).start();
		}
	}

	public Object runAction(String actionPath, ActionContext actionContext) {
		return runAction(actionPath, actionContext, null);
	}

	/**
	 * 执行一个动作。
	 * 
	 * @param actionPath
	 *            动作路径
	 * @param actionContext
	 *            动作上下文
	 * @param parameters
	 *            参数
	 * 
	 * @return 动作执行后的返回结果
	 */
	public Object runAction(String actionPath, ActionContext actionContext,
			Map<String, Object> parameters) {
		Action action = getAction(actionPath);
		if (action == null) {
			log.info("can not find the action : " + actionPath);

			return null;
		} else {
			return action.run(actionContext, parameters);
		}
	}

	/**
	 * 设置一个用户数据。
	 * 
	 * @param key 键
	 * @param value 值
	 */
	public void setData(String key, Object value) {
		if(value == null){
			userDatas.remove(key);
		}else{
			userDatas.put(key, value);
		}
	}
	
	/**
	 * 移除一个用户数据。
	 * 
	 * @param key 键
	 */
	public void removeData(String key){
		userDatas.remove(key);
	}

	/**
	 * 取消事物管理者的监听者。
	 * 
	 * @param thingManagerName
	 *            事物管理者的名称
	 * @param listener
	 *            事物管理者监听
	 */
	public void unregistThingManagerListener(String thingManagerName,
			ThingManagerListener listener) {
		List<ThingManagerListener> listeners = thingManagerListeners
				.get(thingManagerName);
		if (listeners != null) {
			listeners.remove(listener);
		}
	}

	public ActionListener getActionListener() {
		return actionListener;
	}

	public void setActionListener(ActionListener actionListener) {
		this.actionListener = actionListener;
	}

	public boolean isHaveActionListener(){
		return actionListener != null;
	}
	
	/**
	 * 返回指定的文件名是否是保存事物的文件。
	 * 
	 * @param fileName 文件名
	 * @return 是否是事物文件
	 */
	public boolean isThingFile(String fileName){
		for(ThingCoder coder : this.thingCoders){
			if(fileName.endsWith("." + coder.getType()));
		}
		
		return false;
	}

	/**
	 * 添加事物管理器。
	 * 
	 * @param thingManager 事物管理器
	 */
	public void addThingManager(ThingManager thingManager){
		checkIfThingManagerExists(thingManager);
		
		thingManagers.add(thingManager);
	}
	
	public ClassThingManager getClassThingManager() {
		return classThingManager;
	}

	/**
	 * 添加事物管理器到最前面。
	 * 
	 * @param thingManager 事物管理器
	 */
	public void addThingManagerFirst(ThingManager thingManager){		
		checkIfThingManagerExists(thingManager);
		
		thingManagers.add(0, thingManager);
	}
	
	/**
	 * 先从事物管理器列表中移除，然后再加到最前面。如果移除失败（不在事物管理器列表中），则不会添加。
	 * @param thingManager
	 */
	public void moveThingManagerToFirst(ThingManager thingManager) {
		if(thingManagers.remove(thingManager)) {
			thingManagers.add(0, thingManager);
		}
	}
	
	private void checkIfThingManagerExists(ThingManager thingManager){
		for(ThingManager manager : thingManagers){
			if(manager.getName().equals(thingManager.getName())){
				throw new XMetaException("ThingManager already exists, name=" + manager.getName());
			}
		}
	}
	
	/**
	 * 根据事物管理器的名称返回事物管理器。
	 * 
	 * @param name 名称
	 * @return 事物管理器
	 */
	public ThingManager getThingManager(String name){
		for(ThingManager manager : thingManagers){
			if(manager.getName().equals(name)){
				return manager;
			}
		}
		
		return null;
	}
	
	/**
	 * 移除事物管理器。
	 * 
	 * @param thingManager 事物管理器
	 */
	public void removeThingManager(ThingManager thingManager){
		removeThingManager(thingManager, true);
	}

	public void removeThingManager(ThingManager thingManager, boolean deleteRes){
		for(int i=0; i<thingManagers.size(); i++){
			ThingManager tm = thingManagers.get(i);
			if(tm == thingManager || tm.getName().equals(thingManager.getName())){
				thingManagers.remove(i);
				break;
			}
			
		}
		
		if(deleteRes && thingManager.getRootDir() != null){
			UtilFile.delete(thingManager.getRootDir());
		}
		/*
		if(deleteRes && thingManager instanceof FileThingManager){
			//同时删除目录
			FileThingManager fm = (FileThingManager) thingManager;
			UtilFile.delete(fm.getRootFile());
		}*/
	}
	
	public String getOS(){
		return OS;
	}
	
	public String getJVMBit(){
		return PROCESSOR_ARCHITECTURE;
	}
	
	public ThingManager initThingManager(File rootPath){
		String name = rootPath.getName();
		return initThingManager(rootPath, name);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ThingManager initThingManager(File rootPath, String name){
		String thingManagerClass = null;
		
		String link = null;
		
		//如果是加载失败的不重复加载
		if(isFailureThingManager(name)){
			return null;
		}
		try{
			if(rootPath.getCanonicalFile().equals(new File(this.getPath()).getCanonicalFile())){			
				return null;
			}
		}catch(Exception e){
			log.log(Level.WARNING, "XWorker home can not be a project", e);
		}
		
		boolean hasThingsDir = false;
		Properties properties = new Properties();
		if(rootPath.isDirectory()){
			File configFile = new File(rootPath, "config.properties");
			hasThingsDir = true;
			if(!configFile.exists()){
				configFile = new File(rootPath, "xworker.properties");
			}			
			
			if(!configFile.exists()){
				configFile = new File(rootPath, ".dml");
				if(configFile.exists()) {
					hasThingsDir = false;
				}
			}
			if(!configFile.exists()){
				configFile = new File(rootPath, "dml.properties");
				if(configFile.exists()) {
					hasThingsDir = false;
				}
			}
			if(!configFile.exists()){
				configFile = new File(rootPath, "dml.prj");
				if(configFile.exists()) {
					hasThingsDir = false;
				}
			}
			if(configFile.exists()){				
				FileInputStream fin = null; 
				try{
					fin = new FileInputStream(configFile);
					properties.load(fin);
					
					//项目名称
					String pname = properties.getProperty("projectName");
					if(pname == null || "".equals(pname)) {
						pname = properties.getProperty("name");
					}
					if(pname != null && !"".equals(pname.trim())){
						name = pname.trim();
					}
					
					//link是指链接到其他目录
					link = properties.getProperty("link");
					if((link != null && link.trim().equals("")) || "null".equals(link)){
						link = null;
					}
					
					if("false".equals(properties.getProperty("hasThingsDir"))) {
						hasThingsDir = false;
					}
					
					//事物管理器的类
					thingManagerClass = properties.getProperty("class");
				}catch(Exception e){
					this.failureThingManangers.add(name);
					throw new XMetaException("init thing manager error, path=" + rootPath, e);
				}finally{
					if(fin != null){
						try {
							fin.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		
		boolean isLink = false;
		if(link != null){
			File linkFile = new File(link);
			if(!linkFile.exists()){
				linkFile = new File(worldPath, link);
			}
			if(!linkFile.exists()){
				linkFile = new File(rootPath.getParentFile(), link);
			}
			if(linkFile.exists()){
				isLink = true;
				String linkName = linkFile.getName().toLowerCase();
				if(linkFile.isFile() && (linkName.endsWith(".jar") || linkName.endsWith(".zip"))){
					//创建并添加到末尾
					ThingManager thingManager = new JarThingManager(name, linkFile);
					thingManager.init(properties);
					addThingManager(thingManager);
					
					//添加到类库
					getClassLoader().addJarOrZip(linkFile);
					addJarOrZip(rootPath);							
					thingManager.setRootDir(rootPath);
					return thingManager;
				}else{
					ThingManager thingManager = new FileThingManager(name, linkFile);
					thingManager.init(properties);
					addThingManager(thingManager);
					
					//添加类库
					addJarOrZip(linkFile);		
					thingManager.setRootDir(rootPath);
					return thingManager;
				}
			}
		}
		
		if(!isLink){
			ThingManager thingManager = null;
			if(thingManagerClass != null){
				try {
					Class cls = Class.forName(thingManagerClass);
					Constructor constructor = cls.getConstructor(new Class[]{String.class, File.class});
					if(constructor != null){
						thingManager = (ThingManager) constructor.newInstance(new Object[]{name, rootPath});
					}else{
						thingManager = (ThingManager) cls.getConstructor(new Class[] {}).newInstance();
					}
				} catch (Exception e) {
					log.log(Level.WARNING, "can not load thingManager", e);
				}
				if(thingManager == null){
					this.failureThingManangers.add(name);
					return null;
				}
			}else{
				thingManager = new FileThingManager(name, rootPath, hasThingsDir);
			}
			
			try{
				thingManager.init(properties);
			}catch(Exception e){				
				log.log(Level.WARNING, "init thingManager error", e);
				this.failureThingManangers.add(name);
				return null;
			}
			addThingManager(thingManager);
			
			//添加类库
			addJarOrZip(rootPath);			
			thingManager.setRootDir(rootPath);
			return thingManager;
		}else{
			return null;
		}
	}
	
	private void addJarOrZip(File dir) {
		getClassLoader().addJarOrZip(new File(dir, "lib"));
		getClassLoader().addJarOrZip(new File(dir, "lib_" + OS));
		getClassLoader().addJarOrZip(new File(dir, "lib_" + OS + "_" + PROCESSOR_ARCHITECTURE));
		
		//增加了WEB-INF下的包的加载，如果一个外部项目即是WEB应用，又是普通应用时需要
		File webInfo = new File(dir, "WEB-INF");
		if(webInfo.exists() && webInfo.isDirectory()) {
			addJarOrZip(webInfo);
		}
	}
	
	/**
	 * 添加运行期的帮助。
	 * 
	 * @param title 标题
	 * @param sourcePath 范围
	 * @param helpPath 路径事物
	 */
	public void addHelp(String title, String sourcePath, String helpPath){
		Help help = new Help(title, sourcePath, helpPath);
		helps.add(help);
		if(helps.size() > this.helpSize){
			helps.remove(0);
		}
	}
	
	public List<Help> getHelps(){
		return helps;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public TransientThingManager getTransientThingManager() {
		return transientThingManager;
	}
	
	/**
	 * 注册事物的编码编码和解码器。
	 * 
	 * @param thingCoder 编码器的实例
	 */
	public void registThingCoder(ThingCoder thingCoder){
		thingCoders.add(thingCoder);
	}
	
	/**
	 * 返回编码器列表。
	 * 
	 * @return 事物编码劣列表
	 */
	public List<ThingCoder> getThingCoders(){
		return thingCoders;
	}

	/**
	 * 通过类型返回事物的编码器，类型通常是文件的后缀名。
	 * 
	 * @param type 编码类型
	 * @return  事物编码器
	 */
	public ThingCoder getThingCoder(String type){
		for(ThingCoder thingCoder : thingCoders){
			if(thingCoder.acceptType(type)){
				return thingCoder;
			}
		}
		
		return null;
	}

	public String getWebFileRoot() {
		return webFileRoot;
	}

	public void setWebFileRoot(String webFileRoot) {
		this.webFileRoot = webFileRoot;
	}
	
	/**
	 * 把一个目录当作文件事物管理器加入到事物管理器列表中，如果目录是world的home目录，那么过滤不生效。
	 * 
	 * @param name 事物管理器名称
	 * @param dir 目录
	 * @param hasThingDir 是否包含things子目录
	 * @param first 是否添加到第一个事物管理器中
	 * @throws IOException 
	 */
	public void addFileThingManager(String name, File dir, boolean hasThingDir, boolean first) throws IOException{
		File worldDir = new File(this.getPath());
		if(worldDir.getCanonicalFile().equals(dir.getCanonicalFile())){
			return;
		}
		
		ThingManager tm = new FileThingManager(name, dir, hasThingDir);
		if(first){
			this.addThingManagerFirst(tm);
		}else{
			this.addThingManager(tm);
		}
	}
	
	/**
	 * 返回工作目录对应的事物管理器，用于XWorker之外项目获取自身的事物管理器。
	 * 
	 * @return 事物管理器，总是返回第一个事物管理
	 */
	public ThingManager getWorkDirThingManager(){
		return thingManagers.get(0);
	}
}