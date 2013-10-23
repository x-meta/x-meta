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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xmeta.Thing;

/**
 * 弱引用事物实体列表。<p/>
 * 
 * 被引用的事物是链表中的最后一个事物，当前面的事物发生了改变时后面的事物引用就无效，此时
 * 引用返回null。<p/>
 * 
 * 此类一般用于事物的动作缓存等，当事物发生变更时，通常动作等需要重新组织。
 * 
 * @author zyx
 *
 */
public class LinkedThingEntry {
	protected List<ThingEntry> thingEntrys = new ArrayList<ThingEntry>();
	
	public void addThing(Thing thing){
		thingEntrys.add(new ThingEntry(thing));
	}
	
	public void removeLast(){
		if(thingEntrys.size() > 0){
			thingEntrys.remove(thingEntrys.size() - 1);
		}
	}
	
	public Thing getThing(){
		for(Iterator<ThingEntry> iter = thingEntrys.iterator(); iter.hasNext();){
			ThingEntry thingEntry = iter.next();
			
			if(iter.hasNext() && thingEntry.isChanged()){
				return null;
			}
			
			if(!iter.hasNext()){
				return thingEntry.getThing();
			}			
		}
		
		return null;
	}
}