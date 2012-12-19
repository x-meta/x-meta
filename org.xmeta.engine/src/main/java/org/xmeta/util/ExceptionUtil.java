package org.xmeta.util;

public class ExceptionUtil {
	/**
	 * 获取第一个Cause的消息。
	 * 
	 * 有时候异常被捕获的多次并包装了多次，取第一个原始的异常信息。
	 * 
	 * @param t
	 * @return
	 */
	public static String getRootMessage(Throwable t){
		String message = t.getMessage();
		Throwable cause = t;
		while((cause = cause.getCause()) != null){
			message = cause.getMessage();
		}
		
		return message;
	}
}
