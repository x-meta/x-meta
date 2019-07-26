package org.xmeta.test;

import java.io.File;

import org.xmeta.Project;
import org.xmeta.World;

public class TestProject {
	public static void main(String[] args) {
		try {
			World world = World.getInstance();
			world.init(null);
			
			Project project1 = new Project(new File("d:\\test\\helloworld\\"));
			Project project2 = new Project(new File("d:\\test\\helloworld2\\"));
			
			Object world1 = project1.getWorld();
			Object world2 = project2.getWorld();
			
			System.out.println("Is world equals: " + (world1 == world2));
			
			Object thing1 = project1.getThing("SimpleThingEditor");
			Object thing2 = project2.getThing("SimpleThingEditor");
			System.out.println("Is thing equals: " + (thing1 == thing2));
			System.out.println("thing1=" + thing1);
			System.out.println("thing2=" + thing2);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
