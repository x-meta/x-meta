package org.xmeta.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmeta.Index;
import org.xmeta.Thing;
import org.xmeta.World;

public class WorldIndex extends Index{
	/** 
	 * 子索引列表。
	 */
	List<Index> childs = null;
	
	/**
	 * 事物管理器的缓存索引
	 */
	Map<String, ThingManagerIndex> thingManagerIndexs = new HashMap<String, ThingManagerIndex>();
	
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
		if(childs == null){
			refresh();
		}
		
		return childs;
	}

	@Override
	public String getDescription() {
		return "World";
	}

	@Override
	public String getLabel() {
		return "World";
	}

	@Override
	public String getName() {
		return "World";
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
		if(childs == null){
			childs = new ArrayList<Index>();
		}
		
		//事物管理器的索引
		IndexFactory.addOrRemoveChilds(this, childs, World.getInstance().getThingManagers(),
				IndexFactory.thingManagerIndexFactory, Index.TYPE_THINGMANAGER);
		
		//工作组的事物管理器索引
		Thing workingSet = World.getInstance().getThing("_local.xworker.worldExplorer.WorkingSet");
		if(workingSet != null){
			IndexFactory.addOrRemoveChilds(this, childs, workingSet.getChilds(),
					IndexFactory.workingSetIndexFactory, Index.TYPE_WORKING_SET);
		}
		
		sort(childs);
		return true;
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
				
				return o1.getName().compareTo(o2.getName());
			}
			
		});
	}
	
}
