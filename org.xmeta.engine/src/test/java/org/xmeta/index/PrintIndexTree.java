package org.xmeta.index;

import org.xmeta.Index;
import org.xmeta.World;

public class PrintIndexTree {
	public static void printIndex(Index index, String parent){
		String myPath = null;
		if(parent != null){
			myPath = parent + "->" + index.getLabel();
		}else{
			myPath = index.getName();
		}
		System.out.println(myPath);
		
		for(Index child : index.getChilds()){
			printIndex(child, myPath);
		}
	}
	
	public static void main(String args[]){
		try{
			World world = World.getInstance();			
			world.init("E:\\git\\xworker\\xworker\\");
			
			WorldIndex index = new WorldIndex();
			printIndex(index, null);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
