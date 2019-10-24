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

import org.xmeta.Index;
import org.xmeta.ThingIndex;
import org.xmeta.World;

public class ThingIndexIndex extends Index{
	List<Index> childs = null;
	Index parent = null;
	ThingIndex thing;
	
	public ThingIndexIndex(Index parent, ThingIndex thing){
		this.parent = parent;
		this.thing = thing;
		
		//refresh();
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
		if(!indexed){
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
		indexed = true;
		
		if(World.getInstance().getThing(thing.path) == null){
			return false;
		}
		
		if(childs == null){
			childs = new ArrayList<Index>();
		}
		
		return true;
	}
}