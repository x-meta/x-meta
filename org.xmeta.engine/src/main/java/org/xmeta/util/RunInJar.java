package org.xmeta.util;

import org.xmeta.World;

/**
 * 在Jar中运行事物的工具。
 * 
 * @author zyx
 *
 */
public class RunInJar {
	
	public static void main(String[] args){
		try{
			World world = World.getInstance();
			world.init(null);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
