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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 装载一个事物时有可能会遍历所有的事物管理器，把Category缓存起来可以减少遍历的次数。
 * 
 * @author zhangyuxiang
 *
 */
public class CategoryCache {
	List<WeakReference<Category>> categorys = new ArrayList<WeakReference<Category>>();
	
	/**
	 * 添加一个目录。
	 * 
	 * @param category 目录
	 */
	public void addCategory(Category category){
		WeakReference<Category> rf = new WeakReference<Category>(category);
		categorys.add(rf);		
	}
	
	public Category getCategory(){
		for(int i=0; i<categorys.size(); i++){
			WeakReference<Category> rf = categorys.get(i);
			Category category = rf.get();
			if(category == null){
				categorys.remove(i);
				i--;
			}else{
				return category;
			}
		}
		
		return null;
	}
	/**
	 * 获取事物。
	 * 
	 * @param thingName 事物名
	 * @return 事物
	 */
	public synchronized Thing getThing(String thingName){
		for(int i=0; i<categorys.size(); i++){
			WeakReference<Category> rf = categorys.get(i);
			Category category = rf.get();
			if(category == null){
				categorys.remove(i);
				i--;
			}
			
			if(category == null){
				return null;
			}
			ThingManager thingManager = category.getThingManager();			
			Thing thing = thingManager.getThing(thingName);
			if(thing != null){
				return thing;
			}
		}
		
		return null;
	}
}