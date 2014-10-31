package org.xmeta.json;

import org.xmeta.Thing;
import org.xmeta.World;

public class TestAdd {
	public static void main(String args[]){
		try{
			World world = World.getInstance();
			world.init(".");
			
			Thing add = world.getThing("org.xmeta.json.Add");			
			add.doAction("run");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
