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
package org.xmeta.tools;

import java.util.Iterator;

import org.xmeta.Thing;
import org.xmeta.ThingManager;
import org.xmeta.World;

public class SearchAllThings {
	
	
	public static void main(String args[]){
		try{
			//World.getInstance().init("E:\\work\\xmeta_alpha\\");
			World.getInstance().init("D:\\xmeta\\xmeta1.2\\alpha\\");
			
			for(ThingManager thingManager : World.getInstance().getThingManagers()){
				Iterator<Thing> iterator = thingManager.iterator("", true);
				while(iterator.hasNext()){
					Thing thing = iterator.next();
					searchExtends(thing);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void searchExtends(Thing thing){
		String value = thing.getString("descriptions");
		if(value != null && value.indexOf(",") != -1){
			System.out.println(value);
		}
		
		for(Thing child : thing.getChilds()){
			searchExtends(child);
		}
	}
	
	public static void search(Thing thing, String attributeName, String searchFor){
		for(String key : thing.getAttributes().keySet()){
			String value = thing.getString(key);
			if(value != null && value.indexOf(searchFor) != -1){
				System.out.println(value);
			}
		}
		
		for(Thing child : thing.getChilds()){
			search(child, attributeName, searchFor);
		}
	}
}	