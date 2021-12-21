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
package org.xmeta.cache;

import java.lang.ref.WeakReference;

import org.xmeta.Thing;
import org.xmeta.World;

/**
 * 用于引用事物，当事物变更时总能获取最新的事物。
 * 
 * @author zyx
 *
 */
public class ThingEntry {
	protected String path;
	protected long lastmodified;
	protected WeakReference<Thing> thingReference = null;	
	protected Object data = null;
	
	public ThingEntry(Thing thing){
		this(thing.getMetadata().getPath(), thing);
	}
	
	public ThingEntry(String path, Thing thing){
		this.path = path;
		this.lastmodified = thing.getMetadata().getLastModified();
		thingReference = new WeakReference<Thing>(thing);
	}
	
	public String getPath(){
		return path;
	}
	
	public Thing getThing(){
		Thing thing = thingReference.get();
		if(thing != null && thing.isTransient()){
			return thing;
		}

		if(thing != null && !thing.getMetadata().isRemoved() && lastmodified == thing.getMetadata().getLastModified()){
			return thing;
		}else{
			thing = World.getInstance().getThing(path);
			if(thing != null){
				lastmodified = thing.getMetadata().getLastModified();
				thingReference = new WeakReference<Thing>(thing);
			}
			
			return thing;
		}
	}
	
	public boolean isChanged(){
		Thing thing = thingReference.get();
		if(thing != null && !thing.getMetadata().isRemoved() && lastmodified == thing.getMetadata().getLastModified()){
			return false;
		}else{
			return true;
		}
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}