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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmeta.Index;
import org.xmeta.Thing;
import org.xmeta.ThingManager;
import org.xmeta.World;

public class WorldIndex extends Index{
	/** 
	 * 子索引列表。
	 */
	List<Index> childs = null;
	
	/** 是否显示工作组 */
	public boolean showWorkingSet = true;
	
	public List<Index> getThingManagerIndexs(){
		List<Index> thlist = new ArrayList<Index>();
		Map<String, String> context = new HashMap<String, String>();
		getThingManagerIndexs(context, this, thlist);
		
		return thlist;
	}

	/**
	 * 获取所有事物管理器的索引列表。
	 * 
	 * @param context
	 * @param index
	 * @param list
	 */
	private void getThingManagerIndexs(Map<String, String> context, Index index, List<Index> list){
		if(Index.TYPE_THINGMANAGER.equals(index.getType())){
			if(context.get(index.getPath()) == null){
				list.add(index);
				context.put(index.getPath(), index.getPath());
			}
		}else if(Index.TYPE_WORLD.equals(index.getType())){
			for(Index child : index.getChilds()){
				getThingManagerIndexs(context, child, list);
			}
		}else if(Index.TYPE_WORKING_SET.equals(index.getType())){
			for(Index child : index.getChilds()){
				getThingManagerIndexs(context, child, list);
			}
		}else{
			return;
		}
	}
	
	public WorldIndex(){
		refresh();
	}
	
	@Override
	public Object getIndexObject() {
		return World.getInstance();
	}

	@Override
	public Index getParent() {
		return null;
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
		return "Projects";
	}

	@Override
	public String getLabel() {
		return "Project";
	}

	@Override
	public String getName() {
		return "Projects";
	}

	@Override
	public String getPath() {
		return "";
	}

	@Override
	public String getType() {
		return Index.TYPE_WORLD;
	}

	@Override
	public boolean refresh() {
		indexed = true;
		
		if(childs == null){
			childs = new ArrayList<Index>();
		}
		
		World.getInstance().refresh();
		
		//工作组的事物管理器索引
		if(showWorkingSet){
			Thing workingSet = World.getInstance().getThing("_local.xworker.worldExplorer.WorkingSet");
			if(workingSet != null){
				IndexFactory.addOrRemoveChilds(this, childs, workingSet.getChilds(), IndexFactory.workingSetIndexFactory, Index.TYPE_WORKING_SET);
			}else{
				IndexFactory.addOrRemoveChilds(this, childs, Collections.emptyList(), IndexFactory.workingSetIndexFactory, Index.TYPE_WORKING_SET);
			}
		}else{
			IndexFactory.addOrRemoveChilds(this, childs, Collections.emptyList(), IndexFactory.workingSetIndexFactory, Index.TYPE_WORKING_SET);
		}
		
		//已经在WorkingSet下的事物管理器不放到World下
		Map<String, Index> context = new HashMap<String, Index>();
		for(Index childIndex : childs){
			initWorkingSetThingManagers(childIndex, context);
		}
		
		List<ThingManager> thingManagers = new ArrayList<ThingManager>();
		for(ThingManager thingManager : World.getInstance().getThingManagers()){
			if(context.get(thingManager.getName()) == null){
				thingManagers.add(thingManager);
			}
		}
		
		//事物管理器的索引
		IndexFactory.addOrRemoveChilds(this, childs, thingManagers,	IndexFactory.thingManagerIndexFactory, Index.TYPE_THINGMANAGER);
		
		sort(childs);
		return true;
	}
	
	private void initWorkingSetThingManagers(Index workingSetIndex, Map<String, Index> thingManagers){
		if(workingSetIndex instanceof WorkingSetIndex){
			WorkingSetIndex wsetIndex = (WorkingSetIndex) workingSetIndex;
			for(Index childIndex : wsetIndex.getChilds()){
				if(childIndex instanceof ThingManagerIndex){
					thingManagers.put(childIndex.getName(), childIndex);
				}else if(childIndex instanceof WorkingSetIndex){
					initWorkingSetThingManagers(childIndex, thingManagers);
				}
			}
		}
	}
	
	public static void sort(List<Index> indexs){
		Collections.sort(indexs, new Comparator<Index>(){
			@Override
			public int compare(Index o1, Index o2) {
				if(Index.TYPE_WORKING_SET.equals(o1.getType()) && Index.TYPE_THINGMANAGER.equals(o2.getType())){
					return -1;
				}
				
				if(Index.TYPE_WORKING_SET.equals(o2.getType()) && Index.TYPE_THINGMANAGER.equals(o1.getType())){
					return 1;
				}
				
				if(Index.TYPE_CATEGORY.equals(o1.getType()) && Index.TYPE_THING.equals(o2.getType())){
					return -1;
				}
				
				if(Index.TYPE_THING.equals(o2.getType()) && Index.TYPE_CATEGORY.equals(o1.getType())){
					return 1;
				}
				
				return o1.getName().compareTo(o2.getName());
			}
			
		});
	}
	
}