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
		
		//refresh();
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
		if(!indexed){
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
	public synchronized boolean refresh() {
		indexed = true;
		
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