package org.xmeta.cache;

import org.xmeta.Thing;

/**
 * 对象和模型的绑定。当模型发生变更时重新生成对象。
 * 
 * @author zhangyuxiang
 *
 * @param <T>
 */
public abstract class ThingObjectEntry<T> {
	ThingEntry thingEntry;
	T object;
	
	public ThingObjectEntry(Thing thing) {
		thingEntry = new ThingEntry(thing);		
	}
	
	public T getObject() {
		if(object == null || thingEntry.isChanged()) {
			object = createObject();
			thingEntry.getThing();
		}
		
		return object;
	}
	
	protected abstract T createObject();
}
