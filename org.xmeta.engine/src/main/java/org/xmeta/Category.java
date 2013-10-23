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

import java.util.Iterator;
import java.util.List;

/**
 * 目录是包含在事物管理者下的，用来对事物管理者中的事物进行分类。
 * 
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public interface Category {
	/**
	 * 通过名称返回指定的下级目录。
	 * 
	 * @param name 下级目录的名称
	 * @return 下级目录，如果不存在返回null
	 */
	public Category getCategory(String name);
	
	/** 
	 * 获取下一级的目录的列表。
	 * 
	 * @return 下一级目录的列表
	 */
	public List<Category> getCategorys();
	
	/**
	 * 返回当前目录的名称。
	 * 
	 * @return 目录的名称
	 */
	public String getName();
	
	/**
	 * 返回目录的简写。
	 * 
	 * @return
	 */
	public String getSimpleName();
	
	/**
	 * 返回文件所在的位置。
	 * 
	 * @return
	 */
	public String getFilePath();
	
	/**
	 * 返回上级目录。
	 * 
	 * @return 上级目录，如果不存在返回null
	 */
	public Category getParent();
	
	/**
	 * 刷新。
	 */
	public void refresh();
	
	/**
	 * 刷新。
	 * 
	 * @param includeChild
	 */
	public void refresh(boolean includeChild);
	
	/**
	 * 通过事物的名称获得当前目录下的事物。
	 * 
	 * @param name 事物名称
	 * @return 事物，如果不存在返回null
	 */
	public Thing getThing(String name);
	
	/**
	 * 返回此目录所属的事物管理者。
	 * 
	 * @return 事物管理者
	 */
	public ThingManager getThingManager();
	
	/**
	 * 返回当前目录下的所有事物的索引列表，不包含子目录。
	 * 
	 * @return 事物的索引列表
	 */
	public List<ThingIndex> getThingIndexs();
	
	/**
	 * 根据描述返回事物索引列表。
	 * 
	 * @param descriptor
	 * @return
	 */
	public List<ThingIndex> getThingIndexs(String descriptor);
	
	/**
	 * 获得当前目录下的事物列表。
	 * 
	 * 取具体事物是请用World，World负责缓存。
	 * 
	 * @return 事物列表
	 */
	public List<Thing> getThings();
	
	/**
	 * 返回指定事物的列表。
	 * 
	 * 取具体事物是请用World，World负责缓存。
	 * 
	 * @param descriptor
	 * @return
	 */
	public List<Thing> getThings(String descriptor);
	
	/**
	 * 遍历当前目录下的所有事物。
	 * 
	 * 取具体事物是请用World，World负责缓存。
	 * 
	 * @param includeChildCategory 是否包含子目录
	 * @return 事物遍历器
	 */
	public Iterator<Thing> iterator(boolean includeChildCategory);
	
	/**
	 * 通过指定的描述者名称遍历当前目录下的所有事物。
	 * 
	 * 取具体事物是请用World，World负责缓存。
	 * 
	 * @param descriptorPath 描述者的路径
	 * @param includeChildCategory 是否包含子目录
	 * @return 事物遍历器
	 */
	public Iterator<Thing> iterator(String descriptorPath, boolean includeChildCategory);
}