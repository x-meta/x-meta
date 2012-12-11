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
	 * @param action
	 * @param caller
	 * @param parameters
	 * @param namoTime
	 * @param successed
	 */
	public void actionExecuted(Action action, Object caller, ActionContext acitonContext,  Map<String, Object> parameters, long namoTime, boolean successed);
}
