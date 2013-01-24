package org.xmeta.cache;

import org.xmeta.ActionContext;
import org.xmeta.Thing;

/**
 * 构造出来的对象缓存实体，对象是通过已有的事物通过给定的方法创建出来的。
 * 当第一次获取生成的对象或当原事物发生变化后会重新生成。
 * 
 * @author Administrator
 *
 */
public class GeneratedObjectEntry {
	ThingEntry thingEntry;
	String method;
	String path;
	Object generatedThing = null;
	long lastmodified = 0;

	/**
	 * 构造函数。
	 * 
	 * @param thing 原事物
	 * @param method 生成新事物的方法名
	 */
	public GeneratedObjectEntry(Thing thing, String method){
		thingEntry = new ThingEntry(thing);
		this.method = method;
	}
	
	/**
	 * 获取生成的对象。
	 * 
	 * @param actionContext
	 * @return
	 */
	public Object getObject(ActionContext actionContext){
		Thing thing = thingEntry.getThing();
		if(generatedThing == null || lastmodified != thing.getMetadata().getLastModified()){
			generatedThing = thing.doAction(method, actionContext);
			lastmodified = thing.getMetadata().getLastModified();
		}
		
		return generatedThing;
	}
}
