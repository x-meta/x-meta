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