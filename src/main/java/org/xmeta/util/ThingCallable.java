package org.xmeta.util;

import java.util.Map;
import java.util.concurrent.Callable;

import org.xmeta.Action;
import org.xmeta.ActionContext;
import org.xmeta.Thing;

public class ThingCallable implements Callable<Object>{
	Thing thing;
	ActionContext actionContext;
	Map<String, Object> params;
	
	public ThingCallable(Thing thing, ActionContext actionContext, Map<String, Object> params){
		this.thing = thing;
		this.actionContext = actionContext;
		this.params = params;
	}
	
	public Object call(){
		Action action = thing.getAction();
		if(params != null){
			return action.run(actionContext, params);
		}else{
			return action.run(actionContext);
		}
	}

}
