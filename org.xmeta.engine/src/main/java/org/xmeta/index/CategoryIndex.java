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

public class CategoryIndex extends Index{
	List<Index> childs = null;
	Index parent = null;
	Category category = null;
	
	public CategoryIndex(Index parent, Category category){
		this.parent = parent;
		this.category = category;
		
		refresh();
	}
	
	@Override
	public Object getIndexObject() {
		return category;
	}

	@Override
	public Index getParent() {
		return parent;
	}

	@Override
	public List<Index> getChilds() {
		if(childs ==  null){
			refresh();
		}
		
		return childs;
	}

	@Override
	public String getDescription() {
		return category.getName();
	}

	@Override
	public String getLabel() {
		return category.getSimpleName();
	}

	@Override
	public String getName() {
		return category.getSimpleName();
	}

	@Override
	public String getPath() {
		return category.getName() ;
	}

	@Override
	public String getType() {
		return Index.TYPE_CATEGORY;
	}

	@Override
	public boolean refresh() {
		//先判断Category是否还有效
		ThingManager thingManager = World.getInstance().getThingManager(category.getThingManager().getName());
		if(thingManager == null){
			return false;
		}
		
		if(thingManager.getCategory(category.getName()) == null){
			return false;
		}
		
		if(childs == null){
			childs = new ArrayList<Index>();
		}
		//刷新下级节点
		List<Category> categorys = new ArrayList<Category>();
		category.refresh();
		for(Category cat : category.getCategorys()){
			if(cat.getName() != null && !"".equals(cat.getName())){
				categorys.add(cat);
			}
		}
		
		IndexFactory.addOrRemoveChilds(this, childs, categorys,	IndexFactory.categoryIndexFactory, Index.TYPE_CATEGORY);
		
		//事物索引
		IndexFactory.addOrRemoveChilds(this, childs, category.getThingIndexs(),	IndexFactory.thingIndexFactory, Index.TYPE_THING);		
		
		WorldIndex.sort(childs);
		
		return true;
	}

}