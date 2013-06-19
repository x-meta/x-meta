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
