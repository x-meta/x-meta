package org.xmeta.util;

import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
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
	private static final Logger logger = Logger.getLogger(ActionContainer.class.getName());
	static World world = World.getInstance();

	private final Thing actions;
	private final ActionContext actionContext;
	private List<Thing> actionThings = null;
	private Object object;
	private Map<String, ActionAnnotationHelper> methods;
	private boolean log = false;

	public ActionContainer(Thing actions, ActionContext actionContext) {
		this.actionContext = actionContext;
		this.actions = actions;

		//添加继承
		for(Thing ext : actions.getExtends()) {
			append(ext);
		}

		//是否打印日志，如果是那么打印更详细的信息
		this.log = actions.getBoolean("log");

		//初始化对象
		if(actions.getBoolean("thingLoader")){
			this.object = ThingLoader.getObject();
			if(log){
				logger.info("Get object from ThingLoader, object=" + object);
			}
		}else{
			this.object = actions.doAction("getObject", actionContext);
			if(log){
				logger.info("Get object from getObject, object=" + object);
			}
			if(this.object == null){
				Class<?> cls = actions.doAction("getClass", actionContext);
				if(log){
					logger.info("Get object from getClass, class=" + cls);
				}
				if(cls != null){
					try {
						this.object = cls.getConstructor(new Class<?>[0]).newInstance();

						if(log){
							logger.info("Get object from getClass, object=" + object);
						}
						if(this.object != null){
							ThingLoader.load(this.object, actionContext);
						}
					}catch(Exception e){
						logger.log(Level.WARNING, "New object error, actions=" + actions.getMetadata().getPath(), e);
					}
				}
			}
		}

		if(this.object != null){
			setObject(object);
		}
	}

	public void setObject(Object object){
		this.object = object;

		methods = new HashMap<>();

		//init methods
		Class<?> cls = object.getClass();
		for(Method method1 : cls.getMethods()) {
			String name = method1.getName();
			try {
				ActionAnnotationHelper helper = ActionAnnotationHelper.parse(cls, method1);
				methods.put(name, helper);
			} catch (NoSuchMethodException e) {
				throw new ActionException(e);
			}
		}
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
	
	public List<Thing> getAppendActions(){
		return actionThings;
	}

	public ActionAnnotationHelper getMethodHelper(String name){
		if(methods == null){
			return null;
		}

		return methods.get(name);
	}

	@SuppressWarnings("unchecked")
	public <T> T doAction(String name) {
		return (T) doAction(name, Collections.EMPTY_MAP);
	}

	@SuppressWarnings("unchecked")
	public <T> T doAction(String name, ActionContext context) {
		return (T) doAction(name, Collections.EMPTY_MAP);
	}

	@SuppressWarnings("unchecked")
	public <T> T doAction(String name, Map<String, Object> parameters) {
		try {
			ActionAnnotationHelper helper = getMethodHelper(name);
			if(helper != null){
				if(log){
					logger.info("Do action [" + actions.getMetadata().getPath() + ":" + name + "] by method, method=" + helper.getActionMethod());
				}
				Bindings bindings = actionContext.push();
				try {
					if(parameters != null){
						bindings.putAll(parameters);
					}
					return (T) helper.invoke(object, actionContext);
				}finally {
					actionContext.pop();
				}
			}else{
				Thing actionThing = getActionThing(name);
				if (actionThing != null) {
					if(log){
						logger.info("Do action [" + actions.getMetadata().getPath() + ":" + name + "] by action, action=" + actionThing.getMetadata().getPath());
					}

					Action action = world.getAction(actionThing.getMetadata().getPath());
					return action.run(actionContext, parameters);
				}else{
					logger.info("Can not do action [" + actions.getMetadata().getPath() + ":" + name + "], objec method or action not found");
				}

				return null;
			}

		} catch (Throwable e) {
			throw new ActionException("Container do action [" + actions.getMetadata().getPath() + ":" + name + "] exception, actions=" + actions.getMetadata().getPath(), e);
		}
	}

	public <T> T doAction(String name, ActionContext context, Map<String, Object> parameters) {
		return doAction(name, parameters);
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

		return actionThing.getActionThing(name);
	}
	
	public List<Thing> getActionThings(){
		List<Thing> list = new ArrayList<>();
		Map<String, Thing> context = new HashMap<>();
		initActions(actions, list, context);

		if(actionThings != null){
			for(Thing thing : actionThings){
				initActions(thing, list, context);
			}
		}

		list.sort((o1, o2) -> o1.getMetadata().getName().compareTo(o2.getMetadata().getName()));
		return list;
	}

	private void initActions(Thing thing, List<Thing> list, Map<String, Thing> context){
		for (Thing child : thing.getAllChilds()) {
			String name = child.getMetadata().getName();
			if(context.get(name) == null) {
				list.add(child);
				context.put(name, child);
			}
		}

		for(Thing ac : thing.getActionsThings()){
			String name = ac.getMetadata().getName();
			if(context.get(name) == null) {
				list.add(ac);
				context.put(name, ac);
			}
		}
	}
	public ActionContext getActionContext() {
		return actionContext;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder("ActionContainer: path=" + actions.getMetadata().getPath() + "\n    actions=");
		for(Thing ac : getActionThings()){
			str.append(ac.getMetadata().getName()).append(",");
		}
		return str.toString();
	}
	
	public <T> T execute(String name, Object ... parameters){
		try {
			return doAction(name, actionContext, parameters);
		} catch (Throwable e) {
			throw new ActionException("Container do action [" + name + "] exception, actions=" + actions.getMetadata().getPath(), e);
			//return null;
		}
	}
}
