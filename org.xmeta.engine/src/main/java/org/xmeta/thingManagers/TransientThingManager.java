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
package org.xmeta.thingManagers;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.xmeta.Category;
import org.xmeta.Thing;
import org.xmeta.ThingIndex;
import org.xmeta.ThingManager;
import org.xmeta.ThingManagerListener;
import org.xmeta.World;
import org.xmeta.util.ThingClassLoader;

/**
 * 瞬态的事物的管理者。<p/>
 * 
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public class TransientThingManager implements ThingManager{
	private static TransientFinalizer transientFinalizer = new TransientFinalizer();
	
	/** 瞬态管理者的目录只有一个，就是根目录 */
	TransientCategory category = new TransientCategory(this);
	
	/** 瞬态管理者的目录列表（只包含根目录的）*/
	List<Category> categorys = new CopyOnWriteArrayList<Category>();
	
	/** 保存瞬态事物的Map，使用弱引用保存事物，这样事物在其他地方没有引用时可以被虚拟机回收 */
	Map<String, WeakReference<Thing>> things = new ConcurrentHashMap<String, WeakReference<Thing>>();
	
	/** 瞬态事物的索引列表 */
	List<ThingIndex> thingIndexs = new CopyOnWriteArrayList<ThingIndex>();
	
	/** 所有的瞬态事物会被分配给唯一标识，这个标识用long类型的值来递增生成 */
	private long transientId = 0;
		
	public TransientThingManager(){
		categorys.add(category);
		
		transientFinalizer.addTransientManager(this);
	}
	
	/**
	 * 获取下一个瞬态标识。
	 * 
	 * @return 标识
	 */
	public synchronized long getNextId(){
		if(transientId == Long.MAX_VALUE){
			transientId = 0;
		}
		
		//获得一个有效的标识
		while(true){
			transientId++;
			if(things.get("_transient." + transientId) == null){
				return transientId;
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingFactory#getName()
	 */
	public String getName(){
		return "_transient";
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingFactory#clearCache()
	 */
	public void clearCache(){
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingFactory#createCategory(java.lang.String)
	 */
	public boolean createCategory(String categoryName) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingFactory#getCategorys()
	 */
	public List<Category> getCategorys() {
		return categorys;
	}

	/**
	 * 取得事物的数量。
	 * 
	 * @return 事物的数量。
	 */
	public int getSize(){
		return things.size();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingFactory#getThing(java.lang.String)
	 */
	public Thing getThing(String thingPath) {
		WeakReference<Thing> wr = things.get(thingPath);
		if(wr != null){
			Thing thing = wr.get();
			if(thing == null){
				//事物已经被垃圾站回收
				things.remove(thingPath);
			}
			
			return thing;
		}else{
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingFactory#save(org.xmeta.Thing)
	 */
	public boolean save(Thing athing) {
		if(athing == null){
			return false;			
		}
		
		Thing root = athing.getRoot();
		String thingPath = root.getMetadata().getPath();
		
		WeakReference<Thing> wr = things.get(thingPath);
		if(wr == null || wr.get() == null){
			wr = new WeakReference<Thing>(root);
			things.put(thingPath, wr);
		}
		
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingFactory#remove(org.xmeta.Thing)
	 */
	public boolean remove(Thing thing) {
		if(thing == null){
			return false;			
		}
		
		Thing root = thing.getRoot();
		String thingPath = root.getMetadata().getPath();
		
		WeakReference<Thing> wr = things.remove(thingPath);
		if(wr != null){
			return true;
		}else{
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingFactory#getCategory(java.lang.String)
	 */
	public Category getCategory(String name) {
		if(name == null || "".equals(name) || name.equals("_transient")){
			return category;
		}else{
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingFactory#refresh()
	 */
	public void refresh() {		
		String[] keys = new String[things.keySet().size()];
		things.keySet().toArray(keys);
		
		for(String key : keys){
			WeakReference<Thing> wr = things.get(key);
			if(wr == null || wr.get() == null){
				things.remove(key);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingManager#remove()
	 */
	public boolean remove(){
		things.clear();
		thingIndexs.clear();
		
		return true;
	}
	
	public void removeDeadThings(){
		try{
			List<String> keys = new ArrayList<String>();
			for(String key : things.keySet()){
				keys.add(key);
			}
			
			for(String key : keys){
				WeakReference<Thing> wr = things.get(key);
				if(wr == null || wr.get() == null){
					things.remove(key);
					thingIndexs.remove(key);
				}
			}
		}catch(Exception e){
			
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingManager#getThings(java.lang.String, boolean)
	 */
	public List<Thing> getThings(String categoryName) {
		return getThings(categoryName, null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingManager#getThings(java.lang.String, java.lang.String, boolean)
	 */
	public List<Thing> getThings(String categoryName, String descriptorPath) {
		List<Thing> thins = new ArrayList<Thing>();
		
		for(Entry<String, WeakReference<Thing>> entry : things.entrySet()){
			WeakReference<Thing> wr = entry.getValue();
			if(wr != null){
				Thing thing = wr.get();

				if(thing != null && (descriptorPath == null || thing.isThing(descriptorPath))){
					thins.add(thing);
				}
			}
		}
		
		return thins;
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingManager#iterator(java.lang.String, boolean)
	 */
	public Iterator<Thing> iterator(String categoryName, boolean includeChild) {
		return getThings(categoryName).iterator();
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingManager#iterator(java.lang.String, java.lang.String, boolean)
	 */
	public Iterator<Thing> iterator(String categoryName, String descriptorPath, boolean includeChild) {
		return getThings(categoryName, descriptorPath).iterator();
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingManager#getThingIndex(java.lang.String)
	 */
	public ThingIndex getThingIndex(String thingName) {
		for(ThingIndex thingIndex : thingIndexs){
			if(thingIndex.getThingName().equals(thingName)){
				return thingIndex;
			}
		}
		
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingManager#getThingIndexs(java.lang.String, boolean)
	 */
	public List<ThingIndex> getThingIndexs(String categoryName) {
		return thingIndexs;
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingManager#getThingIndexs(java.lang.String, java.lang.String, boolean)
	 */
	public List<ThingIndex> getThingIndexs(String categoryName, String descriptorPath) {
		List<ThingIndex> indexs = new ArrayList<ThingIndex>();
		
		for(ThingIndex index : thingIndexs){
			if(index.descriptors != null && index.descriptors.indexOf(descriptorPath) != -1){
				indexs.add(index);
			}
		}
		
		return indexs;
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingManager#refresh(java.lang.String)
	 */
	public void refresh(String categoryName, boolean includeChildCategory) {
		refresh();
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingManager#removeCategory(java.lang.String)
	 */
	public boolean removeCategory(String categoryName) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingManager#addThingManagerListener(org.xmeta.ThingManager)
	 */
	public void addThingManagerListener(ThingManagerListener listener) {
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.xmeta.ThingManager#removeThingManagerListener(org.xmeta.ThingManagerListener)
	 */
	public boolean removeThingManagerListener(ThingManagerListener listener) {
		return true;
	}

	public ThingClassLoader getClassLoader(){
		return World.getInstance().getClassLoader();
	}

	@Override
	public String getClassPath() {
		return null;
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		return null;
	}

	@Override
	public URL findResource(String name) {
		return null;
	}

	@Override
	public void init(Properties properties) {
	}
}
