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
}