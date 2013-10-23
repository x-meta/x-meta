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