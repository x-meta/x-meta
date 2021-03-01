package org.xmeta.util;

import java.util.Map;

import org.xmeta.Action;
import org.xmeta.ActionContext;
import org.xmeta.Thing;

public class ThingRunnable implements Runnable{
	Thing thing;
	ActionContext actionContext;
	Map<String, Object> params;
	
	public ThingRunnable(Thing thing, ActionContext actionContext, Map<String, Object> params){
		this.thing = thing;
		this.actionContext = actionContext;
		this.params = params;
	}
	
	public void run(){
		Action action = thing.getAction();
		if(params != null){
			action.run(actionContext, params);
		}else{
			action.run(actionContext);
		}
	}
}
