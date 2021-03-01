package org.xmeta;

import java.io.File;
import java.lang.reflect.Method;

import org.xmeta.util.ProjectClassLoader;
import org.xmeta.util.ThingRunner;

/**
 * Project使用独立的类加载器，每一个项目的World对象都是独立加载的。
 * 
 * @author zyx
 *
 */
public class Project {
	private ProjectClassLoader classLoader;
	private Object world;
	private Method getThing;
	private Method getAction;	
	
	public Project(File projectDir) {
		classLoader = new ProjectClassLoader(projectDir);
		
		try {
			Class<?> worldCls = classLoader.loadClass("org.xmeta.World");
			Method getInstance = worldCls.getMethod("getInstance", new Class<?>[0]);
			
			getThing = worldCls.getMethod("getThing", new Class<?>[]{String.class});
			getAction = worldCls.getMethod("getAction", new Class<?>[]{String.class});
			
			world = getInstance.invoke(null, new Object[0]);
			
			Method init = worldCls.getMethod("init", new Class<?>[] {String.class});
			init.invoke(world, new Object[] {World.getInstance().getPath()});
			
			initThingMangers(projectDir, 1);
		}catch(Exception e) {
			throw new ActionException("int project error", e);
		}
	}
	
	public ProjectClassLoader getClassLoader() {
		return classLoader;
	}
	
	public Object getWorld() {
		return world;
	}
	
	public Object createActionContext() {
		try {
			Class<?> cls = classLoader.loadClass("org.xmeta.ActionContext");
			return cls.getConstructor(new Class<?>[0]).newInstance(new Object[0]);			
		}catch(Exception e) {
			throw new ActionException("CreateActionContext eorr", e);
		}
	}
	private void initThingMangers(File dir, int level) {
		if(ThingRunner.isProject(dir)) {
			initThingManager(dir);
			return;
		}
		
		if(level > 3) {
			//最多遍历三级
			return;
		}
		for(File child : dir.listFiles()) {
			if(child.isDirectory()) {
				initThingMangers(child, level + 1);
			}
		}
	}
	
	public void initThingManager(File thingManagerDir) {
		try {
			Method initThingManager = world.getClass().getMethod("initThingManager", new Class<?>[] {File.class});
			initThingManager.invoke(world, new Object[] {thingManagerDir});
		}catch(Exception e) {
			throw new ActionException("Init thing manager eorr", e);
		}
	}
	
	public Object getThing(String path) {
		try {
			return getThing.invoke(world, new Object[] {path});
		} catch (Exception e) {
			throw new ActionException("Get thing error", e);
		} 
	}
	
	public Object getAction(String path) {
		try {
			return getAction.invoke(world, new Object[] {path});
		} catch (Exception e) {
			throw new ActionException("Get action error", e);
		} 
	}
}
