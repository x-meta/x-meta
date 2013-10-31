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