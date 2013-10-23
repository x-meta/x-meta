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

import java.util.ArrayList;
import java.util.List;

import org.xmeta.Category;
import org.xmeta.Index;
import org.xmeta.ThingManager;
import org.xmeta.World;

public class ThingManagerIndex extends Index{
	List<Index> childs = null;
	Index parent = null;
	ThingManager thingManager = null;
	
	public ThingManagerIndex(Index parent, ThingManager thingManager){
		this.parent = parent;
		this.thingManager = thingManager;
		
		refresh();
	}
	
	@Override
	public Object getIndexObject() {
		return thingManager;
	}

	@Override
	public Index getParent() {
		return parent;
	}

	@Override
	public List<Index> getChilds() {
		if(childs ==  null){
			childs = new ArrayList<Index>();
		}
		return childs;
	}

	@Override
	public String getDescription() {
		return thingManager.getName();
	}

	@Override
	public String getLabel() {
		return thingManager.getName();
	}

	@Override
	public String getName() {
		return thingManager.getName();
	}

	@Override
	public String getPath() {
		return thingManager.getName() ;
	}

	@Override
	public String getType() {
		return Index.TYPE_THINGMANAGER;
	}

	@Override
	public boolean refresh() {
		if(World.getInstance().getThingManager(thingManager.getName()) == null){
			return false;
		}
		
		if(childs == null){
			childs = new ArrayList<Index>();
		}
				
		thingManager.refresh();
		List<Category> categorys = new ArrayList<Category>();
		for(Category category : thingManager.getCategorys()){
			if(category.getName() != null && !"".equals(category.getName())){
				categorys.add(category);
			}
		}
		
		IndexFactory.addOrRemoveChilds(this, childs, categorys,	IndexFactory.categoryIndexFactory, Index.TYPE_CATEGORY);
		
		//事物索引
		IndexFactory.addOrRemoveChilds(this, childs, thingManager.getCategory("").getThingIndexs(),	IndexFactory.thingIndexFactory, Index.TYPE_THING);		
		
		WorldIndex.sort(childs);
		
		return true;
	}

}