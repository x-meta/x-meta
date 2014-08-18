package org.xmeta.json;

import org.xmeta.Thing;
import org.xmeta.World;

public class TestJson {
	public static void main(String args[]){
		try{
			World world = World.getInstance();
			world.init(".");
			
			Thing tom = world.getThing("org.xmeta.json.Tom");
			System.out.println(tom);
			tom.doAction("eat");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
