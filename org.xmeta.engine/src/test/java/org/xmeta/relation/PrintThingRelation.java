package org.xmeta.relation;

import java.util.Iterator;

import org.xmeta.Thing;
import org.xmeta.ThingManager;
import org.xmeta.World;

public class PrintThingRelation {
	public static void main(String[] args){
		try{
			//World.getInstance().init("E:\\work\\xworker\\");
			World.getInstance().init("D:\\xworker-1.3.3\\xworker\\");
			
			//使用事物
			for(ThingManager thingManager : World.getInstance().getThingManagers()){
				Iterator<Thing> iter = thingManager.iterator("", true); 
				while(iter.hasNext()){
					Thing thing = iter.next();
					System.out.println(thing.getMetadata().getPath());				
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
