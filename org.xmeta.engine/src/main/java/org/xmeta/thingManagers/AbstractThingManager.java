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
package org.xmeta.thingManagers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xmeta.Category;
import org.xmeta.Thing;
import org.xmeta.ThingIndex;
import org.xmeta.ThingManager;
import org.xmeta.ThingManagerListener;
import org.xmeta.World;
import org.xmeta.cache.ThingCache;

/**
 * 抽象的事物管理器。
 * 
 * @author zhangyuxiang
 *
 */
public abstract class AbstractThingManager implements ThingManager{
	/** 事物管理者的名称 */
	protected String name = null;
	
	/** 根包 */
	Category rootCategory = null;
	
	/** 事物管理监听列表 */
	protected List<ThingManagerListener> listeners = new ArrayList<ThingManagerListener>();	
	
	/** 包列表 */
	protected List<Package> packages = new ArrayList<Package>();
	
	/**
	 * 抽象事物管理者的构造方法。
	 * 
	 * @param name 事物管理者的名称
	 */
	public AbstractThingManager(String name){
		this.name = name;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingFactory#clearCache()
	 */
	public void clearCache() {
		//事物的缓存都在World中完成了，这里已经不需要
	}

	public Category getCategory(String categoryName) {
		return getCategory(categoryName, false);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingFactory#getCategory(java.lang.String)
	 */
	public Category getCategory(String categoryName, boolean refresh) {
		if(categoryName == null || "".equals(categoryName)){
			return rootCategory;
		}
		
		Category pkg = rootCategory;
		
		for(String pkName : categoryName.split("[.]")){
			Category childPkg = pkg.getCategory(pkName);
			if(childPkg == null){
				if(refresh){
				    pkg.refresh();
				}
				pkg = pkg.getCategory(pkName);
			}else{
				pkg = childPkg;
			}
			if(pkg == null){
				return null;
			}
		}
		
		return pkg;
	}

	/**
	 * 刷新父目录，在创建或删除目录时使用。
	 * 
	 * @param categoryName 目录名
	 */
	public void refreshParentCategory(String categoryName){
		if(categoryName == null || "".equals(categoryName)){
			rootCategory.refresh();
		}
		
		int index = categoryName.lastIndexOf(".");
		if(index != -1){
			String name = categoryName.substring(0, index);
			Category category = this.getCategory(name);
			if(category != null){
				category.refresh();
			}else{
				refreshParentCategory(name);
				
				category = this.getCategory(name);
				if(category != null){
					category.refresh();
				}
			}
		}		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingFactory#getCategorys()
	 */
	public List<Category> getCategorys() {
		return rootCategory.getCategorys();
	}
	

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingFactory#getName()
	 */
	public String getName() {
		return name;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingManager#getThingIndexs()
	 */
	public List<ThingIndex> getThingIndexs(String categoryName) {
		Category category = getCategory(categoryName);
		if(category != null){
			return category.getThingIndexs();
		}else{
			return null;
		}
	}
	

	@Override
	public List<ThingIndex> getThingIndexs(String categoryName, String descriptorPath) {
		Category category = getCategory(categoryName);
		if(category != null){
			return category.getThingIndexs(descriptorPath);
		}else{
			return null;
		}
	}

	@Override
	public List<Thing> getThings(String categoryName, String descriptorPath) {
		Category category = getCategory(categoryName);
		if(category != null){
			return category.getThings(descriptorPath);
		}else{
			return null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingManager#getThings()
	 */
	public List<Thing> getThings(String categoryName) {
		Category category = getCategory(categoryName);
		if(category != null){
			return category.getThings();
		}else{
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingManager#iterator()
	 */
	public Iterator<Thing> iterator(final String categoryName, final boolean includeChildCategory) {
		return iterator(categoryName, null, includeChildCategory);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingManager#iterator(java.lang.String, java.lang.String, boolean)
	 */
	public Iterator<Thing> iterator(final String categoryName, final String descriptorPath, final boolean includeChildCategory) {
		Category category = getCategory(categoryName);
		return category.iterator(descriptorPath, includeChildCategory);		
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingManager#refresh(java.lang.String)
	 */
	public void refresh(String categoryName, boolean includeChildCategory) {
		Category category = getCategory(categoryName);
		if(category != null){
			category.refresh(includeChildCategory);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingManager#addThingManagerListener(org.xmeta.ThingManager)
	 */
	public void addThingManagerListener(ThingManagerListener listener) {
		listeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingManager#removeThingManagerListener(org.xmeta.ThingManagerListener)
	 */
	public boolean removeThingManagerListener(ThingManagerListener listener) {
		return listeners.remove(listener);
	}

	@Override
	public Thing getThing(String thingName) {
		//需要先从缓存中读取，否则当遍历iterator时每次都会读取新的，是否和World中的缓存重复呢？
		Thing thing = ThingCache.get(thingName);
		if(thing != null){
			if(thing.getMetadata().isRemoved()){
				//如事物的文件被外部改动或事物已给标记为删除，需要重新读取
				thing = null;
			}else{
				return thing;
			}
		}
		
		thing = doLoadThing(thingName);
		if(thing != null){
			for(ThingManagerListener listener : listeners){
				listener.loaded(this, thing);
			}
			
			for(ThingManagerListener listener : World.getInstance().getThingManagerListeners(getName())){
            	listener.loaded(this, thing);
            }
		}
		
		return thing;
	}
	
	/**
	 * 装载事物。
	 * 
	 * @param thingName 事物名
	 * @return 事物
	 */
	public abstract Thing doLoadThing(String thingName);

	@Override
	public boolean remove(Thing thing) {
		boolean removed = doRemoveThing(thing);
		if(thing != null){
			for(ThingManagerListener listener : listeners){
				listener.removed(this, thing);
			}
			
			for(ThingManagerListener listener : World.getInstance().getThingManagerListeners(getName())){
            	listener.removed(this, thing);
            }
		}
		
		return removed;
	}

	/**
	 * 执行删除事物。
	 * 
	 * @param thing 事物
	 * @return 是否成功
	 */
	public abstract boolean  doRemoveThing(Thing thing);
	
	@Override
	public boolean save(Thing thing) {
		boolean removed = doSaveThing(thing);
		if(thing != null){
			for(ThingManagerListener listener : listeners){
				listener.saved(this, thing);
			}
			
			for(ThingManagerListener listener : World.getInstance().getThingManagerListeners(getName())){
            	listener.saved(this, thing);
            }
		}
		
		return removed;
	}
	
	/**
	 * 执行保存事物。
	 * 
	 * @param thing 事物
	 * @return 是否保存成功
	 */
	public abstract boolean doSaveThing(Thing thing);
}