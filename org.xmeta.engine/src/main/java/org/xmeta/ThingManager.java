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

import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.xmeta.util.ThingClassLoader;

/**
 * 事物管理者是对事物的二级分类，一个事物管理者一般具体提供了如何按照某种格式保存和读取事物，事物管理者包含
 * 目录和事物。
 * 
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public interface ThingManager {
	/**
	 * 添加事物管理者事件。
	 * 
	 * @param listener 事物管理者的事件
	 */
	public void addThingManagerListener(ThingManagerListener listener);
	
	/**
	 * 清空缓存。
	 *
	 */
	public void clearCache();
	
	/**
	 * 创建新的目录。
	 * 
	 * @param categoryName 目录名称
	 * 
	 * @return 已创建或已存在的目录
	 */
	public boolean createCategory(String categoryName);
	
	/**
	 * 通过指定的目录名称获取目录。
	 * 
	 * @param name 目录的名称
	 * @return 目录，如果不存在返回null
	 */
	public Category getCategory(String name);
	
	/**
	 * 返回事物管理者定义的根目录列表。
	 * 
	 * @return 事物管理者的所有目录
	 */
	public List<Category> getCategorys();
	
	/**
	 * 获得事物管理者的名称。
	 * 
	 * @return 事物管理者的名称
	 */
	public String getName();
	
	/**
	 * 通过事物的名称取得事物。
	 * 
	 * @param thingName 事物的名称，此名称为全名（包含目录）
	 * @return 事物
	 */
	public Thing getThing(String thingName);

	/**
	 * 返回指定目录下的事物索引列表。
	 * 
	 * @param categoryName 目录名称
	 * 
	 * @return 目录索引列表
	 */
	public List<ThingIndex> getThingIndexs(String categoryName);
	
	/**
	 * 根据描述者获取指定目录下的事物索引列表。
	 * 
	 * @param categoryName 目录名称
	 * @param descriptorPath 描述者路径
	 * 
	 * @return 目录索引列表
	 */
	public List<ThingIndex> getThingIndexs(String categoryName, String descriptorPath);
	
	/**
	 * 取得指定目录下的事物列表。
	 * 
	 * @param categoryName 目录名称
	 * 
	 * @return 事物列表
	 */
	public List<Thing> getThings(String categoryName);
		
	/**
	 * 根据事物描述者取得指定目录下的事物列表。
	 * 
	 * @param categoryName 目录名称
	 * @param descritporPath 事物描述者的路径
	 * 
	 * @return 事物列表
	 */
	public List<Thing> getThings(String categoryName, String descriptorPath);
	
	/**
	 *　指定目录遍历该目录下的事物。
	 *
	 * @param categoryName 目录名称
	 * @param includeChildCategory 是否包含子目录
	 * @return 事物遍历器
	 */
	public Iterator<Thing> iterator(String categoryName, boolean includeChildCategory);
	
	/**
	 * 指定事物的描述者遍历指定目录下的事物。
	 * 
	 * @param categoryName 目录名称
	 * @param descriptorPath 描述者的路径
	 * @param includeChildCategory 是否包含子目录
	 * 
	 * @return 事物遍历器
	 */
	public Iterator<Thing> iterator(String categoryName, String descriptorPath, boolean includeChildCategory);
	
	/**
	 * 刷新事物管理者。
	 *
	 */
	public void refresh();
	
	/**
	 * 刷新指定目录。
	 * 
	 * @param categoryName 目录
	 * @param includechildCategory 是否包含子目录
	 */
	public void refresh(String categoryName, boolean includeChildCategory);
	
	/**
	 * 删除此事物管理者，在此方法里实现清除此事物管理者的相关内容。
	 *
	 */
	public boolean remove();
	
	/**
	 * 从事物管理者中移除一个事物。
	 * 
	 * @param thing 要移除的事物
	 * @return 已经被移除的事物，如果没有则返回null
	 */
	public boolean remove(Thing thing);
	
	/**
	 * 删除一个目录。
	 * 
	 * @param categoryName 目录名称
	 * @return 是否删除成功
	 */
	public boolean removeCategory(String categoryName);
	
	/**
	 * 删除事物管理者的监听事件。
	 * 
	 * @param listener 事物管理者监听事件
	 */
	public boolean removeThingManagerListener(ThingManagerListener listener);
	
	/**
	 * 保存一个事物。
	 * 
	 * @param athing 要保存的事物
	 * @return 如果保存成功那么返回该事物的根父事物，否则返回null
	 */
	public boolean save(Thing athing);
	
	/** 
	 * 返回事物管理器的类装载器。
	 * 
	 * @return
	 */
	public ThingClassLoader getClassLoader();
	
	/**
	 * 获取事物管理器所依赖的类库的路径。
	 * 
	 * @return
	 */
	public String getClassPath();
	
	/**
	 * 获取资源当作输入流，如果没有返回null。
	 * 
	 * @param name
	 * @return
	 */
	public InputStream getResourceAsStream(String name);
	
	/** 
	 * 查找资源。
	 * 
	 * @param name
	 * @return
	 */
	public URL findResource(String name);
}
