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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.xmeta.cache.ThingEntry;

/**
 * 项目、事物管理器、目录或事物的索引，用于编辑器的目录显示或事物的一般索引。
 * 
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public class Index {	
	private final static Index index = new Index();
	
	public static final String TYPE_WORLD = "world";
	public static final String TYPE_PROJECTS = "projects";
	public static final String TYPE_PLUGINS = "plugins";
	public static final String TYPE_CHILDWORLDS = "childWorlds";
	public static final String TYPE_PROJECT = "project";
	public static final String TYPE_THINGMANAGER = "thingManager";
	public static final String TYPE_CATEGORY = "category";
	public static final String TYPE_THING = "thing";

	/** 被索引的事物 */
	private Object indexObject;
	
	/** 父索引 */
	private Index parent = null;
	
	/** 子索引列表 */
	private List<Index> childs = null;
	
	/**
	 * 构造函数，创建世界的根索引。
	 *
	 */
	private Index(){
		indexObject = World.getInstance();
	}
	
	/**
	 * 取实例。
	 * 
	 * @return 索引实例
	 */
	public static Index getInstance(){
		return index;
	}
	
	/**
	 * 根据路径返回索引列表，由于事物名可以和子目录名一致，所以最多可以返回两个索引。
	 * 
	 * @param path
	 * @return
	 */
	public static List<Index> getIndex(String path){
		List<Index> indexs = new ArrayList<Index>();
		Index parent = Index.getInstance();
		while(parent != null){
			Index tmpParent = null;
			for(Index child : parent.getChilds()){
				if(child.getPath().equals(path)){
					indexs.add(child);
				}else if(path.startsWith(child.getPath())){
					tmpParent = child;
				}else if(child.getType().equals(Index.TYPE_PROJECTS)){
					tmpParent = child;
				}
			}
			
			parent = tmpParent;
		}
		
		return indexs;
	}
	
	/**
	 * 构造函数，当创建子索引时使用。
	 * @param indexObject
	 */
	private Index(Index parent, Object indexObject){
		this.parent = parent;
		this.indexObject = indexObject;
	}
	
	/**
	 * 返回被索引的事物。
	 * 
	 * @return 索引事物
	 */
	public Object getIndexObject(){
		return indexObject;
	}
	
	/**
	 * 取索引的父索引。
	 * 
	 * @return 父索引
	 */
	public Index getParent(){
		return parent;
	}
	
	/**
	 * 取子索引列表。
	 * 
	 * @return 子索引列表
	 */
	public List<Index> getChilds(){
		if(childs == null){
			refresh();
		}
		
		return childs;
	}
	
	/**
	 * 取描述。
	 * 
	 * @return 描述
	 */
	public String getDescription() {
		if(indexObject instanceof World){
			return "World";
		}else if(indexObject instanceof String){
			if(TYPE_PROJECTS.equals(indexObject)){
				return "Project";
			}else if(TYPE_PLUGINS.equals(indexObject)){
				return "Plugin";
			}else{
				return "ChildWorld";
			}
		}else if(indexObject instanceof ThingManager){
			return ((ThingManager) indexObject).getName();
		}else if(indexObject instanceof Category){
			return ((Category) indexObject).getName();
		}else if(indexObject instanceof ThingIndex){
			return ((ThingIndex) indexObject).description;
		}else if(indexObject instanceof ThingEntry){
			ThingEntry entry = (ThingEntry) indexObject;
			Thing thing = entry.getThing();
			if(thing != null){
				return thing.getMetadata().getDescription();
			}else{
				return "";
			}
		}
		
		return "错误的索引";
	} 
	
	/**
	 * 取事物的描述者，如果是事物的索引的话。
	 * 
	 * @return 事物的描述者
	 */
	public String getDescriptors() {
		if(indexObject instanceof ThingIndex){
			return ((ThingIndex) indexObject).descriptors;
		}else if(indexObject instanceof ThingEntry){
			ThingEntry entry = (ThingEntry) indexObject;
			Thing thing = entry.getThing();
			if(thing != null){
				return thing.getString(Thing.DESCRIPTORS);
			}else{
				return "";
			}
		}else{
			return "";
		}
	}
	
	/**
	 * 取标签。
	 * 
	 * @return 标签
	 */
	public String getLabel() {
		if(indexObject instanceof World){
			return "World";
		}else if(indexObject instanceof String){
			return (String) indexObject;
		}else if(indexObject instanceof ThingManager){
			return ((ThingManager) indexObject).getName();
		}else if(indexObject instanceof Category){
			return ((Category) indexObject).getSimpleName();
		}else if(indexObject instanceof ThingIndex){
			return ((ThingIndex) indexObject).label;
		}else if(indexObject instanceof ThingEntry){
			ThingEntry entry = (ThingEntry) indexObject;
			Thing thing = entry.getThing();
			if(thing != null){
				return thing.getMetadata().getLabel();
			}else{
				return "";
			}
		}
		
		return "错误的索引";
	}
	
	/**
	 * 取最后更新时间，如果是事物的索引。
	 * 
	 * @return 最后更新时间
	 */
	public long getLastModified() {
		if(indexObject instanceof ThingIndex){
			ThingIndex thingIndex = (ThingIndex) indexObject;
			return thingIndex.lastModified;
		}else if(indexObject instanceof ThingEntry){
			ThingEntry entry = (ThingEntry) indexObject;
			Thing thing = entry.getThing();
			if(thing != null){
				return thing.getMetadata().getLastModified();
			}else{
				return 0;
			}
		}
		
		return 0;
	}
	
	/**
	 * 取名称。
	 * 
	 * @return 名称
	 */
	public String getName() {
		if(indexObject instanceof World){
			return "world";
		}else if(indexObject instanceof String){
			if(TYPE_PROJECTS.equals(indexObject)){
				return "projects";
			}else if(TYPE_PLUGINS.equals(indexObject)){
				return "plug-ins";
			}else{
				return "childWorld";
			}
		}else if(indexObject instanceof ThingManager){
			return ((ThingManager) indexObject).getName();
		}else if(indexObject instanceof Category){
			return ((Category) indexObject).getSimpleName();
		}else if(indexObject instanceof ThingIndex){
			return ((ThingIndex) indexObject).getName();
		}else if(indexObject instanceof ThingEntry){
			ThingEntry entry = (ThingEntry) indexObject;
			Thing thing = entry.getThing();
			if(thing != null){
				return thing.getMetadata().getName();
			}else{
				return "";
			}
		}
		
		return "错误的索引";
	}
	
	/**
	 * 取路径。
	 * 
	 * @return
	 */
	public String getPath() {
		if(indexObject instanceof World){
			return "";
		}else if(indexObject instanceof String){
			if(TYPE_PROJECTS.equals(indexObject)){
				return "Project";
			}else if(TYPE_PLUGINS.equals(indexObject)){
				return "Plugin";
			}else{
				return "ChildWorld";
			}
		}else if(indexObject instanceof ThingManager){
			ThingManager manager = (ThingManager) indexObject;
			return manager.getName();
		}else if(indexObject instanceof Category){
			Category category = (Category) indexObject;
			return category.getName();
		}else if(indexObject instanceof ThingIndex){
			return ((ThingIndex) indexObject).path;
		}else if(indexObject instanceof ThingEntry){
			ThingEntry entry = (ThingEntry) indexObject;
			Thing thing = entry.getThing();
			if(thing != null){
				return thing.getMetadata().getPath();
			}else{
				return "";
			}
		}
		
		return "错误的索引";
	}
	
	/**
	 * 取大小，如果是事物的索引，返回事物存储文件的大小。
	 * 
	 * @return 大小
	 */
	public long getSize() {
		return 0;
	}
	
	/**
	 * 返回索引的事物的类型。
	 * 
	 * @return 索引的事物的类型
	 */
	public String getType(){
		if(indexObject instanceof World){
			return TYPE_WORLD;
		}else if(indexObject instanceof String){
			return (String) indexObject;			
		}else if(indexObject instanceof ThingManager){
			return TYPE_THINGMANAGER;
		}else if(indexObject instanceof Category){
			return TYPE_CATEGORY;
		}else if(indexObject instanceof ThingIndex){
			return TYPE_THING;
		}else if(indexObject instanceof ThingEntry){
			return TYPE_THING;
		}
		
		return "";
	}
	
	private void mergeCategory(Category category){
		List<Index> newChilds = new ArrayList<Index>();
		for(Category c : category.getCategorys()){
			boolean have = false;
			for(Index index : childs){
				if(index.getPath().equals(c.getName())){
					have = true;
					newChilds.add(index);
					break;
				}
			}
			if(!have){
				newChilds.add(new Index(this, c));
			}
		}
		for(ThingIndex t : category.getThingIndexs()){
			boolean have = false;
			for(Index index : childs){
				if(index.getPath().equals(t.path)){
					have = true;
					newChilds.add(index);
					break;
				}
			}
			if(!have){
				newChilds.add(new Index(this, t));
			}
		}
		
		childs = newChilds;
	}
	
	/**
	 * 刷新子索引。
	 *
	 */
	public void refresh(){
		if(childs == null){
			childs = new ArrayList<Index>();
		}
		
		if(indexObject instanceof World){
			//根
			if(childs.size() == 0){
				childs.add(new Index(this, "ThingManagers"));
			}
			
		}else if(indexObject instanceof String){
			//事物管理器列表
			List<ThingManager> managers = World.getInstance().getThingManagers();
			
			for(ThingManager manager : managers){
				//TransientThingManager不显示
				if(manager == World.getInstance().getTransientThingManager()){
					continue;
				}
				
				boolean have = false;
				for(Index index : childs){
					if(index.getName().equals(manager.getName())){
						have = true;
						break;
					}									
				}
				
				if(!have){
					childs.add(new Index(this, manager));
				}
			}
			
			for(Iterator<Index> iter = childs.iterator(); iter.hasNext();){
				Index index = iter.next();
				
				boolean have = false;
				for(ThingManager manager : managers){
					if(index.getName().equals(manager.getName())){
						have = true;
						break;
					}									
				}
				
				if(!have){
					iter.remove();
				}
			}
		}else if(indexObject instanceof ThingManager){
			ThingManager thingManager = (ThingManager) indexObject;
			Category root = thingManager.getCategory(null);
			root.refresh(false);
			
			mergeCategory(root);
		}else if(indexObject instanceof Category){
			Category categ = (Category) indexObject;
			categ.refresh();
			
			mergeCategory(categ);
		}else if(indexObject instanceof ThingIndex){			
			childs.clear();
			
			ThingIndex thingIndex = (ThingIndex) indexObject;
			Thing thing = World.getInstance().getThing(thingIndex.path);
			if(thing != null){
				for(Thing child : thing.getChilds()){
					childs.add(new Index(this, new ThingEntry(child.getMetadata().getPath(), child)));
				}
			}
		}else if(indexObject instanceof ThingEntry){
			childs.clear();
			
			ThingEntry entry = (ThingEntry) indexObject;
			Thing thing = World.getInstance().getThing(entry.getPath());
			if(thing != null){
				for(Thing child : thing.getChilds()){
					childs.add(new Index(this, new ThingEntry(child.getMetadata().getPath(), child)));
				}
			}
		}
		
		//排序
		if(!(indexObject instanceof World)){
			Collections.sort(childs, new Comparator<Index>(){
				public int compare(Index o1, Index o2) {
					if(o1.getType().equals(Index.TYPE_THING) && Index.TYPE_CATEGORY.equals(o2.getType())){
						return 1;
					}
					
					if(o2.getType().equals(Index.TYPE_THING) && Index.TYPE_CATEGORY.equals(o1.getType())){
						return -1;
					}
					
					return o1.getName().compareTo(o2.getName());
				}
				
			});
		}
	}
		
}
