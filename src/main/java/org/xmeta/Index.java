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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
	public static final String TYPE_FILE = "file";

	/** 是否已创建索引 */
	protected boolean indexed = false;
	
	/**
	 * 取实例。
	 * 
	 * @return 索引实例
	 */
	public static Index getInstance(){
		return index;
	}
	
	/**
	 * 通过Index生成一个ID，使用这个ID可以通过getIndexById方法获取。
	 * 
	 * @param index 索引
	 * @return ID
	 */
	public static String getIndexId(Index index){
		Index parent = index.getParent();
		String id = index.getType() + "|" + index.getName();
		while(parent != null){
			id = parent.getType() + "|" + parent.getName() + "." + id;
			parent = parent.getParent();
		}
		
		return id;
	}
	
	/**
	 * 通过标识获取索引。
	 * 
	 * @param id 索引ID
	 * @return 索引
	 */
	public static Index getIndexById(String id){
		Index parent = Index.getInstance();
		if(id == null || "".equals(id)){
			return parent;
		}
		
		String path = parent.getType() + "|" + parent.getName();
		while(parent  != null){
			if(id.equals(path)){
				return parent;
			}
			
			boolean have = false;
			for(Index child : parent.getChilds()){
				String childPath = path + "." + child.getType() + "|" + child.getName();
				if(id.equals(childPath)){
					return child;
				}else if(id.startsWith(childPath + ".")){
					path = childPath;
					parent = child;
					have = true;
					break;
				}
			}			
			
			if(!have){
				break;
			}
		}
		
		return null;
	}

	public static Index getIndex(String type, String path){
		return getIndex(Index.getInstance(), type, path);
	}

	public static Index getIndex(Index index, String type, String path){
		if(index == null || type == null || path == null){
			return null;
		}

		if(index.getType().equals(type) && index.getPath().equals(path)){
			return index;
		}

		for(Index child : index.getChilds()){
			Index result = getIndex(child, type, path);
			if(result != null){
				return result;
			}
		}

		return null;
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
	 * 取路径，这个路径是通过World.get(path)可以获取的那个参数路径。
	 * 
	 * @return 路径
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
	 * @return 是否刷新成功
	 */
	public abstract boolean refresh();
	
	public long getLastModified(){
		return 0;
	}
	
	public String getLastDate() {
		long time = getLastModified();
		if(time == 0) {
			return "";
		}else {
			Date date = new Date(time);
			SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return sf.format(date);
		}
	}
}