package org.xmeta.test;

import org.xmeta.Index;
import org.xmeta.World;

public class TestIndex {
	public static void main(String args[]){
		try{
			World.getInstance().init("D:\\xmeta\\xmeta1.2\\alpha\\");
			for(Index index : Index.getIndex("test:test:test")){
				System.out.println(index.getType() + "   " + index.getPath());
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		System.exit(0);
	}
}
