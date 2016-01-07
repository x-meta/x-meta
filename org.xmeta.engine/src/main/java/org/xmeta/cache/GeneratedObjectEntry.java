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
	 * @param actionContext 变量上下文
	 * @return 对象
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