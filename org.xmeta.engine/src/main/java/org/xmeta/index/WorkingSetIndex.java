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

import org.xmeta.Index;
import org.xmeta.Thing;
import org.xmeta.ThingManager;
import org.xmeta.World;

public class WorkingSetIndex extends Index{
	String thingPath;
	Index parent = null;
	
	/** 
	 * 子索引列表。
	 */
	List<Index> childs = null;
	
	public WorkingSetIndex(Index parent, Thing thing){
		this.parent = parent;
		thingPath = thing.getMetadata().getPath();
		
		refresh();
	}
	
	@Override
	public Object getIndexObject() {
		return World.getInstance().getThing(thingPath);
	}
	
	public Thing getThing(){
		return World.getInstance().getThing(thingPath);
	}

	@Override
	public Index getParent() {
		return parent;
	}

	@Override
	public List<Index> getChilds() {
		if(childs == null){
			refresh();
		}
		
		return childs;
	}

	@Override
	public String getDescription() {
		return getThing().getString("description");
	}

	@Override
	public String getLabel() {
		return getThing().getMetadata().getLabel();
	}

	@Override
	public String getName() {
		return getThing().getMetadata().getName();
	}

	@Override
	public String getPath() {
		return getThing().getMetadata().getPath();
	}

	@Override
	public String getType() {
		return Index.TYPE_WORKING_SET;
	}

	@Override
	public boolean refresh() {
		Thing thing = getThing();
		if(thing == null){
			return false;
		}
		
		if(childs == null){
			childs = new ArrayList<Index>();
		}
	
		World world = World.getInstance();
		
		//子WorkingSet
		List<Thing> childWorkdSets = thing.getChilds("WorkingSet");
		IndexFactory.addOrRemoveChilds(this, childs, childWorkdSets, IndexFactory.workingSetIndexFactory, Index.TYPE_WORKING_SET);
		
		//事物管理器的索引
		List<Thing> thingManagers = thing.getChilds("ThingManager");
		List<ThingManager> tlist = new ArrayList<ThingManager>();
		for(Thing t : thingManagers){
			ThingManager manager = world.getThingManager(t.getString("name"));
			if(manager != null){
				tlist.add(manager);
			}
		}
		IndexFactory.addOrRemoveChilds(this, childs, tlist,	IndexFactory.thingManagerIndexFactory, Index.TYPE_THINGMANAGER);
		
		WorldIndex.sort(childs);
		return true;
	}
	
}