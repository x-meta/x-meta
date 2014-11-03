package org.xmeta.example;

import org.xmeta.ActionContext;
import org.xmeta.Thing;
import org.xmeta.World;

public class PersonExtendTest {
	public static void animalEat(ActionContext actionContext){
		Thing self = (Thing) actionContext.get("self");
		System.out.println(self.get("name") + " is eatting......");
	}
	
	public static void main(String args[]){
		//初始化World，World是获取事物的容器
		World world = World.getInstance();
		world.init(".");
		
		//获取PersonDescriptor.xer.xml定义的事物
		Thing person = world.getThing("org.xmeta.example.PersonExtend");		
		person.doAction("run");
		//eat是Person继承了Animal的方法
		person.doAction("eat");
	}
}
