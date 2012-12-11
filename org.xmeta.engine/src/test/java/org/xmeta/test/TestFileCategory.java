package org.xmeta.test;

import java.util.Iterator;

import org.xmeta.Category;
import org.xmeta.Thing;
import org.xmeta.World;

public class TestFileCategory {
	public static void main(String args[]){
		try{
			World world = World.getInstance();
			world.init("D:\\xmeta\\xmeta1.2\\alpha\\");
			Category c = (Category) world.get("xmeta:ui:worldExplorer");
			Iterator<Thing> iter = c.iterator(true);
			while(iter.hasNext()){
				System.out.println(iter.next().getMetadata().getPath());
			}
			System.exit(0);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
