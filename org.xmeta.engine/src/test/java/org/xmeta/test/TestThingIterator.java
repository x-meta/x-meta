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
