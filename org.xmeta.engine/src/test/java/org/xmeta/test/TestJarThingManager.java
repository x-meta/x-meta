package org.xmeta.test;

import java.io.File;
import java.util.Iterator;

import org.xmeta.Thing;
import org.xmeta.World;
import org.xmeta.thingManagers.JarThingManager;

public class TestJarThingManager {
	public static void main(String[] args) {
		try {
			World world = World.getInstance();
			world.init("d:/temp");
			
			JarThingManager thingManager = new JarThingManager("xworker_core", new File("d:/xworker/lib/xworker_core-1.4.3-SNAPSHOT.jar"));
			world.addThingManager(thingManager);
			Iterator<Thing> iter = thingManager.iterator(null, true);
			while(iter.hasNext()){
			    Thing thing = iter.next();
			    System.out.println(thing);
			}  
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
