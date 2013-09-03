package org.xmeta.index;

import java.util.ArrayList;
import java.util.List;

import org.xmeta.Category;
import org.xmeta.Index;
import org.xmeta.Thing;
import org.xmeta.ThingIndex;
import org.xmeta.ThingManager;
import org.xmeta.World;

public class ThingIndexIndex extends Index{
	List<Index> childs = null;
	Index parent = null;
	ThingIndex thing;
	
	public ThingIndexIndex(Index parent, ThingIndex thing){
		this.parent = parent;
		this.thing = thing;
		
		refresh();
	}
	
	@Override
	public Object getIndexObject() {
		return thing;
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
		return thing.description;
	}

	@Override
	public String getLabel() {
		if(thing.label == null || "".equals(thing.label)){
			return thing.name;
		}else{
			return thing.label;
		}
	}

	@Override
	public String getName() {
		return thing.name;
	}

	@Override
	public String getPath() {
		return thing.path;
	}

	@Override
	public String getType() {
		return Index.TYPE_THING;
	}

	@Override
	public boolean refresh() {
		if(World.getInstance().getThing(thing.path) == null){
			return false;
		}
		
		if(childs == null){
			childs = new ArrayList<Index>();
		}
		
		return true;
	}
}
