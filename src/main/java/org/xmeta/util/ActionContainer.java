package org.xmeta.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.xmeta.*;
import org.xmeta.annotation.ActionAnnotationHelper;

/**
 * 动作容器，在一些模型中使用，用来存储各种动作。
 * 
 * @author zyx
 *
 */
public class ActionContainer {
	//private static Logger log = LoggerFactory.getLogger(ActionContainer.class);
	private static Logger logger = Logger.getLogger(ActionContainer.class.getName());
	static World world = World.getInstance();

	private Thing actions;
	private ActionContext actionContext;
	private List<Thing> actionThings = null;
	private Object object;
	private Map<String, ActionAnnotationHelper> methods;

	public ActionContainer(Thing actions, ActionContext actionContext) {
		this.actionContext = actionContext;
		this.actions = actions;

		this.object = ThingLoader.getObject();
	}

	public Thing getThing() {
		return actions;
	}
	
	public void append(Thing action) {
		if(actionThings == null) {
			actionThings = new ArrayList<Thing>();			
		}
		
		actionThings.add(action);
	}
	
	public List<Thing> getAppednActions(){
		return actionThings;
	}

	private <T> T invokeMethod(String name){
		if(object == null){
			return null;
		}

		if(methods == null){
			methods = new HashMap<>();
		}

		ActionAnnotationHelper helper = methods.get(name);
		if(helper == null){
			Class<?> cls = object.getClass();
			for(Method method1 : cls.getMethods()) {
				if(method1.getName().equals(name) ) {
					try {
						helper = ActionAnnotationHelper.parse(cls, method1);
					} catch (NoSuchMethodException e) {
						throw new ActionException(e);
					}
					break;
				}
			}

			if(helper == null){
				helper = new ActionAnnotationHelper();
			}

			methods.put(name, helper);
		}

		return (T) helper.invoke(object, actionContext);
	}

	public <T> T doAction(String name) {
		try {
			Thing actionThing = getActionThing(name);
			if (actionThing != null) {
				Action action = world.getAction(actionThing.getMetadata()
						.getPath());
				return action.run(actionContext);
			} else {
				return invokeMethod(name);
			}
		} catch (Throwable e) {
			throw new ActionException("Container do action [" + name + "] exception, actions=" + actions.getMetadata().getPath(), e);
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
				return invokeMethod(name);
			}
		} catch (Throwable e) {
			throw new ActionException("Container do action [" + name + "] exception, actions=" + actions.getMetadata().getPath(), e);
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
				Bindings bindings = actionContext.push();
				try {
					if(parameters != null){
						bindings.putAll(parameters);
					}
					return invokeMethod(name);
				}finally {
					actionContext.pop();
				}
			}
		} catch (Throwable e) {
			throw new ActionException("Container do action [" + name + "] exception, actions=" + actions.getMetadata().getPath(), e);
		}
	}

	public <T> T doAction(String name, ActionContext context, Map<String, Object> parameters) {
		try {
			Thing actionThing = getActionThing(name);
			if (actionThing != null) {
				Action action = world.getAction(actionThing.getMetadata()
						.getPath());
				return action.run(actionContext, parameters);
			} else {
				Bindings bindings = actionContext.push();
				try {
					if(parameters != null){
						bindings.putAll(parameters);
					}
					return invokeMethod(name);
				}finally {
					actionContext.pop();
				}
			}
		} catch (Throwable e) {
			throw new ActionException("Container do action [" + name + "] exception, actions=" + actions.getMetadata().getPath(), e);
		}
	}
	
	public <T> T doAction(String name, ActionContext context, Object ...parameters) {
		return doAction(name, context, UtilMap.toMap(parameters));
	}

	public Thing getActionThing(String name) {
		Thing thing = getActionThing(actions, name);
		if(thing == null && actionThings != null) {
			for(Thing actionThing : actionThings) {
				thing = getActionThing(actionThing, name);
				if(thing != null) {
					break;
				}
			}
		}
			
		if(thing == null && actions.getBoolean("log")) {
			logger.fine("ActionContainer: action is not found : " + actions.getMetadata().getPath()
					+ "/@" + name);
			return null;
		}else {
			return thing;
		}
	}
	
	private Thing getActionThing(Thing actionThing, String name) {
		for (Thing child : actionThing.getAllChilds()) {
			if (child.getMetadata().getName().equals(name)) {
				return child;
			}
		}
		
		Thing child = actionThing.getActionThing(name);
		if(child == null){			
			
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
			throw new ActionException("Container do action [" + name + "] exception, actions=" + actions.getMetadata().getPath(), e);
			//return null;
		}
	}
}
