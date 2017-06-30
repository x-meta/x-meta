package org.xmeta.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmeta.Action;
import org.xmeta.ActionContext;
import org.xmeta.Thing;
import org.xmeta.World;

/**
 * 动作容器，在一些模型中使用，用来存储各种动作。
 * 
 * @author zyx
 *
 */
public class ActionContainer {
	private static Logger log = LoggerFactory.getLogger(ActionContainer.class);
	static World world = World.getInstance();

	private Thing actions;
	private ActionContext actionContext;

	public ActionContainer(Thing actions, ActionContext actionContext) {
		this.actionContext = actionContext;
		this.actions = actions;

	}

	public Thing getThing() {
		return actions;
	}

	public <T> T doAction(String name) {
		try {
			Thing actionThing = getActionThing(name);
			if (actionThing != null) {
				Action action = world.getAction(actionThing.getMetadata()
						.getPath());
				return action.run(actionContext);
			} else {
				return null;
			}
		} catch (Throwable e) {
			log.error("Container do action " + name, e);
			return null;
		}
	}

	public <T> T doAction(String name, ActionContext context) {
		try {
			Thing actionThing = getActionThing(name);
			if (actionThing != null) {
				Action action = world.getAction(actionThing.getMetadata()
						.getPath());
				return action.run(actionContext);
			} else {
				return null;
			}
		} catch (Throwable e) {
			log.error("Container do action " + name, e);
			return null;
		}
	}

	public <T> T doAction(String name, Map<String, Object> parameters) {
		try {
			Thing actionThing = getActionThing(name);
			if (actionThing != null) {
				Action action = world.getAction(actionThing.getMetadata()
						.getPath());
				return action.run(actionContext, parameters);
			} else {
				return null;
			}
		} catch (Throwable e) {
			log.error("Container do action " + name, e);
			return null;
		}
	}

	public <T> T doAction(String name, ActionContext context,
			Map<String, Object> parameters) {
		try {
			Thing actionThing = getActionThing(name);
			if (actionThing != null) {
				Action action = world.getAction(actionThing.getMetadata()
						.getPath());
				return action.run(actionContext, parameters);
			} else {
				return null;
			}
		} catch (Throwable e) {
			log.error("Container do action [" + name + "] exception, actions=" + actions.getMetadata().getPath(), e);
			return null;
		}
	}
	
	public <T> T doAction(String name, ActionContext context, Object ...parameters) {
		return doAction(name, context, UtilMap.toMap(parameters));
	}

	public Thing getActionThing(String name) {
		for (Thing child : actions.getAllChilds()) {
			if (child.getMetadata().getName().equals(name)) {
				return child;
			}
		}
		
		Thing child = actions.getActionThing(name);
		if(child == null){			
			log.warn("action is not found : " + actions.getMetadata().getPath()
					+ "/@" + name);
			return null;
		}else{
			return child;
		}
	}
	
	public List<Thing> getActionThings(){
		List<Thing> list = new ArrayList<Thing>();
		for (Thing child : actions.getAllChilds()) {
			list.add(child);
		}
		
		for(Thing ac : actions.getActionsThings()){
			list.add(ac);
		}
		
		return list;
	}

	public ActionContext getActionContext() {
		return actionContext;
	}

	@Override
	public String toString() {
		String str = "ActionContainer: path=" + actions.getMetadata().getPath() + "\n    actions=";
		for(Thing ac : getActionThings()){
			str = str + ac.getMetadata().getName() + ",";
		}
		return str;
	}
	
	public <T> T execute(String name, Object ... parameters){
		try {
			Thing actionThing = getActionThing(name);
			if (actionThing != null) {
				Action action = world.getAction(actionThing.getMetadata()
						.getPath());
				return action.run(actionContext, parameters);
			} else {
				return null;
			}
		} catch (Throwable e) {
			log.error("Container do action [" + name + "] exception, actions=" + actions.getMetadata().getPath(), e);
			return null;
		}
	}
}
