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
	 * 通过Index生成一个ID，使用这个ID可以通过getIndexById方法获取。
	 * 
	 * @param index
	 * @return
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
	 * @param id
	 * @return
	 */
	public static Index getIndexById(String id){
		Index parent = Index.getInstance();
		if(id == null || "".equals(index)){
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
				}else if(id.startsWith(childPath)){
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
	
	public long getLastModified(){
		return 0;
	}
}