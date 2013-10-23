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
package org.xmeta.test;

import java.io.File;
import java.util.Iterator;

import org.xmeta.Category;
import org.xmeta.Thing;
import org.xmeta.ThingManager;
import org.xmeta.World;

public class TestThingIterator {
	
	private static void iteratorThingManager(ThingManager tm) {
		//tm.refresh();
		for (Iterator<Thing> iter = tm.iterator("", true); iter.hasNext();) {
			Thing thing = iter.next();

			String path = thing.getMetadata().getPath();
			if(path.equals("xmeta:ui:local.gef.snaptohelpers")){
				System.out.println();
			}
			System.out.println(thing.getMetadata().getPath());
		}
	}
	
	public static void findInvalidThingByName(File file){
		String fileName = file.getName();
		if(fileName.split("[.]").length > 2){
			System.out.println(file.getAbsolutePath());
			if(fileName.endsWith(".xer")){
				int index = fileName.indexOf(".");
				fileName = fileName.substring(0, index) + "$" + fileName.substring(index + 1, fileName.length());
				System.out.println("new Name: " + fileName);
				file.renameTo(new File(file.getParentFile(), fileName));
			}
		}
		
		if(file.isDirectory()){
			for(File child : file.listFiles()){
				findInvalidThingByName(child);
			}
		}
	}
	
	public static void main(String args[]){
		try{
			World.getInstance().init("D:\\xmeta\\xmeta1.2\\alpha\\");
			
			//findInvalidThingByName(new File("D:\\xmeta\\xmeta1.2\\alpha\\projects"));
			Category category = (Category) World.getInstance().get("xmeta.app.test.dataObject");
			Iterator<Thing> it = category.iterator("", true);
			while(it.hasNext()){
				Thing thing = it.next();
				System.out.println(thing.getMetadata().getPath());
			}

		}catch(Exception e){
			e.printStackTrace();
		}
		
		System.exit(0);
	}
	
}