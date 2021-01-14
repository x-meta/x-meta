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

import java.util.Iterator;
import java.util.List;

import org.xmeta.util.ThingClassLoader;

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
	 * @return 目录名
	 */
	public String getSimpleName();
	
	/**
	 * 返回文件所在的位置。
	 * 
	 * @return 文件路径
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
	 * @param includeChild 是否包含子目录
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
	 * @param descriptor 描述者
	 * @return 事物索引列表
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
	 * @param descriptor 描述者
	 * @return 事物列表
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
	
	/**
	 * 返回Category对应的ClassLoader。如果当前classLoader为null一般使用父级的classLoader。
	 * 
	 * @return
	 */
	public ThingClassLoader getClassLoader();
	
	/**
	 * 设置当前Category的类加载器。
	 * 
	 * @param classLoader
	 */
	public void setClassLoader(ThingClassLoader classLoader);
}