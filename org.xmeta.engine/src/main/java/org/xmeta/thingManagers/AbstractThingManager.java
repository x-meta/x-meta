/*
    X-Meta Engine。
    Copyright (C) 2013  zhangyuxiang

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
    For alternative license options, contact the copyright holder.

    Emil zhangyuxiang@tom.com
 */
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

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingFactory#getCategory(java.lang.String)
	 */
	public Category getCategory(String categoryName) {
		if(categoryName == null || "".equals(categoryName)){
			return rootCategory;
		}
		
		Category pkg = rootCategory;
		
		for(String pkName : categoryName.split("[.]")){
			Category childPkg = pkg.getCategory(pkName);
			if(childPkg == null){
				pkg.refresh();
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
	 * @param thingName
	 * @return
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
	 * @param thing
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
	 * @param thing
	 * @return
	 */
	public abstract boolean doSaveThing(Thing thing);
}