/*******************************************************************************
* Copyright 2007-2013 See AUTHORS file.
 * 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
******************************************************************************/
package org.xmeta;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.parsers.ParserConfigurationException;

import ognl.OgnlException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmeta.cache.LinkedThingEntry;
import org.xmeta.cache.ThingCache;
import org.xmeta.cache.ThingEntry;
import org.xmeta.codes.TxtThingCoder;
import org.xmeta.codes.XmlCoder;
import org.xmeta.thingManagers.TransientThingManager;
import org.xmeta.util.OgnlUtil;
import org.xmeta.util.ThingCallable;
import org.xmeta.util.ThingRunnable;
import org.xmeta.util.ThingUtil;
import org.xmeta.util.UtilData;
import org.xmeta.util.UtilMap;
import org.xmeta.util.UtilString;
import org.xmeta.util.UtilThing;
import org.xml.sax.SAXException;

/**
 * <p>在X-Meta引擎里用事物表示任何东西。</p>
 * 
 * <p>事物有状态和行为，可以通过set和get、getxx方法来设置和获取事物的状态，可以通过doAction(String actionName)
 * 或doAction(String actionName, ActionContext actionContext)方法来执行事物。</p>
 * 
 * <p>事物之间有描述和继承关系，一个事物可以继承它的描述者行为以及继承继承者的属性和行为。</p>
 * 
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public class Thing {
	/** 日志 */
	private static Logger log = LoggerFactory.getLogger(Thing.class);
	private static Map<String, String> nameCache = new HashMap<String, String>(4096);
	
	/** 事物的名字 */
	public static final String THING = "thing";
	
	/** 属性的名字 */
	public static final String ATTRIBUTE = "attribute";
	
	/** 名字的名字 */
	public static final String NAME = "name";
	
	/** 标签的名字 */
	public static final String LABEL = "label";
	
	/** 描述者属性的名字 */
	public static final String DESCRIPTORS = "descriptors";
	
	/** 继承者属性的名字 */
	public static final String EXTENDS = "extends";
	
	/** 描述属性的名字 */
	public static final String DESCRIPTION = "description";
	
	/** 原始事物的路径，尤其是在做detach时 */
	public static final String ORIGIN_THING_PATH = "__ORIGIN_THING_PATH__";
	
	/** 事物的属性集合。 */
	protected Map<String, Object> attributes = new HashMap<String, Object>(32);
	
	/** 父事物，包含此事物的父事物。 */
	protected Thing parent = null;
	
	/** 事物的元数据，用于存放此事物在系统中的一些属性。 */
	protected ThingMetadata metadata = new ThingMetadata(this);
	
	/** 子事物列表 */
	protected List<Thing> childs = new CopyOnWriteArrayList<Thing>();
	
	/** 事物的名称（类名）的缓存列表 */
	protected List<String> thingNames = new CopyOnWriteArrayList<String>();
	
	/** 继承列表的缓存 */
	protected ThingEntry[] extendsCaches = null;
	
	/** 描述者列表的缓存 */
	protected ThingEntry[] descriptorsCaches = null;
	
	/** 读取事物内部信息的缓存 */
	//protected Map<String, Object> caches = new HashMap<String, Object>();
	
	/** 动作事物的缓存 */
	protected Map<String, LinkedThingEntry> actionCaches = new ConcurrentHashMap<String, LinkedThingEntry>();
		
	/** 正在更新的线程 */
	private static Map<Thread, Stack<Thread>> updatingThreads = new ConcurrentHashMap<Thread, Stack<Thread>>();
	
	/** 是否是瞬态的事物 */
	protected boolean isTransient = false;
	
	/** 每一个事物都有动作的形态 */
	protected Action action = null;
	
	/** 附加于事物的用户数据 */
	protected Map<String, Object> datas = null;//new HashMap<String, Object>();
	
	/** 
	 * 默认构造函数，构造一个空的瞬态事物。 
	 */
	public Thing(){		
		this(null, null, null, true);
	}
	
	/**
	 * 通过描述者的路径来构造一个瞬态事物。
	 * 
	 * @param descriptorPath 描述者的路径
	 */
	public Thing(String descriptorPath){	
		this(null, null, descriptorPath, true);
	}
	
	/**
	 * 通过名称和标签创建一个瞬态事物。
	 * 
	 * @param name 事物的名称
	 * @param label 事物的标签
	 */
	public Thing(String name, String label){
		this(name, label, null, true);
	}
	
	/**
	 * 通过名称、标签和描述者的路径来构造一个瞬态事物。
	 * 
	 * @param name 事物的名称
	 * @param label 事物的标签
	 * @param descriptorPath 事物的路径
	 */
	public Thing(String name, String label, String descriptorPath){
		this(name, label, descriptorPath, true);
	}
	
	/**
	 * 指定名称、标签、描述者和是否是瞬态的来构造一个事物。
	 * 非瞬态的事物一般有事物管理者创建。
	 * 
	 * @param name 事物的名称
	 * @param label 事物的标签
	 * @param descriptorPath 事物的描述者
	 * @param isTransient 是否是瞬态的
	 */
	public Thing(String name, String label, String descriptorPath, boolean isTransient){	
		this.beginModify();
		try{
			this.put(Thing.NAME, name);
			this.put(Thing.LABEL, label);
			this.put(Thing.DESCRIPTORS, descriptorPath);
			
			if(descriptorPath != null){
				this.initDefaultValue();
			}
			
			if(isTransient){
				TransientThingManager manager = World.getInstance().getTransientThingManager();
				String id = "" + manager.getNextId();
				metadata.setId(id);
				metadata.setPath("_transient.p" + id);
				metadata.setCategory(manager.getCategory("_transient"));			
				
				this.isTransient = true;
				this.save();
			}else{
				this.isTransient = false;
			}
		}finally{
			this.endModify(true);
		}
	}
	
	/**
	 * 添加一个子事物。
	 * 
	 * @param childThing 子事物
	 */
	public synchronized void addChild(Thing childThing){	
		//添加到子节点的末尾
		addChild(childThing, childs.size());				
	}
	
	public synchronized void addChild(Thing childThing, int index){
		addChild(childThing, index, true);
	}
	
	public synchronized void addChild(Thing childThing, boolean changeParen){
		addChild(childThing, childs.size(), changeParen);
	}
	
	/** 
	 * 在指定的索引位置添加一个子事物。
	 * 
	 * 如果指定索引不存在，那么尽量添加在最后面。
	 * 
	 * @param childThing 子事物
	 * @param index 位置索引
	 * @param changeParent 是否改变子事物的父事物为本事物
	 */
	public synchronized void addChild(Thing childThing, int index, boolean changeParent){		
		beginModify();
		
		try{
			if(childThing != null && changeParent){
				String childThingId = childThing.getMetadata().getId();
				if(childThingId == null || "".equals(childThingId)){
					childThingId = childThing.getMetadata().getName();
				}
				if(childThingId != null){					
					//过滤掉任何和路径相关的字符，ID不能包含这些字符
					childThingId = childThingId.replace('\'', '_');
					childThingId = childThingId.replace('\"', '_');
					childThingId = childThingId.replace(':'	, '_');
					childThingId = childThingId.replace('@'	, '_');
					childThingId = childThingId.replace('&'	, '_');
					childThingId = childThingId.replace('*'	, '_');
					childThingId = childThingId.replace('#'	, '_');
					childThingId = childThingId.replace('$'	, '_');
					childThingId = childThingId.replace('%'	, '_');
					childThingId = childThingId.replace('/'	, '_');
					childThingId = childThingId.replace('.'	, '_');
					childThingId = childThingId.replace('\\', '_');
				}
				
				//判断子节点的ID是否重复，如果有重复那么重新设置一个
				boolean repeated = false;
				for(Thing child : childs){
					if(child.getMetadata().getId().equals(childThingId)){
						repeated = true;
						break;
					}
				}
				
				if(!repeated){
					//id可以使用
					childThing.getMetadata().setId(childThingId);
					childs.add(index, childThing);
				}else{
					//id重复，设置新的ID
					int idIndex = 0;
					for(Thing child : childs){
						String childId = child.getMetadata().getId();
						if(childId.startsWith(childThingId)){
							try{
								int aIndex = Integer.parseInt(childId.substring(childThingId.length(), childId.length()));
								if(aIndex > idIndex){
									idIndex = aIndex;
								}
							}catch(Exception e){							
							}
						}
					}
					
					idIndex ++;
					childThingId = childThingId + idIndex;
					childThing.getMetadata().setId(childThingId);
					childs.add(index, childThing);
				}
				childThing.parent = this;
				initChildMetadata(childThing);
			}else if(childThing != null){
				childs.add(childThing);

				if(changeParent){
					initChildMetadata(childThing);
				}
			}
		}finally{
			endModify(changeParent);
		}		
	}
	
	/**
	 * <p>在指定位置添加一个描述者。</p>
	 * 
	 * 如果描述者已经存在且位置不同，那么会切换到新的位置，如果index为-1那么添加至结尾。
	 * 
	 * @param index 描述者的位置
	 * @param descriptor 描述者
	 */
	public synchronized void addDescriptor(int index, Thing descriptor){
		if(descriptor != null){
			String descriptors = (String) attributes.get(Thing.DESCRIPTORS);
			String descriptorPath = descriptor.getMetadata().getPath();
			
			if(descriptors != null && !"".equals(descriptors)){
				descriptors = UtilString.insert(descriptors, descriptorPath, index);
				
				put(Thing.DESCRIPTORS, descriptors);
			}else{				
				put(Thing.DESCRIPTORS, descriptorPath);
			}
		}				
	}
	
	/**
	 * 通过描述者的路径加入描述者，可以加入多个，中间使用','号隔开。
	 * 
	 * @param index 位置索引
	 * @param descriptorPath 描述者的路径
	 */
	public synchronized void addDescritpor(int index, String descriptorPath){
		if(descriptorPath != null){
			for(String descPath : UtilString.split(descriptorPath, ',')){
				Thing descriptor = World.getInstance().getThing(descPath);
				if(descriptor != null){
					addDescriptor(index, descriptor);
					index++;
				}
			}
		}
	}
		
	/**
	 * 添加需要分离的子事物。
	 * 
	 * @param forDetachedChild 需要分离的子事物
	 * @param detachToTransient 是否克隆成瞬态事物
	 */
	private void addDetachedChild(Thing thing, Thing forDetachedChild, Map<Thing, Thing> context){
		beginModify();		
		try{
			if(context.get(forDetachedChild) != null){
				thing.addChild(context.get(forDetachedChild));
			}else{				
				Thing newChildThing = null;
				newChildThing = new Thing();
				newChildThing.attributes.putAll(forDetachedChild.attributes);
				newChildThing.getMetadata().setId(forDetachedChild.getMetadata().getId());
				
				context.put(forDetachedChild, newChildThing);
				thing.addChild(newChildThing);
				
				for(Thing childChildThing : forDetachedChild.childs){
					newChildThing.addDetachedChild(newChildThing, childChildThing, context);
				}
			}
		}finally{
			endModify(true);
		}
	}
	
	/**
	 * <p>在指定位置添加一个继承事物。</p>
	 * 
	 * 如果继承已经存在且位置不同，那么会切换到新的位置，如果index为-1那么添加至结尾。
	 * 
	 * @param extendThing 要继承的事物
	 * @param index 参数位置
	 */
	public void addExtend(int index, Thing extendThing){		
		if(extendThing != null){
			String extendsStr = (String) attributes.get(Thing.EXTENDS);
			String extendPath = extendThing.getMetadata().getPath();
			
			if(extendsStr != null && !"".equals(extendsStr)){
				extendsStr = UtilString.insert(extendsStr, extendPath, index);
				
				put(Thing.EXTENDS, extendsStr);
			}else{				
				put(Thing.EXTENDS, extendPath);
			}
		}
	}
	
	/**
	 * 改变一个子事物的位置索引。
	 * 
	 * @param child 子事物
	 * @param index 要移动的位置，如果为-1，那么通过moveStep来上移或下移
	 * @param moveStep 小于0表示上移，大于0表示下移，步伐为其绝对值
	 */
	public void changeChildIndex(Thing child, int index, int moveStep){
		//首先判断是本子事物
		int oldIndex = 0;
		boolean have = false;
		for(Thing chd : childs){
			if(chd == child){
				have = true;				
				break;
			}
			
			oldIndex ++;
		}
				
		if(have){			
			childs.remove(child);
			if(index < 0){
				index = oldIndex + moveStep;
			}
			
			if(index < 0){
				index = 0;
			}
			
			if(index >= childs.size()){
				childs.add(child);
			}else{
				childs.add(index, child);
			}
			
			child.updateLastModified();
		}				
	}
	
	/**
	 * 从一个Map数据认知并把认知结果保存到自身。
	 * 
	 * @param adata 一个Map数据
	 */
	@SuppressWarnings("unchecked")
	public void cognize(Map<String, Object> adata){
		if(adata == null){
			return;
		}
		
		beginModify();
		
		try{
			//保存属性
			for(Thing attributeDescriptor : getAllAttributesDescriptors()){
				String key = attributeDescriptor.getMetadata().getName();
				
				/*if(Thing.NAME.equals(key) || Thing.LABEL.equals(key) || Thing.DESCRIPTION.equals(key)
						|| Thing.EXTENDS.equals(key) || Thing.DESCRIPTORS.equals(key)){
					continue;
				}*/
				
				if(adata.containsKey(key)){
					Object value = adata.get(key);
					if(!(value instanceof Map)){
						put(key, value);
					}
				}
			}
			
			//添加子事物
			for(Thing childDescriptor : getAllChildsDescriptors()){
				String childName = childDescriptor.getMetadata().getName();
				
				Object value = adata.get(childName);
				if(value instanceof Map){
					Map<String, Object> childData = (Map<String, Object>) value;
					Thing newChild = new Thing(null, null, childDescriptor.getMetadata().getPath(), false);
					newChild.cognize(childData);
					
					addChild(newChild);
				}
			}
		}finally{
			endModify(true);
		}
	}
	
	/**
	 * 从一个XML数据认知并把认知结果保存到自身。
	 * 
	 * @param xmlData 一个XML数据（字符串）
	 * @throws IOException IO异常
	 * @throws SAXException  SAX异常
	 * @throws ParserConfigurationException 分析异常 
	 */
	public void cognize(String xmlData) throws ParserConfigurationException, SAXException, IOException{
		Thing thing = new Thing();
		XmlCoder.parse(thing, xmlData);
		cognize(thing);
	}
	
	/**
	 *  <p>认知另一事物并把认知结果作为自己的一部分。</p>
	 *  
	 *  认识另一事物和认识非事物的对象的区别是，认识其他事物是把另一事物的所有内容保存到自身，而认识
	 *  非事物物时是根据自身的描述者所描述的属性和子事物去从认知对象上取值的。
	 *  
	 * @param thing 另一事物
	 */
	public void cognize(Thing thing){
		if(thing == null){
			return;
		}
		
		beginModify();
		
		try{
			//获取被认知事物的描述者并添加到自己的描述者列表中
			List<Thing> descriptors = thing.getDescriptors();
			for(Thing descriptor : descriptors){
				//元事物的描述者不添加
				if(descriptor != World.getInstance().baseClass && !descriptor.getMetadata().getPath().equals("metaThing")){
					addDescriptor(-1, descriptor);
				}			
			}
			
			//获取被认知的事物的继承对象并添加到自己的继承列表中
			List<Thing> extendThings = thing.getExtends();
			for(Thing extend : extendThings){
				addExtend(-1, extend);
			}
			
			//保存属性
			for(String key : thing.getAttributes().keySet()){
				//name, label, description, extends, descriptors等属性不获取
				if(Thing.NAME.equals(key) || Thing.LABEL.equals(key) || Thing.DESCRIPTION.equals(key)
						|| Thing.EXTENDS.equals(key) || Thing.DESCRIPTORS.equals(key)){
					
					putIfNull(key, thing.getAttribute(key));
					continue;
				}
				
				put(key, thing.getAttribute(key));
			}
			
			//认知被认知事物的子事物
			for(Thing child : thing.getChilds()){
				//判断自身的子事物是否含有相同的，判断第一个描述者是否相同和名称是否相同
				boolean have = false;
				//Thing mySameChild = null;
				/*List<Thing> childDescriptors = child.getDescriptors();			
				for(Thing myChild : getChilds()){
					List<Thing> myChildDescriptors = myChild.getDescriptors();
					if(childDescriptors.get(0) == myChildDescriptors.get(0) && 
							childDescriptors.get(0).getMetadata().getId().equals(myChildDescriptors.get(0).getMetadata().getId())){
						have = true;
						mySameChild = myChild;
						break;
					}
				}*/
				
				if(have){
					//mySameChild.cognize(child);
				}else{
					//作为新的子事物添加
					Thing detachedChild = child.detach();
					addChild(detachedChild);
				}
			}
		}finally{
			endModify(true);
		}
	}
	
	/**
	 * 如果指定名称的属性为null，那么设置值，否则不。
	 * 
	 * @param name 名称
	 * @param value 值
	 */
	private void putIfNull(String name, Object value){
		Object v = attributes.get(name);
		if(v == null || "".equals(v)){
			put(name, value);
		}
	}
	
	public void paste(Map<String, Object> data){
		cognize(data);
	}
	
	public void paste(String data) throws ParserConfigurationException, SAXException, IOException{
		cognize(data);
	}
	
	public void parseXML(String xml) throws ParserConfigurationException, SAXException, IOException{
		cognize(xml);
	}
	
	public void paste(Thing data){
		cognize(data);
	}
	
	
	/**
	 * 克隆一个新事物，新的事物是瞬态的。
	 * 
	 * 不会克隆事物的继承和描述，仅仅是克隆事物本身。
	 * 
	 * @return 克隆的新事物
	 */
	public Thing detach(){
		Thing newThing = null;
		
		beginModify();		
		try{
			newThing = new Thing();
			newThing.attributes.putAll(this.attributes);
			newThing.getMetadata().setId(this.getMetadata().getId());
			
			Map<Thing, Thing> context = new HashMap<Thing, Thing>();
			context.put(this, newThing);
			
			for(Thing childThing : getChilds()){
				addDetachedChild(newThing, childThing, context);
			}
			
			if(this.get(ORIGIN_THING_PATH) == null){
				newThing.set(ORIGIN_THING_PATH, this.getMetadata().getPath());
			}
		}finally{
			endModify(true);
		}
		return newThing;
	}
	
	/**
	 * 使用detach()。
	 * 
	 * @param detachToTransient 是否detach到瞬态
	 * @return 事物
	 * @deprecated
	 */
	public Thing detach(boolean detachToTransient){
		return detach();
	}
	
	/**
	 * 执行一个动作，把自己作为self变量放入动作上下文中。
	 * 
	 * @param name 动作名称
	 * 
	 * @return 执行后的返回结果
	 */
	public <T> T doAction(String name){
		return run(name, new ActionContext(), (Object[]) null, false, true);
	}
	
	/**
	 * 执行一个动作，把自己作为self变量放入动作上下文中，使用新的动作上下文和传入参数。
	 * 
	 * @param name 动作名
	 * @param parameters 参数
	 * @return 执行结果
	 */
	public <T> T doAction(String name, Map<String, Object> parameters){
		return run(name, new ActionContext(), parameters, false, true);
	}
	
	/**
	 * 执行一个动作，把自己作为self变量放入动作上下文中。
	 * 
	 * @param name 动作名称
	 * @param actionContext 变量容器
	 * @return 执行后的返回结果
	 */
	@SuppressWarnings("unchecked")
	public <T> T doAction(String name, ActionContext actionContext){
		return (T) run(name, actionContext, (Map<String, Object>) null, false, true);
	}
	
	/**
	 * 执行一个动作，把自己作为self变量放入动作上下文中。
	 * 
	 * @param name 动作名称
	 * @param actionContext 变量容器
	 * @param parameters 参数
	 * @return 执行后的返回结果
	 */
	public <T> T doAction(String name, ActionContext actionContext, Map<String, Object> parameters){
		return run(name, actionContext, parameters, false, true);
	}
	
	public <T> T doAction(String name, ActionContext actionContext, Object ... parameters){
		return doAction(name, actionContext, UtilMap.toMap(parameters));
	}
		
	/**
	 * 执行一个动作，把自己作为self变量放入动作上下文中。
	 * 
	 * @param name 动作名
	 * @param actionContext  变量上下文
	 * @param parameters 参数
	 * @param isSubAction 是否是子动作
	 * @return 执行结果
	 */
	public <T> T doAction(String name, ActionContext actionContext, Map<String, Object> parameters, boolean isSubAction){
		return run(name, actionContext, parameters, isSubAction, true);
	}
	
	/**
	 * 执行一个动作。
	 * 
	 * @param name 动作名
	 * @param context 变量上下文
	 * @param parameters 参数
	 * @param isSubAction 是否是子动作
	 * @param includeSelf 是否包含自己
	 * @return 执行结果
	 */
	public <T> T run(String name, ActionContext context, Map<String, Object> parameters, boolean isSubAction, boolean includeSelf){
		Thing actionThing = getActionThing(name);
		//System.out.println("Thing 515 get action time : " + (System.currentTimeMillis() - start));
		Action action = null;
		if(actionThing == null){
			return null;
		}else{
			action = actionThing.getAction();
		}
		if(context == null){
			context = new ActionContext();
		}
	
		//long start = System.currentTimeMillis();
		if(action == null){
			log.info("thing's action is not found : " + getMetadata().getPath() + " : " + name);
			return null;
		}else{
			World world = World.getInstance();
			if(world.isHaveActionListener()){
				ActionListener listener = world.getActionListener();
				try{
					listener.actionExecuted(this, name, context, parameters, System.currentTimeMillis(), true);
				}catch(Throwable t){
					log.error("ActionRecorder error", t);
				}
			}
			
			if(includeSelf){
				Bindings bindings = context.pushPoolBindings();
				bindings.setCaller(this, name);
				bindings.put("self", this);
				
				try{
					fireGlobalContextDoActionEvent(name, parameters, context);
					return action.runMapParams(context, parameters, this, isSubAction);
					//return action.run(context, parameters, this, isSubAction);
				}finally{
					context.pop();
					//log.info("run action time " + actionThing.getMetadata().getPath() + " : " + (System.currentTimeMillis() - start));
				}
			}else{
				context.peek().setCaller(this, name);
				fireGlobalContextDoActionEvent(name, parameters, context);
				return action.runMapParams(context, parameters, this, isSubAction);
			}
		}
	}
	
	public void fireGlobalContextDoActionEvent(String name, Map<String,Object> params, ActionContext actionContext){
		World world = World.getInstance();
		if(world.globalContexts != null && world.globalContexts.size() > 0){
			for(ThingEntry contextEntry : world.globalContexts){
				Thing context = contextEntry.getThing();				
				if(context != null && context != this){
					context.doAction("onDoAction", actionContext, "thing", this, "actionName", name, "params", params);					
				}
			}
		}
	}
	
	public <T> T  exec(String name, ActionContext context, Object... params){
		return run(name, context, params, false, false);		
	}
	
	public <T> T  exec(String name, Object... params){
		return run(name, null, params, false, false);	
	}
	
	public <T> T  doExec(String name, ActionContext context, Object... params){
		return run(name, context, params, false, true);		
	}
	
	public <T> T  doExec(String name, Object... params){
		return run(name, null, params, false, true);	
	}
	
	/**
	 * 按普通参数的方式执行。
	 * 
	 * @param name 动作名
	 * @param context 变量上下文
	 * @param parameters 参数列表
	 * @param isSubAction 是否是子动作
	 * @param includeSelf 是否包含自己
	 * @return 执行结果
	 */
	public <T> T  run(String name, ActionContext context, Object[] parameters, boolean isSubAction, boolean includeSelf){
		Thing actionThing = getActionThing(name);
		//System.out.println("Thing 515 get action time : " + (System.currentTimeMillis() - start));
		Action action = null;
		if(actionThing == null){
			return null;
		}else{
			action = actionThing.getAction();
		}
		if(context == null){
			context = new ActionContext();
		}
	
		//long start = System.currentTimeMillis();
		if(action == null){
			log.info("thing's action is not found : " + getMetadata().getPath() + " : " + name);
			return null;
		}else{
			if(includeSelf){
				Bindings bindings = context.pushPoolBindings();
				bindings.setCaller(this, name);
				bindings.put("self", this);
				
				try{
					return action.runArrayParams(context, parameters, this, isSubAction);
				}finally{
					context.pop();
					//log.info("run action time " + actionThing.getMetadata().getPath() + " : " + (System.currentTimeMillis() - start));
				}
			}else{
				return action.runArrayParams(context, parameters, this, isSubAction);
			}
		}
	}
			
	/**
	 * 执行一个动作，不把自己放入动作上下文中。
	 * 
	 * @param name 动作名
	 * @param context 变量上下文
	 * @param parameters 参数
	 * @param isSubAction 是否是子动作
	 * @return 执行结果
	 */
	public <T> T  run(String name, ActionContext context, Map<String, Object> parameters, boolean isSubAction){
		return run(name, context, parameters, isSubAction, false);
	}
	
	/**
	 * 执行一个动作，不把自己放入动作上下文中。
	 * @param name 动作名
	 * @param context 变量上下文
	 * @param parameters 参数
	 * @return 执行结果
	 */
	public <T> T  run(String name, ActionContext context, Map<String, Object> parameters){
		return run(name, context, parameters, false, false);
	}
	
	/**
	 * 执行一个动作，不把自己放入动作上下文中。
	 * 
	 * @param name 动作名
	 * @param context 变量上下文
	 * @return 执行结果
	 */
	public <T> T  run(String name, ActionContext context){
		return run(name, context, (Object[]) null, false, false);
	}
	
	/**
	 * 执行一个动作，不把自己放入动作上下文中，使用新的动作上下文和传入的参数。
	 * 
	 * @param name 动作名
	 * @param parameters 参数
	 * @return 执行结果
	 */
	public <T> T  run(String name, Map<String, Object> parameters){
		return run(name, null, parameters, false, false);
	}
	
	/**
	 * 执行一个动作，不把自己放入动作上下文中。
	 * 
	 * @param name 动作名
	 * @return 执行结果
	 */
	public <T> T  run(String name){
		return run(name, null, (Object[]) null, false, false);
	}
	
	public Object get(Path path){
		if(path == null || path.getChildPath() == null){
			return this;
		}

		Path lastChildPath = path;
		Path childPath = path.getChildPath();
		Thing currentThing = this;
		Thing nextCurrentThing = null;
		while(childPath != null && currentThing != null){
			switch(childPath.getType()){
			case Path.TYPE_CHILD_THINGS:
				//获取子事物列表
				List<Thing> childThings = new ArrayList<Thing>();
				for(Iterator<Thing> iter = currentThing.getChildsIterator(); iter.hasNext();){
    				Thing child = iter.next();
    				if(childPath.getThingName() != null && !"".equals(childPath.getThingName()) && !child.isThingByName(childPath.getThingName())){
    					//按照事物名取，但不是本事物名
    					continue;
    				}
    				
    				childThings.add(child);    		
				}
				return childThings;
			case Path.TYPE_CHILD_THING_AT_INDEX:
				//获取第n个子事物
				int index = 0;
				nextCurrentThing = null;
				for(Iterator<Thing> iter = currentThing.getChildsIterator(); iter.hasNext();){
					Thing child = iter.next();
					if(childPath.getThingName() != null && !"".equals(childPath.getThingName()) && !child.isThingByName(childPath.getThingName())){
						//按照事物名取，但不是本事物名
						continue;
					}
					
					if(childPath.getIndex() == index){
						nextCurrentThing = child;
						lastChildPath = childPath;
						childPath = childPath.getChildPath();
						
						break;
					}
					index++;    		
				}
				currentThing = nextCurrentThing;
				break;
			case Path.TYPE_CHILD_THING:
				//子事物
				nextCurrentThing = null;
				for(Iterator<Thing> iter = currentThing.getChildsIterator(); iter.hasNext();){
					Thing child = iter.next();
					if(child.getMetadata().getId().equals(childPath.getThingId())){
						nextCurrentThing = child;
						lastChildPath = childPath;
						childPath = childPath.getChildPath();
						
						break;
					}
				}
				
				currentThing = nextCurrentThing;
				break;
			case Path.TYPE_ATTRIBUTE:
				return currentThing.get(childPath.getAttributeName());
			case Path.TYPE_CHILD_THING_OR_INDEX:
				int index1 = 0;
				nextCurrentThing = null;
				for(Iterator<Thing> iter = currentThing.getChildsIterator(); iter.hasNext();){
    				Thing child = iter.next();
    				if(childPath.getThingName() != null && !"".equals(childPath.getThingName()) && !child.isThingByName(childPath.getThingName())){
    					//按照事物名取，但不是本事物名
    					continue;
    				}
    				
    				if(child.getMetadata().getId().equals(childPath.getThingId())){
    					nextCurrentThing = child;
						lastChildPath = childPath;
						childPath = childPath.getChildPath();
						
						break;
    				}
    				try{
    					if(index1 == Short.parseShort(childPath.getThingId())){
    						nextCurrentThing = child;
    						lastChildPath = childPath;
    						childPath = childPath.getChildPath();
    						
    						break;
    					}
    				}catch(Exception e){    					
    				}
    				index1++;
				}			
				currentThing = nextCurrentThing;
				break;
				default:
					
			}
		}
			
		if(currentThing == null){
			return getNullReturn(lastChildPath);
		}else{
			if(childPath == null){
				return currentThing;
			}else{
				return null;
			}
		}
	}
	
	/**
	 * <p>通过路径获得事物的属性或者子事物，可返回属性值、子事物或者子事物列表。</p>
	 * 
	 * 路径遵从事物的路径规则。
	 * 
	 * @param path 路径
	 * @return 路径对应的对象，找不到返回null
	 */
	public Object get(String path){
		if(path == null || "".equals(path)){
			return this;
		}
					
		//拆分路径
		String paths[] = UtilString.split(path, '/'); //path.split("[/]");
		Thing current = this;
		
		for(int i=0; i<paths.length; i++){
			//long start = System.currentTimeMillis();
			
    		if(current == null){
    			return getNullReturn(path);
    		}
    		
    		String p = paths[i];
    		if(p.equals("")){
    			continue;
    		}
    		
    		String[] ps = UtilString.split(p, '@'); //p.split("[@]");
    		if(ps.length == 1){
    			//取属性
    			if(i < paths.length - 1){
    				return getNullReturn(path);
    			}else{
    				if(p.endsWith("@")){
    					return current.getAllChilds(ps[0]);
    				}else{
    					return current.getAttribute(ps[0]);
    				}
    			}
    		}else if(ps.length == 2 && ps[1].equals("")){
    			//取节点列表
    			if(i < paths.length - 1){
    				return getNullReturn(path);
    			}else{
    				if("".equals(ps[0])){
    					return current.getChilds();
    				}else{
    					return current.getAllChilds(ps[0]);
    				}
    			}
    		}else if(ps.length == 2){
    			//取子节点
    			boolean setted = false;
    			boolean isByName = false;
    			
    			String compareStr = ps[1];
    			if(compareStr.startsWith("?")){
    				isByName = true;
    				compareStr = compareStr.substring(1, compareStr.length());
    			}
    			
    			//List<Thing> currentChilds = current.getAllChilds();
    			//for(Thing currentChild : currentChilds){
    			for(Iterator<Thing> iter = current.getChildsIterator(); iter.hasNext();){
    				Thing currentChild = iter.next();
    				if((ps[0].equals("") || currentChild.isThingByName(ps[0])) && 
    						((isByName == false && compareStr.equals(currentChild.getMetadata().getId())) ||
    								(isByName && compareStr.equals(currentChild.getMetadata().getName())))){    			
    					
    					current = currentChild;
    					setted = true;
    					break;    		
    				}
    				//currentChilds.add(currentChild);
    			}
    			    
    			if(!setted){    	
    				int index = -1;
        			try{
        				index = Integer.parseInt(ps[1]);
        			}catch(Exception e){    				
        			}
        			
        			if(index != -1){        			
        				int n = 0;
        				//for(Thing currentChild : currentChilds){
        				for(Iterator<Thing> iter = current.getChildsIterator(); iter.hasNext();){
        	    			Thing currentChild = iter.next();
            				if((ps[0].equals("") || currentChild.isThingByName(ps[0]))){
            					if(n == index){
	            					current = currentChild;
	            					setted = true;
	            					break;    		
            					}
            					
            					n++;
            				}
            			}
        			}else{
        				return null;
        			}
    			}
    			
    			if(!setted){
    				return getNullReturn(path);
    			}
    		}else{
    			//不认识的格式
    			return getNullReturn(path);
    		}
    		
    		
    		//System.out.println("get " + path + " : " + p + " " + (System.currentTimeMillis() - start));    		
    	}
    	
    	return current;
	}
	
	private Thing getSelfActionThing(String name, Map<Thing, Object> context, LinkedThingEntry linkedThingEntry){
		if(context.get(this) != null){
			return null;
		}else{
			context.put(this, this);
		}
		
		//从自己定义的动作中寻找
		Thing actionSet = null;
		for(Thing child : getChilds()){
			if(child.isThingByName("actions")){
				actionSet = child;
				break;
			}
		}			
		if(actionSet != null){
			for(Thing child : actionSet.getChilds()){
				if(child.getMetadata().getName().equals(name)){// || child.getMetadata().getName().equals(name)){
					linkedThingEntry.addThing(child);
					return child;
				}
			}
		}
		
		return null;
	}
	
	private Thing getActionThing(String name, Map<Thing, Object> context, Map<Thing, Object> superContext, LinkedThingEntry linkedThingEntry){
		Thing actionThing = this.getSelfActionThing(name, context, linkedThingEntry);
		if(actionThing == null){
			actionThing = this.getSuperActionThing(name, context, superContext, linkedThingEntry);
		}
		
		return actionThing;
	}
	
	private Thing getSuperActionThing(String name, Map<Thing, Object> context, Map<Thing, Object> superContext, LinkedThingEntry linkedThingEntry){
		Thing actionThing = null;
		if(superContext.get(this) != null){
			return null;
		}
		superContext.put(this, this);
		
		linkedThingEntry.addThing(this);
		try{
			//从描述者自己上寻找	 		
			if(actionThing == null){
				for(Thing descriptor : getDescriptors()){
					actionThing = descriptor.getSelfActionThing(name, context, linkedThingEntry);
					if(actionThing != null){
						break;
					}
				}
			}
			
			//从继承者自己上寻找
			if(actionThing == null){
				for(Thing extend : getExtends()){
					actionThing = extend.getSelfActionThing(name, context, linkedThingEntry);
					if(actionThing != null){
						break;
					}
				}
			}
			
			//从描述者的父上寻找
			if(actionThing == null){
				for(Thing descriptor : getDescriptors()){
					actionThing = descriptor.getSuperActionThing(name, context, superContext, linkedThingEntry);
					if(actionThing != null){
						break;
					}
				}
			}
			
			//从继承者父上寻找
			if(actionThing == null){
				for(Thing extend : getExtends()){
					actionThing = extend.getSuperActionThing(name, context, superContext, linkedThingEntry);
					if(actionThing != null){
						break;
					}
				}
			}
		}finally{
			if(actionThing == null){
				linkedThingEntry.removeLast();
			}
		}
		
		return actionThing;
	}
	
	/**
	 * <p>获得指定动作的事物定义。</p>
	 * 
	 * 搜寻事物动作的规则是：
	 *    如果不是super的动作，那么先搜寻事物本身定义的动作。
	 *    依次搜寻描述者和描述者的继承定义的动作。
	 *    依次搜索事物的描述者的继承者定义的动作。
	 * 
	 * @param name 动作名称
	 * @return 动作事物，如果不存在返回null
	 */
	public Thing getActionThing(String name){
		//取事物定义的行为
		if(name == null || "".equals(name)){
			return null;
		}
		Thing actionThing = null;		
		LinkedThingEntry linkedThingEntry = actionCaches.get(name);
		if(linkedThingEntry != null){
			Thing thing = linkedThingEntry.getThing();
			if(thing != null){
				return thing;
			}
		}
		
		linkedThingEntry = new LinkedThingEntry();
		
		Map<Thing, Object> context = new HashMap<Thing ,Object>();
		Map<Thing, Object> superContext = new HashMap<Thing ,Object>();
		if(name.startsWith("super.")){
			actionThing = this.getSuperActionThing(name.substring(6, name.length()), context, superContext, linkedThingEntry);
		}else{
			actionThing = this.getActionThing(name, context, superContext, linkedThingEntry);
		}
		
		if(actionThing != null){
			actionCaches.put(name, linkedThingEntry);
		}
		
		return actionThing;
	}
		
	private void addToSourceByName(List<Thing> srcs, Thing forAdd){
		boolean have = false;
		String forAddName = forAdd.getMetadata().getName();
		for(Thing src : srcs){
			if(src.getMetadata().getName().equals(forAddName)){
				have = true;
				break;
			}
		}
		
		if(!have){
			srcs.add(forAdd);
		}
	}
	
	public Action getAction(){
		if(action == null){
			action = new Action(this);
		}
		
		return action;
	}
	
	/**
	 * 把当前事物转化为一个Runnable。
	 * 
	 * @param actionContext 变量上下文
	 * @return runnable
	 */
	public Runnable getRunnable(ActionContext actionContext){
		return new ThingRunnable(this, actionContext, null);
	}
	
	/**
	 * 把当前事物转化为一个Runnable。
	 * 
	 * @param actionContext 变量上下文
	 * @param params 参数
	 * @return runnable
	 */
	public Runnable getRunnable(ActionContext actionContext, Map<String, Object> params){
		return new ThingRunnable(this, actionContext, params);
	}
	
	/**
	 * 把当前事物转化为一个Callable。
	 * 
	 * @param actionContext 变量上下文
	 * @return callable
	 */
	public Callable<Object> getCallable(ActionContext actionContext){
		return new ThingCallable(this, actionContext, null);
	}
	
	/**
	 * 把当前事物转化为一个Callable。
	 * 
	 * @param actionContext 变量上下文
	 * @param params 参数
	 * @return callable
	 */
	public Callable<Object> getCallable(ActionContext actionContext, Map<String, Object> params){
		return new ThingCallable(this, actionContext, params);
	}
	
	public List<Thing> getActionsThings(){
		return getActionThings();
	}
	
	/**
	 * 返回本事物的所有的动作定义，包括自身定义的、描述者定义的和继承定义的。
	 * 
	 * @return 动作定义列表
	 */
	public List<Thing> getActionThings(){
		List<Thing> actionThings = new ArrayList<Thing>();
		
		//找事物自身定义的动作
		
		Thing actionSet = null;
		for(Thing child : childs){
			if(child.isThingByName("actions")){
				actionSet = child;
				break;
			}
		}
		//getThing("actions@0");
		if(actionSet != null){
			for(Thing child : actionSet.getAllChilds()){
				addToSourceByName(actionThings, child);
			}
		}
		
		//从描述者中取
		for(Thing descriptor : getAllDescriptors()){
			Thing descActionSet = descriptor.getThing("actions@0");
			if(descActionSet != null){
				for(Thing child : descActionSet.getAllChilds()){
					addToSourceByName(actionThings, child);
				}
			}
		}
		
		//从继承者取		
		for(Thing extend : getAllExtends()){
			Thing descActionSet = extend.getThing("actions@0");
			if(descActionSet != null){
				for(Thing child : descActionSet.getAllChilds()){
					addToSourceByName(actionThings, child);
				}
			}
		}
		
		return actionThings;
	}
	
	/**
	 * 取本事物的所有描述者所定义属性描述列表。
	 * 
	 * @return 属性描述列表
	 */
	public List<Thing> getAllAttributesDescriptors(){
		List<Thing> attributesDescriptors = new ArrayList<Thing>();
		
		//取本事物的描述者列表
		List<Thing> selfDescriptors = getDescriptors();
		Map<String, String> context = new HashMap<String, String>(); //过滤重复名字的上下文
		for(Thing descriptor : selfDescriptors){
			for(Thing attr : descriptor.getAllChilds("attribute")){
				String name = attr.getMetadata().getName();
				if(context.get(name) == null){
					context.put(name, name);
					attributesDescriptors.add(attr);
				}
			}
			//UtilData.addToSource(attributesDescriptors, descriptor.getAllChilds("attribute"), false);
		}
		
		return attributesDescriptors;
	}
		
	/**
	 * 获得所有的直接第一级子事物，包括继承的事物的子事物。
	 * 
	 * @return 所有的子事物
	 */
	public List<Thing> getAllChilds(){
		List<Thing> childList = new ArrayList<Thing>();

		//添加自身定义的子事物
		childList.addAll(getChilds());
		
		//添加继承者的子事物
		for(Thing thing : getAllExtends()){
			for(Thing child : thing.childs){
				if(!"private".equals(child.getString("modifier"))){
					childList.add(child);
					//childList.addAll(thing.childs);
				}
			}
		}
		
		return childList;
	}
	
	/**
	 * <p>根据描述者的名称来获取所有符合的子事物，包括继承的子事物。</p>
	 * 
	 * 注：这里是描述者的名，不是描述者的路径。
	 * 
	 * @param thingName 描述者的名称
	 * 
	 * @return 描述者的名称为指定名称的子事物
	 */
	public List<Thing> getAllChilds(String thingName){
		List<Thing> childs = new ArrayList<Thing>();
		
		for(Iterator<Thing> iter = getChildsIterator(); iter.hasNext();){
			Thing child = iter.next();
			
			if(child.isThingByName(thingName) && (!"private".equals(child.getString("modifier")) || child.getParent() == this)){
				UtilData.addToSource(childs, child, true);
			}
		}
		
		return childs;
	}
	
	/**
	 * 获取子事物的描述者列表，返回本事物的所有描述者所定义的子事物的描述列表。
	 * 
	 * @return 描述者列表
	 */
	public List<Thing> getAllChildsDescriptors(){		
		List<Thing> childsDescriptors = new ArrayList<Thing>();
		
		for(Thing descriptor : getDescriptors()){
			UtilData.addToSource(childsDescriptors, descriptor.getAllChilds(Thing.THING), false);
		}
		
		return childsDescriptors;
	}
	
	/**
	 * 返回事物的所有继承事物列表，包括继承的继承...。
	 * 
	 * @return 所有继承的事物列表
	 */
	public List<Thing> getAllExtends(){
		List<Thing> extendList = new ArrayList<Thing>();
		
		Map<Thing, Object> context = new HashMap<Thing, Object>();
		Map<Thing, Object> extendCache = new HashMap<Thing, Object>();
		
		getAllExtends(this, extendList, context, extendCache);
		
		return extendList;
	}
	
	private void getAllExtends(Thing thing, List<Thing> extendList, Map<Thing, Object> context, Map<Thing, Object> extendCache){
		if(context.get(thing) == null){
			context.put(thing, thing);
			
			List<Thing> exts = thing.getExtends();
			for(Thing ext : exts){
				if(extendCache.get(ext) == null){
					extendList.add(ext);
					getAllExtends(ext, extendList, context, extendCache);
				}
			}
		}
	}
	
	/**
	 * 获得属性值。
	 * 
	 * @param name 属性名称
	 * @return 属性的值。
	 */
	public Object getAttribute(String name){
		Object value = attributes.get(name);
		if(value != null){
			return value;
		}else{
			return null;
			/*
			//判断是否是当前事物的属性，如果是返回null
			for(Thing attributeDescriptor : getAllAttributesDescriptors()){
				if(attributeDescriptor.getMetadata().getName().equals(name)){
					return null;
				}
			}
			
			//不是当前事物所定义的属性，从继承事物中取属性
			for(Thing extend : getAllExtends()){
				value = extend.attributes.get(name);
				if(value != null){
					return value;
				}
			}
			
			return null;*/
		}
	}
	
	private Thing getAttributeDescriptor(Map<Thing, Object> context, String name){
		if(context.get(this) == null){
			context.put(this, this);
			List<Thing> attributesDescriptors = getAllAttributesDescriptors();
			for(Thing attrDescriptor : attributesDescriptors){
				if(attrDescriptor.getMetadata().getName().equals(name)){
					return attrDescriptor;
				}
			}
			
			for(Thing extend : getExtends()){
				Thing attrDescriptor =  extend.getAttributeDescriptor(context, name);
				if(attrDescriptor != null){
					return attrDescriptor;
				}
			}
			
			return null;
		}else{
			return null;
		}
	}
	
	/**
	 * 根据指定的属性名称获取该属性的描述者。
	 * 
	 * @param name 属性名称
	 * @return 属性的描述者
	 */
	public Thing getAttributeDescriptor(String name){
		Map<Thing, Object> context = new HashMap<Thing, Object>();
		return getAttributeDescriptor(context, name);
	}
	
	/**
	 * 获得属性集合。
	 * 
	 * @return 属性集合
	 */
	public Map<String, Object> getAttributes(){
		return attributes;
	}
	
	/**
	 * <p>根获取事物属性描述列表，只返回第一个描述者的属性描述列表。</p>
	 * 
	 * @return 事物的属性描述列表
	 */
	public List<Thing> getAttributesDescriptors(){
		//取本事物的描述者列表
		List<Thing> selfDescriptors = getDescriptors();
		for(Thing descriptor : selfDescriptors){
			List<Thing> attrDescriptors = descriptor.getChilds(Thing.ATTRIBUTE);
			return attrDescriptors;
		}
		
		return Collections.emptyList();
	}
	
	public BigDecimal getBigDecimal(String name){
		return this.getBigDecimal(name, null);
	}
	
	public BigDecimal getBigDecimal(String name, BigDecimal defaultValue){
		return UtilData.getBigDecimal(getAttribute(name), defaultValue);
	}
	
	public BigDecimal getBigDecimal(String name, BigDecimal defaultValue, ActionContext actionContext) throws OgnlException{
		return UtilData.getBigDecimal(UtilData.getData(this, name, actionContext), defaultValue);
	}
	
	public BigInteger getBigInteger(String name){
		return getBigInteger(name, null);
	}
	
	public BigInteger getBigInteger(String name, BigInteger defaultValue){
		return UtilData.getBigInteger(getAttribute(name), defaultValue);
	}
	
	public BigInteger getBigInteger(String name, BigInteger defaultValue, ActionContext actionContext) throws OgnlException{
		return UtilData.getBigInteger(UtilData.getData(this, name, actionContext), defaultValue);
	}
	
	public boolean getBoolean(String name){
		return getBoolean(name, false);
	}
	
	public boolean getBoolean(String name, boolean defaultValue){
		return UtilData.getBoolean(getAttribute(name), defaultValue);
	}
	
	public boolean getBoolean(String name, boolean defaultValue, ActionContext actionContext) throws OgnlException{
		return UtilData.getBoolean(UtilData.getData(this, name, actionContext), defaultValue);
	}
	
	public byte getByte(String name){
		return getByte(name, (byte) 0);
	}
	
	public byte getByte(String name, byte defaultValue){
		return UtilData.getByte(getAttribute(name), defaultValue);
	}
	
	public byte getByte(String name, byte defaultValue, ActionContext actionContext) throws OgnlException{
		return UtilData.getByte(UtilData.getData(this, name, actionContext), defaultValue);
	}
	
	public byte[] getBytes(String name){
		return getBytes(name, null);
	}
	
	public byte[] getBytes(String name, byte[] defaultValue){
		return UtilData.getBytes(getAttribute(name), defaultValue);
	}
	
	public byte[] getBytes(String name, byte[] defaultValue, ActionContext actionContext) throws OgnlException{
		return UtilData.getBytes(UtilData.getData(this, name, actionContext), defaultValue);
	}
	
	public char getChar(String name){
		return getChar(name, (char) 0);
	}
	
	public char getChar(String name, char defaultValue){
		return UtilData.getChar(getAttribute(name), defaultValue);
	}
	
	public char getChar(String name, char defaultValue, ActionContext actionContext) throws OgnlException{
		return UtilData.getChar(UtilData.getData(this, name, actionContext), defaultValue);
	}
	
	/**
	 * 返回本事物的直接子事物，不包含继承的子事物。
	 * 
	 * @return 本事物的直接子事物
	 */
	public List<Thing> getChilds(){
		return childs;
	}
	 
	/**
	 * 根据描述者的名称来获取所有符合的子事物，不包括继承的子事物。
	 * 
	 * @param thingName 描述者的名称
	 * 
	 * @return 描述者的名称为指定名称的子事物
	 */
	public List<Thing> getChilds(String thingName){
		List<Thing> childThings = new ArrayList<Thing>();
		
		for(Thing child : getChilds()){
			if(child.isThingByName(thingName)){
				UtilData.addToSource(childThings, child, true);
			}
		}
		
		return childThings;
	}
	
	/**
	 * 获取子事物的描述者列表，只返回第一个描述者所定义的子事物的描述列表。
	 * 
	 * @return 子事物的描述者列表
	 */
	public List<Thing> getChildsDescriptors(){
		List<Thing> selfDescriptors = getDescriptors();
		return selfDescriptors.get(0).getAllChilds(Thing.THING);
	}
	
	public Date getDate(String name){
		return getDate(name, null);
	}
	
	public Date getDate(String name, Date defaultValue){
		return UtilData.getDate(getAttribute(name), defaultValue);
	}
	
	public Date getDate(String name, Date defaultValue, ActionContext actionContext) throws OgnlException{
		return UtilData.getDate(UtilData.getData(this, name, actionContext), defaultValue);
	}
	
	public Thing getClassThing(){
		return getDescriptor();
	}
	
	public List<Thing> getClasses(){
		return getDescriptors();
	}
	
	/**
	 * 返回事物的主要的描述者。
	 * 
	 * @return 第一个描述者
	 */
	public Thing getDescriptor(){
		return getDescriptors().get(0);
	}
	
	/**
	 * <p>返回本事物定义的的所有描述者的列表。</p>
	 * 
	 * <p>一个事物的描述者列表是在属性descriptors里定义的，如果有多个那么之间用,号隔开。另外元事物是所有的
	 * 事物的基本描述，一般在描述者列表中元事物被放到了列表的末端</p>
	 * 
	 * <p>如果事物的一个描述者继承了其他事物，那么继承的其他事物也是这个事物的描述者，这样的描述者在此方法里
	 * 不能获得，如想取得事物的所有包含描述者继承的描述者，那么可以是用getAllDescriptors()方法。</p>
	 * 
	 * @see getAllDescriptors
	 * @return 本事物的所有描述者的列表
	 */
	public List<Thing> getDescriptors(){
		World world = World.getInstance();
		List<Thing> descriptors = new ArrayList<Thing>();
		
		if(descriptorsCaches == null || descriptorsCaches.length == 0){
			initDescriptors();
		}
		
		for(int i=0; i<descriptorsCaches.length; i++){
			ThingEntry entry = descriptorsCaches[i];
			if(entry != null){
				Thing thing = entry.getThing();
				
				if(thing == null){
					removeDescriptorsCache(i);
				}else{
					//UtilData.addToSource(descriptors, thing, true);
					descriptors.add(thing);
				}
			}
		}
		
		if(world.baseClass != null){
			descriptors.add(world.baseClass);
		}
		
		return descriptors;
	}
	
	/**
	 * <p>返回本事物所有的描述者列表，包括描述者继承的事物。</p>
	 * 
	 * 与getDescriptors方法不同的是getDescriptors方法只返回自身定义的描述者的列表。
	 * 
	 * @see getDescriptors
	 * @return 包含描述者继承的事物所有的描述者列表
	 */
	public List<Thing> getAllDescriptors(){
		//添加自身定义的描述者
		List<Thing> descriptors = new ArrayList<Thing>();				
		List<Thing> extendsList = new ArrayList<Thing>();
		Map<Thing, Object> caches = new HashMap<Thing, Object>();
		Map<Thing, Object> context = new HashMap<Thing, Object>();
		
		if(descriptorsCaches == null || descriptorsCaches.length == 0){
			this.initDescriptors();
		}
		
		for(int i=0; i<descriptorsCaches.length; i++){
			ThingEntry entry = descriptorsCaches[i];	
				if(entry != null){
				Thing thing = entry.getThing();
				if(thing == null){
					removeDescriptorsCache(i);
				}else{
					descriptors.add(thing);
					
					//context.put(thing, thing);
					caches.put(thing, thing);
					
					thing.getAllExtends(thing, extendsList, context, caches);
				}
			}
		}		
		
		for(Thing desc : extendsList){
			descriptors.add(desc);
		}
		
		if(caches.get(World.getInstance().baseClass) == null){
			//元事物是每个事物的描述者
			descriptors.add(World.getInstance().baseClass);
		}
		
		return descriptors;
	}
	
	public double getDouble(String name){
		return getDouble(name, 0);
	}
	
	public double getDouble(String name, double defaultValue){
		return UtilData.getDouble(getAttribute(name), defaultValue);
	}
	
	public double getDouble(String name, double defaultValue, ActionContext actionContext) throws OgnlException{
		return UtilData.getDouble(UtilData.getData(this, name, actionContext), defaultValue);
	}
	
	/**
	 * 返回本事物的继承事物列表。
	 * 
	 * @return 继承事物列表
	 */
	public List<Thing> getExtends() {
		List<Thing> extendList = new ArrayList<Thing>();
		
		if(extendsCaches  == null){
			initExtends();
		}
		
		for(int i=0; i<extendsCaches.length; i++){
			ThingEntry entry = extendsCaches[i];
			
			if(entry != null){
				Thing thing = entry.getThing();
				if(thing == null){
					removeExtendsCache(i);					
				}else{
					extendList.add(thing);
				}
			}
		}
				
		return extendList;		
	}
	
	public float getFloat(String name){
		return getFloat(name, 0);
	}
	
	public float getFloat(String name, float defaultValue){
		return UtilData.getFloat(getAttribute(name), defaultValue);
	}
	
	public float getFloat(String name, float defaultValue, ActionContext actionContext) throws OgnlException{
		return UtilData.getFloat(UtilData.getData(this, name, actionContext), defaultValue);
	}
	
	public int getInt(String name){
		return getInt(name, 0);
	}
	
	public int getInt(String name, int defaultValue){
		int value = UtilData.getInt(getAttribute(name), defaultValue);
		//BigInteger bi = UtilData.getBigInteger(getAttribute(name), null);
		//if(bi == null){
	    //	return defaultValue;
		//}else{
		//	return bi.intValue();
		//}
		return value;
	}
	
	public int getInt(String name, int defaultValue, ActionContext actionContext) throws OgnlException{
		return UtilData.getInt(UtilData.getData(this, name, actionContext), defaultValue);
	}
	
	/**
	 * 从事物中取指定的属性的字符串的值作为变量名，然后从ActionContext中取变量，支持var:或ognl:，默认相当于 var:。
	 * @param name 属性名
	 * @param actionContext 变量上下文 
	 * @return 值
	 * @throws OgnlException 异常 
	 */
	public Object getObject(String name, ActionContext actionContext) throws OgnlException{		
		Object value = this.get(name);
		if(value != null && value instanceof String){
			String str = (String) value;
			if(str.startsWith("var:")){
				return actionContext.get(str.substring(4, str.length()));
			}else if(str.startsWith("ognl:")){
				return OgnlUtil.getValue(this, name, actionContext);
			}else if(str.startsWith("thing:")){
				String thingPath = str.substring(6, str.length());
				return World.getInstance().getThing(thingPath);
			}else{
				if("".equals(str)){
					return null;
				}
				
				return actionContext.get(str);
			}
		}
		
		return value;
	}
	
	public long getLong(String name){
		return getLong(name, 0);
	}
	
	public long getLong(String name, long defaultValue){
		return UtilData.getLong(getAttribute(name), defaultValue);
	}
	
	public long getLong(String name, long defaultValue, ActionContext actionContext) throws OgnlException{
		return UtilData.getLong(UtilData.getData(this, name, actionContext), defaultValue);
	}
	
	/**
	 * 获得本事物的元数据。
	 * 
	 * @return 当前事物的元数据
	 */
	public ThingMetadata getMetadata(){
		return metadata;
	}
		
	private Object getNullReturn(String path){
    	if(path.endsWith("@")){
    		return Collections.emptyList();
    	}else{
    		return null;
    	}
    }
	
	private Object getNullReturn(Path path){
		while(path.getChildPath() != null){
			path = path.getChildPath();
		}
		
		if(path.getType() == Path.TYPE_CHILD_THINGS){
			return Collections.emptyList();
		}else{
			return null;
		}
	}
	
	/**
	 * 返回本事物的父事物。
	 * 
	 * @return 父事物，如果没有返回null
	 */
	public Thing getParent(){
		return parent;
	}
	
	/**
	 * 返回本事物的最根级（顶层）父事物，如果当前事物已经是根事物，那么返回自身。
	 * 
	 * @return 根父事物
	 */
	public Thing getRoot(){
		Map<Thing, Object> context = new HashMap<Thing, Object>();
		
		if(parent == null){
			return this;
		}
		
		Thing currentParent = parent;			
		while(true){
			if(context.get(currentParent) != null){
				return currentParent;
			}else{
				if(currentParent.parent == null){
					return currentParent;
				}
				
				context.put(currentParent, parent);
				currentParent = currentParent.parent;				
			}
		}		
	}
	
	public short getShort(String name){
		return getShort(name, (short) 0);
	}
	
	public short getShort(String name, short defaultValue){
		return UtilData.getShort(getAttribute(name), defaultValue);
	}
	
	public short getShort(String name, short defaultValue, ActionContext actionContext) throws OgnlException{
		return UtilData.getShort(UtilData.getData(this, name, actionContext), defaultValue);
	}
	
	public String getString(String name){
		return getString(name, null);
	}
	
	/**
	 * 获取字符串，trim()后的空字符串也返回null。
	 * 
	 * @param name 属性名
	 * @return 结果
	 */
	public String getStringBlankAsNull(String name){
		String attr = getString(name);
		if(attr != null){
			attr = attr.trim();
		}
		
		if(attr == null || "".equals(attr)){
			return null;
		}else{
			return attr;
		}
	}
	
	public String getString(String name, String defaultValue){
		return UtilData.getString(getAttribute(name), defaultValue);
	}
	
	/**
	 * 通过属性值从上下文中取字符串，通过UtilString获取字符串，如果返回null或空，那么返回defaultValue。
	 * 
	 * @param name 属性名
	 * @param defaultValue 默认值
	 * @param actionContext 变量上下文
	 * @return 值
	 */
	public String getString(String name, String defaultValue, ActionContext actionContext){
		String value = UtilString.getString(this, name, actionContext);
		if(value == null || "".equals(value)){
			return defaultValue;
		}else{
			return value;
		}
	}
	
	/**
	 * 通过指定的子事物的路径获取一个子事物。
	 * 
	 * @param childThingPath 子事物的路径
	 * @return 子事物
	 */
	public Thing getThing(String childThingPath){
		Object obj = get(childThingPath);
		if(obj instanceof Thing){
			return (Thing) obj;
		}else{
			return null;
		}
	}
	
	/**
	 * 返回属性所指向的引用的事物，如果不存在替换为根事物的路径当前的根事物开始寻找。
	 * 
	 * 有的时候一个属性的值是一个所引用的事物的路径，由于模型引擎总是使用全路径，
	 * 这样如果引用时本事物根事物的某个字事物，并且根事物拷贝到了其它目录下或改名了，
	 * 那么这个引用就会失效。
	 * 
	 * 通过使用此方法可以避免这个问题，并且属性的值会重设，如果引用的事物同属一个根
	 * 事物的话。
	 * 
	 * @param attribute 属性名称
	 * @return 所引用的事物
	 */
	public Thing getQuotedThing(String attribute){
		return UtilThing.getQuoteThing(this, attribute);
	}
	
	/**
	 * 返回事物的事物名，相当于Java对象的类名。
	 * 
	 * @return 事物名
	 */
	public String getThingName(){
		List<Thing> descriptors = getDescriptors();
		return descriptors.get(0).getMetadata().getName();
	}
	
	/**
	 * 返回事物的所有事物名。
	 * 
	 * @return 事物名
	 */
	public List<String> getThingNames(){
		if(thingNames.size() == 0){
			initThingNames();
		}
		
		return thingNames;
	}
	
	private synchronized void initDescriptors(){
		World world = World.getInstance();
		if(world == null){
			return;
		}
		
		thingNames.clear();
		
		List<ThingEntry> tempList = new ArrayList<ThingEntry>();
		String descriptorsNamesStr = (String) attributes.get(Thing.DESCRIPTORS);
		if(descriptorsNamesStr != null){
			String descriptorsNames[] = UtilString.split(descriptorsNamesStr, ','); //.split("[,]");
			for(String descriptorName : descriptorsNames){
				Thing descriptor = world.getThing(descriptorName);
				if(descriptor != null){
					tempList.add(new ThingEntry(descriptorName, descriptor));
				}
			}
		}		
		
		ThingEntry[] tmp  = new ThingEntry[tempList.size()];
		tmp = tempList.toArray(tmp);
		descriptorsCaches = tmp;
		
		//也重新清除动作缓存
		actionCaches.clear();
	}
	
	/**
     * 初始化默认值。
     *
     */
    public void initDefaultValue(){   	
    	beginModify();
    	try{
	        List<Thing> fields = this.getAllAttributesDescriptors();        
	        for(Iterator<Thing> iter = fields.iterator(); iter.hasNext();){
	        	Thing field = iter.next();
	        	String name = field.getMetadata().getName();
        		Object value = this.getAttribute(name);
        		if(value == null || "".equals(value)){
        			String defaultValue = field.getString("default");
        			if(defaultValue != null && !"".equals(defaultValue)){	        			        		
	        			attributes.put(name, defaultValue);
	        			
	        			String type = field.getStringBlankAsNull("type");
	        			if(type != null){
	        				UtilData.resetAttributeByType(this, name, type);
	        			}
	        		}else if(!this.getAttributes().containsKey(name)){
	        			//这个代码应该会产生大量的空值，会占用内存
	        			//attributes.put(name, null);
	        		}
	        	}
	        }
	        
	        //设置默认的名字属性，不要在这里默认初始化名字属性，因为有些已存在的事物name允许为空
	        /*
	        if(attributes.get(NAME) == null){
	        	Thing descriptor = this.getDescriptor();
	        	if(descriptor != null){
	        		attributes.put(NAME, descriptor.get(NAME));
	        	}
	        }*/
    	}finally{
    		endModify(true);
    	}
    }
    
	private void initExtends(){
		World world = World.getInstance();
		if(world == null){
			return;
		}
		extendsCaches = null;
		
		List<ThingEntry> tempList = new ArrayList<ThingEntry>();
		String extendsNamesStr = (String) attributes.get(Thing.EXTENDS);
		if(extendsNamesStr != null){
			String extendsNames[] = UtilString.split(extendsNamesStr, ',');//.split("[,]");
			for(String extendName : extendsNames){
				if(extendName.equals("_root") && getRoot() != this){
					tempList.add(new ThingEntry(extendName, getRoot()));
				}else{
					Thing descriptor = world.getThing(extendName);
					if(descriptor != null){
						tempList.add(new ThingEntry(extendName, descriptor));
					}
				}
			}
		}		
		
		extendsCaches = new ThingEntry[tempList.size()];
		extendsCaches = tempList.toArray(extendsCaches);		
	}
	
	private void initThingNames(){
		thingNames.clear();
		List<Thing> descriptors = getAllDescriptors();
		
		for(Thing descriptor : descriptors){
			boolean have = false;
			for(String name : thingNames){
				if(name.equals(descriptor.getMetadata().getName())){
					have = true;
					break;
				}
			}
			
			if(!have){
				thingNames.add(descriptor.getMetadata().getName());
			}
		}
		
		for(Thing extend : getAllExtends()){
			boolean have = false;
			for(String name : thingNames){
				if(name.equals(extend.getMetadata().getName())){
					have = true;
					break;
				}
			}
			
			if(!have){
				thingNames.add(extend.getMetadata().getName());
			}
		}
	}
	
	/**
	 * 重新初始化所有子事物的路径。
	 */
	public void initChildPath(){
		for(Thing child : childs){
			initChildMetadata(child);
		}
	}
	
	/**
	 * 初始化子事物的元数据。
	 * 
	 * @param child 子事物
	 */
	protected void initChildMetadata(Thing child){
		ThingMetadata childMeta = child.getMetadata();
		childMeta.setCategory(metadata.getCategory());
		if(getRoot() == this){
			childMeta.setPath(metadata.getPath() + "/@" + childMeta.getId());
		}else{
			childMeta.setPath(metadata.getPath() + "/@" + childMeta.getId());
		}
		
		for(Thing childChild : child.getChilds()){
			child.initChildMetadata(childChild);
		}
	}
	
	private boolean isThing(Map<Thing, Object> context, Thing descriptor){
		if(descriptor == null){
			return false;
		}
		
		if(context.get(this) == null){
			context.put(this, this);
			
			for(Thing mydescriptor : getAllDescriptors()){
				if(descriptor.getMetadata().getPath().equals(mydescriptor.getMetadata().getPath())){
					return true;
				}
			}
			
			/*
			for(Thing extend : getAllExtends()){
				if(extend.isThing(context, descriptor)){
					return true;
				}
			}*/
		}
		
		return false;
	}
	
	/**
	 * 根据指定的描述者来判断该事物是否是这个指定描述者的所描述的事物。
	 * 类似Java的instanceof的作用。
	 * 
	 * @param descriptorPath 描述者的路径
	 * @return 是否是描述者所描述的事物
	 */
	public boolean isThing(String descriptorPath){
		Thing descriptor = World.getInstance().getThing(descriptorPath);
		
		return isThing(descriptor);
	}
	
	/**
	 * 根据指定的描述者来判断该事物是否这个指定描述者所描述的事物。
	 * 类似Java的instanceof的作用。
	 * 
	 * @param descriptor 描述者
	 * @return 是否这个描述者所描述的事物
	 */
	public boolean isThing(Thing descriptor){
		Map<Thing, Object> context = new HashMap<Thing, Object>();
		
		return isThing(context, descriptor);
	}
		
	/**
	 * 根据描述者的名称返回当前事物是否是指定的事物，此判定方法并非严格。
	 * 类似Java的instanceof的作用。
	 * 
	 * @param descriptorName 描述者的名称
	 * @return 是否是该事物
	 */
	public boolean isThingByName(String descriptorName){
		List<Thing> allDescriptors = this.getAllDescriptors();
		for(int i=0; i<allDescriptors.size() - 1; i++){
			Thing descriptor = allDescriptors.get(i);
			if(descriptor.getMetadata().getName().equals(descriptorName)){
				return true;
			}
		}
		
		return false;
		/*
		//long start = System.currentTimeMillis();
		if(descriptorsCaches == null || descriptorsCaches.length == 0){
			initDescriptors();
		}
		
		if(descriptorsCaches == null){
			return false;
		}
		
		for(int i=0; i<descriptorsCaches.length; i++){
			ThingEntry entry = descriptorsCaches[i];
			if(entry != null){
				Thing thing = entry.getThing();
				if(thing == null){
					removeDescriptorsCache(i);					
				}else{
					if(thing.getMetadata().getName().equals(descriptorName)){
						return true;
					}
				}
			}
		}
		
		//System.out.println("Thing 1461 isThingByName : " + (System.currentTimeMillis() - start));
		return false;	
		*/ 
	}
	
	private synchronized void removeDescriptorsCache(int index){
		ThingEntry[] temp = new ThingEntry[descriptorsCaches.length - 1];
		for(int i=0; i<descriptorsCaches.length; i++){
			if(i < index){
				temp[i] = descriptorsCaches[i];
			}else if(i > index){
				temp[i - 1] = descriptorsCaches[i];
			}
		}
		
		descriptorsCaches = temp;
	}
	
	private synchronized void removeExtendsCache(int index){
		ThingEntry[] temp = new ThingEntry[extendsCaches.length - 1];
		for(int i=0; i<extendsCaches.length; i++){
			if(i < index){
				temp[i] = extendsCaches[i];
			}else if(i > index){
				temp[i - 1] = extendsCaches[i];
			}
		}
		
		extendsCaches = temp;
	}
	
	/**
	 * 使用新的事物来覆盖当前事物。
	 * 
	 * @param thing
	 */
	public void replace(Thing thing){
		this.attributes.clear();		
		this.childs.clear();
		
		this.paste(thing);
		this.initChildPath();
	}
	
	/**
	 * 返回本事物是否是瞬态的。
	 * 
	 * @return 是否是瞬态的
	 */
	public boolean isTransient(){
		return this.isTransient;
	}
	
	/**
	 * 返回遍历所有的子节点遍历器。
	 * 
	 * @return 子节点遍历器
	 */
	public Iterator<Thing> getChildsIterator(){
		//添加自己的子节点和所有的继承的子节点
		final List<List<Thing>> allChilds = new ArrayList<List<Thing>>();
		allChilds.add(childs);
		for(Thing extend : getAllExtends()){
			allChilds.add(extend.getChilds());			
		}
		
		return new Iterator<Thing>(){
			int currentIndex = 0;
			int currentSubIndex = 0;
			Thing nextChild = null;
			List<Thing> currentList = null;
			boolean inited = false;
			boolean hasNext = false;
			
			public boolean hasNext() {	
				if(!inited){
					initCurrent();
					inited = true;
				}
				
				return hasNext;
			}

			public void initCurrent(){
				if(currentList == null || currentSubIndex == currentList.size()){
					if(allChilds.size() == currentIndex){
						nextChild = null;
						hasNext = false;
						return;
					}else{
						currentList = allChilds.get(currentIndex);
						currentIndex ++;
						currentSubIndex = 0;
					}
				}
				
				if(currentSubIndex < currentList.size()){
					nextChild = currentList.get(currentSubIndex);
					hasNext = true;
				}else{
					initCurrent();
				}
			}
			
			public Thing next() {
				initCurrent();
				inited = true;
				
				Thing next = nextChild;
				currentSubIndex++;
				
				initCurrent();
				return next;
			}

			public void remove() {				
			}			
		};
	}
	
	/**
	 * 设置属性的值。
	 * 
	 * @param name 属性名称
	 * @param value 属性的值
	 * @return 值
	 */
	public Object put(Object name, Object value){
		if(name == null){
			return null;
		}
		
		//使用名字缓存，试图减少一些内存占用
		String key = name.toString();
		String k = nameCache.get(key);
		if(k == null){
			k = key;
			nameCache.put(k, k);
		}
		attributes.put(k, value);
		
		if(name.equals(Thing.DESCRIPTORS)){
			initDescriptors();
			
			actionCaches.clear();
		}
		
		if(name.equals(Thing.EXTENDS)){
			initExtends();
			
			actionCaches.clear();
		}
		
		updateLastModified();
		
		return value;
	}
	
	/**
	 * 放入Map的全部值，但不触发descriptor和extends的改变事件，同时也会更新日期。
	 * 
	 * @param values 值
	 */
	public void putAll(Map<String, Object> values){
		attributes.putAll(values);
		
		updateLastModified();
	}
	
	/**
	 * 设置属性。
	 * 
	 * @param name 属性名
	 * @param value 值
	 */
	public void set(Object name, Object value){
		put(name, value);
	}
	
	public void setParent(Thing parent){
		this.parent = parent;
		if(parent != null){
			this.getMetadata().setPath(parent.getMetadata().getPath() + "/@" + this.getMetadata().getId());
		}
	}
	
	/**
	 * 从本事物的描述者列表中移除指定的描述者。
	 * 
	 * @param descriptor 描述者
	 */
	public void removeDescriptor(Thing descriptor){
		if(descriptor == null){
			return;
		}
		
		String descriptors = (String) attributes.get(Thing.DESCRIPTORS);
		String descriptorPath = descriptor.getMetadata().getPath();
		
		if(descriptors.indexOf(descriptorPath) != -1){
			descriptors = descriptors.replaceAll("(" + descriptorPath + ",)" , "");
		}
		
		if(descriptors.indexOf(descriptorPath) != -1){
			descriptors = descriptors.replaceAll("(," + descriptorPath + ")" , "");
		}
		
		if(descriptors.indexOf(descriptorPath) != -1){
			descriptors = descriptors.replaceAll("(" + descriptorPath + ")" , "");
		}
		
		put(Thing.DESCRIPTORS, descriptors);
	}
	
	/**
	 * 删除指定的子事物。
	 * 
	 * @param child 要删除的子事物
	 */
	public void removeChild(Thing child){
		if(childs.contains(child)){
			child.getMetadata().setRemoved(true);
		}
		
		child.setParent(null);
		childs.remove(child);			
		updateLastModified();
	}	
	
	/**
	 * 删除调用自身的事物管理者删除自己，设置自身的状态为已删除。
	 *
	 *@return 是否成功
	 */
	public boolean remove(){
		if(this.getParent() != null){
			this.getParent().removeChild(this);
			return true;
		}else{
			metadata.setRemoved(true);
			updateLastModified();
			
			ThingCache.remove(metadata.getPath());
			ThingManager manager = metadata.getThingManager();
			if(manager != null){
				return manager.remove(this);
			}else{
				return false;
			}
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		this.metadata.removed = true;
		ThingCache.remove(metadata.getPath());
	}

	/**
	 * 从本事物的继承列表中移除指定的继承事物。
	 * 
	 * @param extend 继承的事物
	 */
	public void removeExtend(Thing extend){
		if(extend == null){
			return;
		}
		
		String extendstr = (String) attributes.get(Thing.EXTENDS);
		String extendPath = extend.getMetadata().getPath();
		
		if(extendstr.indexOf(extendPath) != -1){
			extendstr = extendstr.replaceAll("(" + extendPath + ",)" , "");
		}
		
		if(extendstr.indexOf(extendPath) != -1){
			extendstr = extendstr.replaceAll("(," + extendPath + ")" , "");
		}
		
		if(extendstr.indexOf(extendPath) != -1){
			extendstr = extendstr.replaceAll("(" + extendPath + ")" , "");
		}
		
		put(Thing.EXTENDS, extendstr);
	}
	
	/**
	 * 保存自己，通常是调用事物所在的事物管理者来保存。
	 *
	 * @return 是否成功
	 */
	public synchronized boolean save(){
		Thing root = this.getRoot();
		ThingManager manager = root.getMetadata().getThingManager();
		if(manager != null){
			if(root.getMetadata().getPath() == null || "".equals(root.getMetadata().getPath())){
				root.getMetadata().initPath();
			}
			
			
			boolean saved = manager.save(root);
			
			//如果不在缓存中，加入到缓存中
			if(ThingCache.get(root.getMetadata().getPath()) == null){
				ThingCache.put(root.getMetadata().getPath(), this);
			}
			
			return saved;
		}else{
			return false;
		}
	}
	
	/**
	 * 把自己按照指定的路径保存到指定的目录下，如果目标事物存在那么会替换目标事物。
	 * 
	 * @param thingManager 事物管理器
	 * @param path 事物路径
	 */
	public void saveAs(String thingManager, String path){
		Thing thing = this.getRoot();
		String oldPath = thing.getMetadata().getPath();
		int dotIndex = path.lastIndexOf(".");
		String thingName = null;
		String category = null;
		if(dotIndex == -1){
			category = "";
			thingName = path;
		}else{
			category = path.substring(0, dotIndex);
			thingName = path.substring(dotIndex + 1, path.length());
		}
		
		ThingManager manager = World.getInstance().getThingManager(thingManager);		
		if(manager != null){
			if(!"_transient".equals(thingManager) && (thing.isTransient() || this.isTransient())){
				//如果不是瞬态的事物管理器，也修改为非瞬态的状态
				this.setTransient(false);
			}
			
			Category cat = manager.getCategory(category);
			if(cat == null){
				manager.createCategory(category);				
				cat = manager.getCategory(category);
			}
			thing.getMetadata().setCategory(cat);
			thing.getMetadata().setReserve(thingName);
			//thing.getMetadata().setReserve(root.getMetadata().getReserve());
			thing.getMetadata().initPath();
			if(thing.getMetadata().getCoderType() == null || "".equals(thing.getMetadata().getCoderType())){
				thing.getMetadata().setCoderType(TxtThingCoder.TYPE);
			}
			thing.initChildPath();
			
			//替换属性路径
			ThingUtil.replaceThing(thing, oldPath, path);
			manager.save(thing);
			
			//清除已有的事物缓存，使事物重新加载
			ThingCache.remove(path);
		}else{
			throw new XMetaException("Thing manager not exists, name=" + thingManager);
		}
	}
	
	/**
	 * 把自己拷贝一个新的事物到指定的事物管理器的指定目录下。
	 * 
	 * @param thingManager 事物管理器
	 * @param category 目录
	 * @return 事物
	 */
	public Thing copyTo(String thingManager, String category){
		Thing root = this.getRoot();
		Thing thing = root.detach();
		ThingManager manager = World.getInstance().getThingManager(thingManager);		
		if(manager != null){
			Category cat = manager.getCategory(category);
			if(cat == null){
				manager.createCategory(category);				
				cat = manager.getCategory(category);
			}
			thing.getMetadata().setCategory(cat);
			thing.getMetadata().setReserve(root.getMetadata().getReserve());
			thing.getMetadata().initPath();
			thing.getMetadata().setCoderType(root.getMetadata().getCoderType());
			thing.initChildPath();
			
			
			manager.save(thing);
			return thing;
		}else{
			return null;
		}
	}
		
	/**
	 * beginBigModifiy必须和endBigModify成对出现。
	 *
	 */
	public void beginModify(){
		Thread currentThread = Thread.currentThread();
		Stack<Thread> stk = updatingThreads.get(currentThread);
		if(stk == null){
			stk = new Stack<Thread>();
			stk.push(currentThread);
			
			updatingThreads.put(currentThread, stk);
		}else{
			stk.push(currentThread);
		}
	}
	
	public void endModify(boolean change){
		Thread currentThread = Thread.currentThread();
		Stack<Thread> stk = updatingThreads.get(currentThread);
		if(stk == null){
			return;
		}else{
			stk.pop();
			
			if(stk.size() == 0){
				updatingThreads.remove(currentThread);
				
				if(change){
					//System.out.println("modified");
				    updateLastModified();
				}
			}
		}
	}
	
	private void updateLastModified(){
		Thread currentThread = Thread.currentThread();
		Stack<Thread> stk = updatingThreads.get(currentThread);
		if(stk != null && stk.size() > 0){
			//当前线程还在修改数据，不需要更新
			return;
		}
	    
		Map<Thing, Object> context = new HashMap<Thing, Object>();
		 
		long lastModified = System.currentTimeMillis();		 
		this.getMetadata().setLastModified(lastModified);
		context.put(this, this);
		 
		//更新父事物的最后更新时间
		Thing parent = getParent();
		while(parent != null && context.get(parent) == null){
			parent.getMetadata().setLastModified(lastModified);
			context.put(parent, parent);
			parent = parent.getParent();
		}
		 
		//更新子事物的最后更新时间
		for(Thing child : getChilds()){
			changeChildLastModified(child, context, lastModified);
		}
	}
	
	private void changeChildLastModified(Thing child, Map<Thing, Object> context, long lastModified){
		if(context.get(child) == null){
			child.getMetadata().setLastModified(lastModified);
			context.put(child, child);
			
			for(Thing childChild : child.getChilds()){
				changeChildLastModified(childChild, context, lastModified);
			}
		}
	}
	
	public String toString(){
		return "Thing{name=" + getMetadata().getName() +
			",label=" + getMetadata().getLabel() + 
			",path=" + getMetadata().getPath() + ",descriptios=" + getString("descriptors") + "}";
	}
	
	public void setData(String key, Object data){
		if(key == null){
			return;
		}
	
		synchronized(this){
			if(datas == null){
				datas = new HashMap<String, Object>();
			}	
		}
		
		datas.put(key, data);
	}
	
	public Object getData(String key){
		if(datas == null){
			return null;
		}
		
		return datas.get(key);
	}
	
	/**
	 * 返回保存Data数据的Map，有可能返回null，如果没有初始化（放过数据）。
	 * @return 保存的值
	 */
	public Map<String, Object> getDatas(){
		return datas;
	}
	
	/**
	 * 设置缓存数据，如果事物在后面修改了，那么缓存失效。
	 * 
	 * @param key key
	 * @param data 数据
	 */
	public void setCachedData(String key, Object data){
		String timeKey = "__" + key + "__Modified__";
		datas.put(key, data);
		datas.put(timeKey, getMetadata().getLastModified());
	}
	
	/**
	 * 获取缓存的数据。
	 * 
	 * @param key 键
	 * @return 值
	 */
	public Object getCachedData(String key){
		String timeKey = "__" + key + "__Modified__";
		Object data = datas.get(key);
		Long lastTime = (Long) datas.get(timeKey);
		if(lastTime == null || lastTime != getMetadata().getLastModified()){
			return null;
		}else{
			return data;
		}
	}

	public void setTransient(boolean isTransient) {
		setTransient(isTransient, new HashMap<Thing, Thing>());
	}
	
	private void setTransient(boolean isTransient, Map<Thing, Thing> context){
		if(context.get(this) != null){
			return;
		}
		
		context.put(this, this);
		this.isTransient = isTransient;
		
		for(Thing child : childs){
			child.setTransient(isTransient, context);
		}
	} 
	
	@SuppressWarnings("unchecked")
	public <T> T getObject(String key){
		return (T) get(key);
	}
	
	/**
	 * 返回指定索引位置的子节点，如果超出范围返回null。
	 * 
	 * @param index
	 * @return 如果超出子节点的范围，返回null，否则返回对应的子节点。
	 */
	public Thing getChildAt(int index){
		if(index < 0 || index >= childs.size()){
			return null;
		}else{
			return childs.get(index);
		}
	}
	
	/**
	 * 获取参考子节点的相对位置的子节点，首先找到参考子节点的位置索引，然后和目标index相加获得最终的索引位置。
	 * 所以如果index小于0，是指获取参考节点的索引更小的子节点，如果index大于0，则是获取后面的节点。
	 * 
	 * @param refChild
	 * @param index
	 * @return 如果超出子节点的范围或参考子节点不是当前事物的子节点返回null，否则返回对应的子节点。
	 */
	public Thing getChildBy(Thing refChild, int index){
		int rindex = childs.indexOf(refChild);
		if(rindex == -1){
			return null;
		}else{
			return getChildAt(index + rindex);
		}
	}
}