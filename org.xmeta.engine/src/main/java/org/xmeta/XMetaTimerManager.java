package org.xmeta;

import java.util.Timer;
import java.util.TimerTask;

public class XMetaTimerManager {
	private static Timer timer = new Timer("X-Meta engine timer");
	
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
