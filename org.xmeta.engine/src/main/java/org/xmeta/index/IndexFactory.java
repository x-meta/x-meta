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
package org.xmeta.index;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmeta.Category;
import org.xmeta.Index;
import org.xmeta.Thing;
import org.xmeta.ThingIndex;
import org.xmeta.ThingManager;

public abstract class IndexFactory {
	public abstract Index createIndex(Index parent, Object obj);
	
	public abstract String getName(Object obj);
	
	public static IndexFactory thingIndexFactory = new IndexFactory(){
		@Override
		public Index createIndex(Index parent, Object obj) {
			return new ThingIndexIndex(parent, (ThingIndex) obj);
		}

		@Override
		public String getName(Object obj) {
			return ((ThingIndex) obj).name;
		}
	};
	
	public static IndexFactory categoryIndexFactory = new IndexFactory(){
		@Override
		public Index createIndex(Index parent, Object obj) {
			return new CategoryIndex(parent, (Category) obj);
		}

		@Override
		public String getName(Object obj) {
			return ((Category) obj).getSimpleName();
		}
	};
	
	public static IndexFactory workingSetIndexFactory = new IndexFactory(){
		@Override
		public Index createIndex(Index parent, Object obj) {
			return new WorkingSetIndex(parent, (Thing) obj);
		}

		@Override
		public String getName(Object obj) {
			return ((Thing) obj).getMetadata().getName();
		}
	};
	
	public static IndexFactory thingManagerIndexFactory = new IndexFactory(){
		@Override
		public Index createIndex(Index parent,Object obj) {
			return new ThingManagerIndex(parent, (ThingManager) obj);
		}

		@Override
		public String getName(Object obj) {
			return ((ThingManager) obj).getName();
		}
	};
		
	@SuppressWarnings("rawtypes")
	public static void addOrRemoveChilds(Index parent, List<Index> childs, List indexObjects, IndexFactory indexFactory, String type){
		//添加新的索引
		Map<String, Index> existsMap = new HashMap<String, Index>();
		for(Index child : childs){
			existsMap.put(child.getName(), child);
		}
		
		Map<String, Index> context = new HashMap<String, Index>();
		for(Object obj : indexObjects){
			Index index = existsMap.get(indexFactory.getName(obj));
			if(index == null){
				index = indexFactory.createIndex(parent, obj);
				childs.add(index);
			}
			context.put(indexFactory.getName(obj), index);
		}
		
		//删除不存在的索引
		for(int i=0; i<childs.size(); i++){
			Index child = childs.get(i);
			if(type.equals(child.getType())){
				if(context.get(child.getName()) == null){
					childs.remove(i);
					i--;
				}
			}
		}
	}
}