/*
    X-Meta Engine。
    Copyright (C) 2013  zhangyuxiang

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
    For alternative license options, contact the copyright holder.

    Emil zhangyuxiang@tom.com
 */
package org.xmeta;

import java.util.Timer;
import java.util.TimerTask;

public class XMetaTimerManager {
	private static Timer timer = new Timer("X-Meta engine timer", true);
	
	/**
	 * 注册定时器。
	 * 
	 * @param task
	 * @param delay
	 * @param period
	 */
	public static void schedule(TimerTask task, long delay, long period){
		timer.schedule(task, delay, period);
	}
}