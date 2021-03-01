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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.xmeta.Index;
import org.xmeta.Thing;
import org.xmeta.ThingManager;
import org.xmeta.World;

public class WorkingSetIndex extends Index{
	String thingPath;
	Index parent = null;
	Thing thing = null;
	
	/** 
	 * 子索引列表。
	 */
	List<Index> childs = null;
	
	public WorkingSetIndex(Index parent, Thing thing){
		this.parent = parent;
		thingPath = thing.getMetadata().getPath();
		this.thing = thing;
	}
	
	@Override
	public Object getIndexObject() {
		return World.getInstance().getThing(thingPath);
	}
	
	public Thing getThing(){
		Thing t = World.getInstance().getThing(thingPath);
		if(t == null){
			return thing;
		}else{
			return t;
		}
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
		indexed = true;
		
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
		Map<String, String> context = new HashMap<String, String>();
		for(Thing t : thingManagers){
			String name = t.getString("name");
			context.put(name, name);
			ThingManager manager = world.getThingManager(name);
			if(manager != null){
				tlist.add(manager);
			}
		}
		
		//设置在ThingManager的项目里设置的工作组
		for(ThingManager tm : world.getThingManagers()) {
			Properties p = tm.getProperties();
			if(context.get(tm.getName()) == null && p != null && this.getName().equals(p.get(Index.TYPE_WORKING_SET))) {
				tlist.add(tm);
			}
		}
		IndexFactory.addOrRemoveChilds(this, childs, tlist,	IndexFactory.thingManagerIndexFactory, Index.TYPE_THINGMANAGER);
		
		for(Index child : childs){
			if(child.getType().equals(WorkingSetIndex.TYPE_WORKING_SET)){
				child.refresh();
			}
		}
		WorldIndex.sort(childs);
		return true;
	}
	
}