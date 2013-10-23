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
	 * @param category
	 */
	public void addCategory(Category category){
		WeakReference<Category> rf = new WeakReference<Category>(category);
		categorys.add(rf);		
	}
	
	/**
	 * 获取事物。
	 * 
	 * @param thingName
	 * @return
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