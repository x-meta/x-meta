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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xmeta.cache.ThingEntry;
import org.xmeta.index.WorldIndex;

/**
 * 项目、事物管理器、目录或事物的索引，用于编辑器的目录显示或事物的一般索引。
 * 
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public abstract class Index {	
	private final static Index index = new WorldIndex();
	
	public static final String TYPE_WORLD = "world";
	public static final String TYPE_PROJECTS = "projects";
	public static final String TYPE_PLUGINS = "plugins";
	public static final String TYPE_CHILDWORLDS = "childWorlds";
	public static final String TYPE_PROJECT = "project";
	public static final String TYPE_THINGMANAGER = "thingManager";
	public static final String TYPE_CATEGORY = "category";
	public static final String TYPE_THING = "thing";
	public static final String TYPE_WORKING_SET = "workingSet";

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
	 * 返回被索引的事物。
	 * 
	 * @return 索引事物
	 */
	public abstract Object getIndexObject();
	
	/**
	 * 取索引的父索引。
	 * 
	 * @return 父索引
	 */
	public abstract Index getParent();
	
	/**
	 * 取子索引列表。
	 * 
	 * @return 子索引列表
	 */
	public abstract List<Index> getChilds();
	
	/**
	 * 取描述。
	 * 
	 * @return 描述
	 */
	public abstract String getDescription();
	
	
	/**
	 * 取标签。
	 * 
	 * @return 标签
	 */
	public abstract String getLabel() ;
	
		
	/**
	 * 取名称。
	 * 
	 * @return 名称
	 */
	public abstract String getName() ;
	
	/**
	 * 取路径。
	 * 
	 * @return
	 */
	public abstract String getPath() ;	
	
	/**
	 * 返回索引的事物的类型。
	 * 
	 * @return 索引的事物的类型
	 */
	public abstract String getType();
	
	
	/**
	 * 刷新子索引。
	 *
	 */
	public abstract boolean refresh();
}
