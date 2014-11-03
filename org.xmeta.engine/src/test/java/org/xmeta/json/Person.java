package org.xmeta.json;

import org.xmeta.ActionContext;
import org.xmeta.Thing;

public class Person {
	public static void run(ActionContext actionContext){
		Thing self = (Thing) actionContext.get("self");
		
		System.out.println("name=" + self.get("name"));
		System.out.println("age=" + self.get("age"));

		//子事物
		for(Thing child : self.getChilds()){
			System.out.println("child name=" + child.get("name"));
			System.out.println("child age=" + child.get("age"));
		}
	}
}
