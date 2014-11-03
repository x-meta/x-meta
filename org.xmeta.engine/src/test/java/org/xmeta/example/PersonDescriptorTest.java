package org.xmeta.example;

import org.xmeta.Thing;
import org.xmeta.World;

public class PersonDescriptorTest {
	public static void main(String args[]){
		//初始化World，World是获取事物的容器
		World world = World.getInstance();
		world.init(".");
		
		//获取PersonDescriptor.xer.xml定义的事物
		Thing person = world.getThing("org.xmeta.example.PersonDescriptor");		
		person.doAction("run");
	}
}
