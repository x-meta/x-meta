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

import java.util.Timer;
import java.util.TimerTask;

public class XMetaTimerManager {
	private static Timer timer = new Timer("X-Meta engine timer", true);
	
	/**
	 * 注册定时器。
	 * 
	 * @param task 定时器任务
	 * @param delay 延迟
	 * @param period 周期
	 */
	public static void schedule(TimerTask task, long delay, long period){
		timer.schedule(task, delay, period);
	}
}