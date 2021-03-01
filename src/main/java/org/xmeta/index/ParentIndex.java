package org.xmeta.index;

import java.util.Collections;
import java.util.List;

import org.xmeta.Index;

public class ParentIndex extends Index{
	
	Index parent;
	
	public ParentIndex(Index parent) {
		this.parent = parent;
	}

	@Override
	public Object getIndexObject() {
		return parent;
	}

	@Override
	public Index getParent() {
		return parent;
	}

	@Override
	public List<Index> getChilds() {
		return Collections.emptyList();
	}

	@Override
	public String getDescription() {
		return parent.getDescription();
	}

	@Override
	public String getLabel() {
		return "..";
	}

	@Override
	public String getName() {
		return "..";
	}

	@Override
	public String getPath() {
		return parent.getPath();
	}

	@Override
	public String getType() {
		return "parent";
	}

	@Override
	public boolean refresh() {
		return false;
	}

}
