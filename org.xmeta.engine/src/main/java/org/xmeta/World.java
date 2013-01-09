/*
 * Copyright 2007-2008 The X-Meta.org.
 * 
 * Licensed to the X-Meta under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The X-Meta licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xmeta;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmeta.cache.ThingCache;
import org.xmeta.cache.ThingEntry;
import org.xmeta.codes.XerThingCoder;
import org.xmeta.codes.XmlThingCoder;
import org.xmeta.thingManagers.ClassThingManager;
import org.xmeta.thingManagers.FileMonitor;
import org.xmeta.thingManagers.FileThingManager;
import org.xmeta.thingManagers.JarThingManager;
import org.xmeta.thingManagers.TransientThingManager;
import org.xmeta.util.ThingClassLoader;
import org.xmeta.util.ThingOgnlAccessor;
import org.xmeta.util.UtilFile;

/**
 * 世界是存放事物的容器。
 * <p/>
 * 
 * 可以通过事物的路径访问这个世界下的所有事物。<p/>
 *
 * 事物的路径规则参看{@link org.xmeta.Path}}<br/>
 * 
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 * 
 */
public class World {
	/** 日志 */
	private static Logger log = LoggerFactory.getLogger(World.class);

	/** 世界的唯一单态实例 */
	private final static World worldInstance = new World();

	/** 瞬态事物的管理者 */
	private TransientThingManager transientThingManager = new TransientThingManager();
	private ClassThingManager classThingManager = new ClassThingManager();

	/** 元事物 */
	public Thing metaThing = null;// new MetaThing();

	/** 全局上下文 */
	protected List<ThingEntry> globalContexts = new CopyOnWriteArrayList<ThingEntry>();

	/** 世界的路径，项目、目录和存放事物的根目录 */
	private String worldPath = ".";

	/** 用户数据 */
	private Map<String, Object> userDatas = new HashMap<String, Object>();

	/** 公共事物管理这的监听者注册列表 */
	private Map<String, List<ThingManagerListener>> thingManagerListeners = new ConcurrentHashMap<String, List<ThingManagerListener>>();

	/** 世界级的类装载器 */
	private ThingClassLoader worldClassLoader = null;
	
	/** 动作监听器  */
	private ActionListener actionListener = null;
	
	/** 帮助列表，运行时对用户解决问题可能有帮助的内容，可以在任何一个事物的帮助菜单中查看 */
	private List<Help> helps = new ArrayList<Help>();
	/** 帮助列表的最大个数 */
	public int helpSize = 100;
	/** 是否打印装载信息 */
	private boolean verbose = false;
	/** 打印verbose的缓存，相同的不需要再次打印 */
	private Map<String, String> verboseThingCache = null;
	/** 事物管理器列表 */
	private List<ThingManager> thingManagers = new ArrayList<ThingManager>();
	/** 事物编码器 */
	private List<ThingCoder> thingCoders = new ArrayList<ThingCoder>();
	/** 目录缓存 */
	private Map<String, CategoryCache> categoryCaches = new HashMap<String, CategoryCache>();
	
	/**
	 * 运行时所有的事物基本都通过World获取，为提交性能增加路径缓存。
	 * 
	 * 路径缓存1和路径缓存2，未避免系统长期使用而导致路径缓存无限增长，每隔一段时间情况pathCache2，然后pathCach1和pahCach2互换 */
	private Map<String, Path> pathCache1 = new HashMap<String, Path>(5000);
	private Map<String, Path> pathCache2 = new HashMap<String, Path>(5000);

	/**
	 * 私有构造方法，目前系统中只允许存在一个世界。
	 * 
	 */
	private World() {
		try{
			ThingOgnlAccessor.init();
		}catch(Exception e){			
		}
	}

	/**
	 * 取得世界的实例。
	 * 
	 * @return 世界的实例
	 */
	public static World getInstance() {
		return worldInstance;
	}
	/**
	 * 创建一个ThingManager，必须是全路径<projectName>:<thingManagerName>。
	 * 
	 * @param path
	 */
	public ThingManager createThingManager(String name, String link){
		File managerRootFile = new File(this.getPath() + "/projects/" + name);
		if(managerRootFile.exists()){
			throw new XMetaException("ThingManager already exists, name=" + name);
		}
		managerRootFile.mkdirs();
		if(link != null){
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
		}
		
		return initThingManager(managerRootFile);
	}
	
	/**
	 * 添加全局上下文。
	 * 
	 * @param contextThing
	 */
	public void addGlobalContext(Thing contextThing, int index) {
		boolean have = false;
		for (ThingEntry entry : globalContexts) {
			Thing thing = entry.getThing();
			if (thing != null
					&& thing.getMetadata().getPath().equals(
							contextThing.getMetadata().getPath())) {
				have = true;
				break;
			}
		}

		if (!have) {
			ThingEntry entry = new ThingEntry(contextThing);
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
	 * @param contextThing
	 */
	public void removeGlobalContext(Thing contextThing) {
		ThingEntry forRemoved = null;
		for (ThingEntry entry : globalContexts) {
			Thing thing = entry.getThing();
			if (thing != null
					&& thing.getMetadata().getPath().equals(
							contextThing.getMetadata().getPath())) {
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
	 * @param path
	 *            路径
	 * @return 路径所对应的事物
	 */
	public Object get(String pathStr) {
		if(pathStr == null || "".equals(pathStr)){
			return this;
		}
		
		Path path = pathCache1.get(pathStr);
		if(path == null){
			path = pathCache2.get(pathStr);
			if(path == null){
				path = new Path(pathStr);
			}
			pathCache1.put(pathStr, path);
		}
		
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
				String categoryPath = path.getPath();
				String thingName;
				int index = categoryPath.lastIndexOf(".");
				if(index != -1){
					thingName = categoryPath.substring(index + 1, categoryPath.length());
					categoryPath = categoryPath.substring(0, index);					
				}else{
					thingName = categoryPath;
					categoryPath = null;
				}
				CategoryCache  categoryCache = categoryCaches.get(categoryPath);
				if(categoryCache != null){
					thing = categoryCache.getThing(thingName);
				}
				if(thing != null){
					ThingCache.put(path.getPath(), thing);
				}else{				
					//按照事务管理器列表，获取第一个事物
					for(int i=0; i<thingManagers.size(); i++){
						ThingManager thingManager = thingManagers.get(i);
						thing = thingManager.getThing(path.getPath());
						if(thing != null){
							path.setType(Path.TYPE_THING);
							break;
						}
					}
					
					if(thing != null){
						ThingCache.put(path.getPath(), thing);
						if(categoryCache == null){
							categoryCache = new CategoryCache();
							categoryCache.addCategory(thing.getMetadata().getCategory());
							categoryCaches.put(categoryPath, categoryCache);
						}
					}else{
						//查找是否是目录
						Category category = null;
						for(int i=0; i<thingManagers.size(); i++){
							ThingManager thingManager = thingManagers.get(i);
							category = thingManager.getCategory(path.getPath());
							if(category != null){
								path.setType(Path.TYPE_CATEGORY);
								return category;
							}
						}
					}
				}
			}
		}
		
		if(thing == null){
			thing = loadThingFromClasspath(path.getPath());
		}
		if(thing != null){
			return thing.get(path);
		}else{
			return null;
		}
	}
	
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
	 * @param project
	 * @param manager
	 * @param thingCategory
	 * @return
	 */
	public Thing loadThingFromClasspath(String thingPath){		
		Thing thing = null;
		for(ThingCoder coder : this.thingCoders){
			String resource = "/" + thingPath.replace('.', '/') + "." + coder.getType();
			URL url = this.getClass().getResource(resource);
			
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
						
						long lastModified = 0;
						if(url.getProtocol().toLowerCase().equals("file")){
							lastModified = new File(url.getFile()).lastModified();
						}
						coder.decode(thing, fin, lastModified);
					}catch(Exception e){
						System.out.println("load thing error, thingPath=" + thingPath);
						e.printStackTrace();
						return null;
					}finally{
						if(thing != null){
							thing.endModify(false);
						}
						
						if(fin != null){
							try {
								fin.close();
							} catch (IOException e) {
							}
						}
					}
					
					//如果是文件，加入到文件监控，文件变化了可以重新读取事物，可用于在Java项目手工编程时
					if(url.getProtocol().toLowerCase().equals("file")){
						FileMonitor.getInstance().addFile(thingPath, thing, new File(url.getFile()));
					}
					
					//放到缓存中
					ThingCache.put(metadata.getPath(), thing);
					
					return thing;
				} catch (IOException e) {
					//e.printStackTrace();
					log.error("load thing from classpath error", e);
				}
			}
		}
		return null;
	}

	/**
	 * 以流的形式返回资源，如果没有返回null。首先从文件系统中找，其次从classpath下寻找资源。
	 * 
	 * @param path
	 * @return
	 */
	public InputStream getResourceAsStream(String path) throws IOException{
		if(path == null){
			return null;
		}
		
		path = path.replace('\\', '/');
		if(path.startsWith("world|")){
			//相对于World的路径
			path = World.getInstance().getPath() + "/" + path.substring(6, path.length());	
			
			File file = new File(path);
			if(file.exists()){
				return new FileInputStream(file);
			}else{
				return this.getClassLoader().getResourceAsStream("/" + path.substring(6, path.length()));
			}
		}else if(path.startsWith("project|") || path.indexOf(":") != -1){
			//基于项目的路径，旧的路径规则，已不再使用
			int index = path.indexOf(":");
			String projectName = null;
			if(path.startsWith("project|")){
				projectName = path.substring(8, index);
			}else{
				projectName = path.substring(0, index);
			}
			path = path.substring(index + 1, path.length());
			path = "/" + projectName + "/" + path;
			for(ThingManager thingManager : getThingManagers()){
				InputStream in = thingManager.getResourceAsStream(path);
				if(in != null){
					return in;
				}
			}
			
			return getClassLoader().getResourceAsStream(path);
		}else{
			//先从ThingManager中找
			if(path.startsWith("/")){
				for(ThingManager thingManager : getThingManagers()){
					InputStream in = thingManager.getResourceAsStream(path);
					if(in != null){
						return in;
					}
				}
			}
			
			//直接从文件系统中找
			File file = new File(path);
			if(file.exists()){
				return new FileInputStream(file);
			}else{
				if(path.startsWith("/")){
					return World.class.getResourceAsStream(path);
				}else{
					return World.class.getResourceAsStream("/" + path);
				}
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
	 * @param actionPath
	 * @param actionContext
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Class getActionClass(String actionPath, ActionContext actionContext){
		return getActionClass(getThing(actionPath), actionContext);
	}
	
	/**
	 * 返回动作类，通常是动作事物对应的类，有些动作可能没有对应类。
	 * 
	 * @param actionThing
	 * @param actionContext
	 * @return
	 */
	@SuppressWarnings("unchecked")
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
	 * @param key
	 * @return
	 */
	public Object getData(String key) {
		return userDatas.get(key);
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
	 * @param actionContext
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
	 * 获取事物管理器列表。
	 * 
	 * @return
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

	/**
	 * 通过给定事物的存放路径来初始化世界。
	 * 
	 * @param worldPath
	 *            世界的路径
	 */
	public void init(String worldPath) {
		// 设置事物的路径
		File f = new File(worldPath);
		this.worldPath = f.getAbsolutePath();

		//初始化类库路径
		// 设置类装载器
		worldClassLoader = new ThingClassLoader(Thread.currentThread().getContextClassLoader());
		
		// 设置library path
		String libraryPath = System.getProperty("java.library.path");
		String worldLibraryPath = new File(f, "library").getAbsolutePath();
		libraryPath = libraryPath + File.pathSeparator + worldLibraryPath;
		System.setProperty("java.library.path", libraryPath);
		addLibraryDir(worldLibraryPath);

		// 初始化项目等
		metaThing = new MetaThing();
		refresh();
		
		//事物编码
		thingCoders.clear();
		thingCoders.add(new XerThingCoder());
		thingCoders.add(new XmlThingCoder());
		
		//添加World目录下的事物管理器
		thingManagers.clear();
		File projectsFiles = new File(f, "projects");
		if(projectsFiles.exists()){
			for(File projectFile : projectsFiles.listFiles()){
				this.initThingManager(projectFile);			
			}
		}
		thingManagers.add(transientThingManager);
		//thingManagers.add(classThingManager);
		
		// 初始化子系统，此方法应该属于IDE的方法
		Thing config = getThing("xworker.lang.config.Project");
		if(config != null){
			config.doAction("init");
		}
	}
	

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
			log.error("error on init library path", e);
		}
	}

	/**
	 * 刷新当前世界中项目。
	 * 
	 */
	public void refresh() {
	}

	/**
	 * 注册事物管理器的监听者。<p?.
	 * 
	 * 这里注册的监听者是所有事物管理者共有的，注册的键值是事物管理者的名称，不同项目下同名的事物管理者可以通过自身的名字获得相同
	 * 的事物管理者事件监听者列表，如果注册时事物管理者名称为*，那么表示此事件监听者监听所有事物管理者的事件。
	 * <p/>
	 * 
	 * 虽然在World中注册了事物管理者监听事件，但在具体的事物管理者实现中要使用注册的监听者，那么需在触发 事件通过World取得相应的监听者列表。
	 * <p/>
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
			if (log.isInfoEnabled()) {
				log.info("can not find the action : " + actionPath);
			}
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
			if (log.isInfoEnabled()) {
				log.info("can not find the action : " + actionPath);
			}

			return null;
		} else {
			return action.run(actionContext, parameters);
		}
	}

	/**
	 * 设置一个用户数据。
	 * 
	 * @param key
	 * @param value
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
	 * @param key
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
	 * 添加事物管理器。
	 * 
	 * @param index
	 * @param thingManager
	 */
	public void addThingManager(ThingManager thingManager){
		checkIfThingManagerExists(thingManager);
		
		thingManagers.add(thingManager);
	}
	
	/**
	 * 添加事物管理器到最前面。
	 * 
	 * @param thingManager
	 */
	public void addThingManagerFirst(ThingManager thingManager){
		checkIfThingManagerExists(thingManager);
		
		thingManagers.add(0, thingManager);
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
	 * @param name
	 * @return
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
	 * @param thingManager
	 */
	public void removeThingManager(ThingManager thingManager){
		thingManagers.remove(thingManager);
		
		//同时删除目录
		UtilFile.delete(new File(worldPath + "/projects/" + thingManager.getName()));
	}
	
	public ThingManager initThingManager(File rootPath){
		String name = rootPath.getName();
		String link = null;
		if(rootPath.isDirectory()){
			File configFile = new File(rootPath, "config.properties");
			if(configFile.exists()){
				Properties properties = new Properties();
				FileInputStream fin = null; 
				try{
					fin = new FileInputStream(configFile);
					properties.load(fin);
					//name = properties.getProperty("name");
					link = properties.getProperty("link");
					if(link != null && link.trim().equals("")){
						link = null;
					}
				}catch(Exception e){
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
		if(link != null){
			File linkFile = new File(link);
			if(!linkFile.exists()){
				linkFile = new File(worldPath, link);
			}
			String linkName = linkFile.getName().toLowerCase();
			if(linkFile.isFile() && (linkName.endsWith(".jar") || linkName.endsWith(".zip"))){
				//创建并添加到末尾
				ThingManager thingManager = new JarThingManager(name, linkFile);
				addThingManager(thingManager);
				
				//添加到类库
				getClassLoader().addJarOrZip(linkFile);
				getClassLoader().addJarOrZip(new File(rootPath, "lib"));
				return thingManager;
			}else{
				ThingManager thingManager = new FileThingManager(name, linkFile);
				addThingManager(thingManager);
				
				//添加类库
				getClassLoader().addJarOrZip(new File(linkFile, "lib"));
				return thingManager;
			}
		}else{
			ThingManager thingManager = new FileThingManager(name, rootPath);
			addThingManager(thingManager);
			
			//添加类库
			getClassLoader().addJarOrZip(new File(rootPath, "lib"));
			return thingManager;
		}
	}
	
	/**
	 * 添加运行期的帮助。
	 * 
	 * @param title
	 * @param sourcePath
	 * @param helpPath
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
	 * @return
	 */
	public List<ThingCoder> getThingCoders(){
		return thingCoders;
	}

	/**
	 * 通过类型返回事物的编码器，类型通常是文件的后缀名。
	 * 
	 * @param type
	 * @return
	 */
	public ThingCoder getThingCoder(String type){
		for(ThingCoder thingCoder : thingCoders){
			if(thingCoder.getType().equals(type)){
				return thingCoder;
			}
		}
		
		return null;
	}
}
