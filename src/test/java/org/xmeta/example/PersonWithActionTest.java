package org.xmeta.example;

import org.xmeta.Action;
import org.xmeta.ActionContext;
import org.xmeta.Thing;
import org.xmeta.World;

public class PersonWithActionTest {
	/**
	 * 事物的行为实现。
	 * 
	 * @param actionContext
	 */
	public static void run(ActionContext actionContext){
		Thing self = (Thing) actionContext.get("self");
		
		//属性
		System.out.println("name=" + self.get("name"));
		System.out.println("age=" + self.get("age"));

		//子事物
		for(Thing child : self.getChilds()){
			System.out.println("child name=" + child.get("name"));
			System.out.println("child age=" + child.get("age"));
		}
	}
	
	public static void main(String args[]){
		//初始化World，World是获取事物的容器
		World world = World.getInstance();
		world.init(".");
		
		//获取Perxon.xer.xml定义的事物
		Thing person = world.getThing("org.xmeta.example.PersonWithAction");
		person.doAction("run");
		
		//Person也可以转化为动作执行，不过这里执行会出现异常，因此action直接执行是不包含self变量
		Action action = person.getAction();
		System.out.println("ActionContext has not self, will throw a exception!!");
		action.run();
	}
}
