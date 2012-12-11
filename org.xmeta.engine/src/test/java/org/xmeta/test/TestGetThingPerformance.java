package org.xmeta.test;

import org.xmeta.Thing;
import org.xmeta.World;

public class TestGetThingPerformance {
	public static void main(String args[]){
		try{
			World world = World.getInstance();
			world.init("D:\\xmeta\\xmeta1.2\\alpha\\");
			int count = 100000;
			Thing thing = world.getThing("xmeta:core:test.things.lang.actions.TestPerformance:/@actions/@java");
			long start = System.currentTimeMillis();
	        for(int i=0; i<count; i++){
	            thing = world.getThing("xmeta:core:test.things.lang.actions.TestPerformance:/@actions/@java");
	        }
	        System.out.println("get Thing time : " + (System.currentTimeMillis() - start)); 
	        
	        System.exit(0);	        
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
