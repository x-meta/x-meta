package org.xmeta.example;

import org.xmeta.Thing;
import org.xmeta.World;

public class PersonTest {
	public static void main(String args[]){
		//初始化World，World是获取事物的容器
		World world = World.getInstance();
		world.init(".");
		
		//获取Perxon.xer.xml定义的事物
		Thing person = world.getThing("org.xmeta.example.Person");
		
		//属性
		System.out.println("name=" + person.get("name"));
		System.out.println("age=" + person.get("age"));

		//子事物
		for(Thing child : person.getChilds()){
			System.out.println("child name=" + child.get("name"));
			System.out.println("child age=" + child.get("age"));
		}
	}
}
