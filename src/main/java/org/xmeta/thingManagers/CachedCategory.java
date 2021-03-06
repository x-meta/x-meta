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
import java.util.Stack;

import org.xmeta.Category;
import org.xmeta.Thing;
import org.xmeta.ThingIndex;
import org.xmeta.ThingManager;
import org.xmeta.World;
import org.xmeta.XMetaException;
import org.xmeta.util.ThingClassLoader;

/**
 * 子包和事物索引都预先读取并缓存的包。
 * 
 * @author zhangyuxiang
 *
 */
public abstract class CachedCategory implements Category{
	/** 子包列表 */
	protected List<Category> childCategorys = new ArrayList<Category>();
	
	/** 事物索引列表 */
	protected List<ThingIndex> thingIndexs = new ArrayList<ThingIndex>();
	
	/** 父包 */
	protected Category parent;
	
	/** 包名称 */
	protected String name;
	
	/** 事物管理器 */
	protected ThingManager thingManager;
	
	/** 是否已经刷新过 */
	protected boolean refreshed = false;
	
	protected ThingClassLoader classLoader = null;
	
	public CachedCategory(ThingManager thingManager, Category parent, String name){
		this.thingManager = thingManager;
		this.parent = parent;
		this.name = name;
	}
	
	private void checkRefresh(){
		if(!refreshed){
			refreshed = true;
			refresh(false);
		}
	}
	
	@Override
	public Category getCategory(String name) {
		checkRefresh();
		
		for(Category child : childCategorys){
			if(child.getSimpleName().equals(name)){
				return child;
			}
		}
		
		return null;
	}
	
	@Override
	public List<Category> getCategorys() {
		checkRefresh();
		
		return childCategorys;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Category getParent() {
		return parent;
	}

	@Override
	public String getSimpleName() {
		if(name == null){
			return null;
		}
		int index = name.lastIndexOf(".");
		if(index == -1){
			return name;
		}else{
			return name.substring(index + 1, name.length());
		}
	}

	@Override
	public Thing getThing(String thingName) {	
		checkRefresh();
		
		if(name != null && !name.equals("")){
			//改为从world获取事物
			return World.getInstance().getThing(name + "." + thingName);
			//return thingManager.getThing();
		}else{
			return World.getInstance().getThing(thingName);
			//return thingManager.getThing(thingName);
		}
	}
	
	@Override
	public List<ThingIndex> getThingIndexs() {
		checkRefresh();
		
		return thingIndexs;
	}

	@Override
	public List<ThingIndex> getThingIndexs(String descriptor) {
		checkRefresh();
		
		if(descriptor == null || "".equals(descriptor)){
			return thingIndexs;
		}
		
		List<ThingIndex> indexs = new ArrayList<ThingIndex>();
		for(ThingIndex index : thingIndexs){
			if(index.descriptors != null){
				for(String desc : index.descriptors.split("[,]")){
					if(desc.equals(descriptor)){
						indexs.add(index);
						break;
					}
				}
			}
		}
		
		return indexs;
	}

	@Override
	public ThingManager getThingManager() {
		return thingManager;
	}

	@Override
	public List<Thing> getThings() {
		checkRefresh();
		
		List<Thing> things = new ArrayList<Thing>();
		for(ThingIndex index : thingIndexs){
			//通过World获取事物，zhangyuxiang 2013-03-15
			Thing thing = World.getInstance().getThing(index.path);
			if(thing != null){
				things.add(thing);
			}
			
		}
		return things;
	}

	@Override
	public List<Thing> getThings(String descriptor) {
		checkRefresh();
		
		List<Thing> things = new ArrayList<Thing>();
		
		for(ThingIndex index : thingIndexs){
			if(index.descriptors != null){
				for(String desc : index.descriptors.split("[,]")){
					if(desc.equals(descriptor)){
						//通过World获取事物，zhangyuxiang 2013-03-15
						Thing thing = World.getInstance().getThing(index.path);
						
						if(thing != null){
							things.add(thing);
						}
						break;
					}
				}
			}
		}
		
		return things;
	}

	@Override
	public Iterator<Thing> iterator(boolean includeChildCategory) {
		return iterator(null, includeChildCategory);
	}

	@Override
	public Iterator<Thing> iterator(final String descriptorPath, final boolean includeChildCategory) {
		checkRefresh();
		
		final Category category = this;
		return new Iterator<Thing>(){
			Thing current;
			Stack<IteratorStackEntry> stacks = new Stack<IteratorStackEntry>();			
			{
				IteratorStackEntry root = new IteratorStackEntry();
				root.category = category;
				root.thingIndex = 0;
				root.categoryIndex = 0;
				stacks.push(root);
			}
			
			/**
			 * 初始化下一个事物。
			 */
			private void initNextThing(){
				//如果当前的current为null表示下一个next未初始化
				//if(current == null){
				//	if(stacks.size() == 0){
				//		return;
				//	}
				while(stacks.size() > 0 && current == null) {
					
					IteratorStackEntry entry = stacks.peek();
					
					//先遍历事物				
					List<ThingIndex> thingIndexs = entry.thingIndexs;
					if(thingIndexs == null){
						thingIndexs = entry.category.getThingIndexs(descriptorPath);
						entry.thingIndexs = thingIndexs;
					}
					if(entry.thingIndex < thingIndexs.size()){
						//所有的事物都应该通过, 修改从world获取事物，2013-03-15
						current = World.getInstance().getThing(thingIndexs.get(entry.thingIndex).path); 
							//entry.category.getThing(thingIndexs.get(entry.thingIndex).getName());
						entry.thingIndex ++;
						return;
					}else{
						//遍历目录
						List<Category> categorys = entry.categorys;
						if(categorys == null){
							categorys = entry.category.getCategorys();
							entry.categorys = categorys;
						}
						if(entry.categoryIndex < categorys.size()){
							//如果还有子目录，下压一个栈
							Category category = categorys.get(entry.categoryIndex);
							IteratorStackEntry aentry = new IteratorStackEntry();
							aentry.category = category;
							aentry.thingIndex = 0;
							aentry.categoryIndex = 0;
							entry.categoryIndex ++;
							stacks.push(aentry);
							//initNextThing();
						}else{
							stacks.pop();
							//initNextThing();
						}
					}
					
				}
			}
			
			public boolean hasNext() {
				if(current != null){
					return true;
				}else{
					initNextThing();
					return current != null;
				}
			}

			public Thing next() {
				if(current == null){
					initNextThing();
				}
				
				Thing temp = current;
				current = null;
				return temp;
			}

			public void remove() {		
				throw new XMetaException("not supported");
			}			
		};
	}
	
	/**
	 * 添加子包。
	 * 
	 * @param category 包
	 */
	public void addCategory(Category category){
		childCategorys.add(category);
	}
	
	/**
	 * 添加事物索引。
	 * 
	 * @param thingIndex 事物索引
	 */
	public void addThingIndex(ThingIndex thingIndex){
		thingIndexs.add(thingIndex);
	}
	
	@Override
	public ThingClassLoader getClassLoader() {
		if(classLoader == null) {
			Category parent = getParent();
			if(parent == null) {
				if(thingManager != null) {
					return thingManager.getClassLoader();
				}else {
					return World.getInstance().getClassLoader();
				}
			}else {
				return parent.getClassLoader();
			}
		}else {
			return classLoader;
		}
	}

	@Override
	public void setClassLoader(ThingClassLoader classLoader) {
		this.classLoader = classLoader;
	}
}