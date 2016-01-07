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

import java.util.Map;

/**
 * 动作监听器。
 * 
 * @author Administrator
 *
 */
public interface ActionListener {
	/**
	 * 动作已被执行之后触发的事件。
	 * 
	 * 由于当动作执行完时调用显示的顺序正好是堆栈的相反顺序（因为调用者总是后完成），为了
	 * 改变这种情况，改成在动作执行前调用，因此时间和是否成功参数就无意义了。
	 * 
	 * @param action 动作
	 * @param caller 调用者
	 * @param parameters 参数
	 * @param namoTime 微秒
	 * @param successed 是否成功
	 * @param actionContext 变量上下文
	 */
	public void actionExecuted(Action action, Object caller, ActionContext actionContext,  Map<String, Object> parameters, long namoTime, boolean successed);
	
	/**
	 * 增加了事物动作的监听。
	 * 
	 * @param thing 事物
	 * @param method 方法
	 * @param actionContext 变量上下文
	 * @param parameters 参数
	 * @param namoTime 微秒
	 * @param successed 是否成功
	 */
	public void actionExecuted(Thing thing, String method, ActionContext actionContext,  Map<String, Object> parameters, long namoTime, boolean successed);
}