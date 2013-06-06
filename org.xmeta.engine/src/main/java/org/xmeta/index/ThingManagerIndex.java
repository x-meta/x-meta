package org.xmeta.index;

import java.util.ArrayList;
import java.util.List;

import org.xmeta.Index;
import org.xmeta.ThingManager;

public class ThingManagerIndex extends Index{
	List<Index> childs = new ArrayList<Index>();
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
		// TODO Auto-generated method stub
		return super.refresh();
	}

}
