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
 * <p>事物事件的监听者接口，主要用来监听一个事物的改动事件和删除事件。</p>
 * 
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public interface ThingListener {
	/**
	 * 事物改变后触发的事件。
	 * 
	 * @param thing 发生改变的事物
	 */
	public void changed(Thing thing);
	
	/**
	 * 事物已删除后的触发的事件，此方法是调用事物自身的remove()方法后才触发的事件。
	 * 
	 * @param thing 事物被删除后触发的事件
	 */
	public void removed(Thing thing);
}