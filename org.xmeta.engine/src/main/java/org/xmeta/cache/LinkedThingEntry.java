/*
 * Copyright 2007-2008 The X-Meta.org.
 * 
 * Licensed to the X-Meta under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The X-Meta licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
