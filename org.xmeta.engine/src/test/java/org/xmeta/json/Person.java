package org.xmeta.json;

import org.xmeta.ActionContext;
import org.xmeta.Thing;

public class Person {
	public static void run(ActionContext actionContext){
		Thing self = (Thing) actionContext.get("self");
		System.out.println(self.getMetadata().getName() + " is eatting");
	}
}
