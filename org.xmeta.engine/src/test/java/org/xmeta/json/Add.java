package org.xmeta.json;

import org.xmeta.ActionContext;
import org.xmeta.Thing;

public class Add {
	public static void run(ActionContext actionContext){
		Thing self = (Thing) actionContext.get("self");
		
		System.out.println(self.getInt("x") + self.getInt("y"));
	}
}
