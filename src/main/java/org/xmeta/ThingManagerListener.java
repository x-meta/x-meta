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

/**
 * <p>事物管理者的事件监听者接口。 </p>
 * 
 * 通过事物的方法getMetadata().getThingManager()可以获得触发事件的事物管理者。
 *
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public interface ThingManagerListener {
	/**
	 * 当一个事物被载入时触发此事件。
	 * 
	 * @param thingManager 事物管理器
	 * @param thing 载入的事物
	 */
	public void loaded(ThingManager thingManager, Thing thing);
	
	/**
	 * 当一个事物被保存时触发此事件。
	 * 
	 * @param thingManager 事物管理器
	 * @param thing 被保存的事物
	 */
	public void saved(ThingManager thingManager, Thing thing);
	
	/**
	 * 当一个事物被删除是触发此事件。
	 * 
	 * @param thingManager 事物管理器
	 * @param thing 被删除的事物
	 */
	public void removed(ThingManager thingManager, Thing thing);
}