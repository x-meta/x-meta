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
package org.xmeta.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ExceptionUtil {
	private static final String[] CAUSE_METHOD_NAMES = { "getCause",
			"getNextException", "getTargetException", "getException",
			"getSourceException", "getRootCause", "getCausedByException",
			"getNested", "getLinkedException", "getNestedException",
			"getLinkedCause", "getThrowable", };
    
	/**
	 * 获取第一个Cause的消息。
	 * 
	 * 有时候异常被捕获的多次并包装了多次，取第一个原始的异常信息。
	 * 
	 * @param t 异常
	 * @return 异常的字符串
	 */
	public static String getRootMessage(Throwable t){
		String message = t.getMessage();
		Throwable cause = t;
		while((cause = cause.getCause()) != null){
			message = cause.getMessage();
		}
		
		return message;
	}
	
	/**
	 * 返回异常的根异常。
	 * 
	 * @param t 异常
	 * @return 最初始的根异常
	 */
	public static Throwable getRootCause(Throwable t){
		Throwable cause = t;
		while(true){
			Throwable c = getCause(cause);
			if(c != null){
				cause = c;
			}else{
				break;
			}
		}
		
		return cause;
	}
	
	public static Throwable getCause(Throwable t){
		for(String methodName : CAUSE_METHOD_NAMES){
			Throwable th = getCauseUsingMethodName(t, methodName);
			if(th != null){
				return th;
			}
		}
		
		return null;
	}

	private static Throwable getCauseUsingMethodName(final Throwable throwable,
			final String methodName) {
		Method method = null;
		try {
			method = throwable.getClass().getMethod(methodName);
		} catch (final NoSuchMethodException ignored) { // NOPMD
			// exception ignored
		} catch (final SecurityException ignored) { // NOPMD
			// exception ignored
		}

		if (method != null
				&& Throwable.class.isAssignableFrom(method.getReturnType())) {
			try {
				return (Throwable) method.invoke(throwable);
			} catch (final IllegalAccessException ignored) { // NOPMD
				// exception ignored
			} catch (final IllegalArgumentException ignored) { // NOPMD
				// exception ignored
			} catch (final InvocationTargetException ignored) { // NOPMD
				// exception ignored
			}
		}
		return null;
	}
	
	public static String toString(Throwable t){
		StringBuffer text = new StringBuffer();
		toString(t, text);
		return text.toString();
	}
	
	private static void toString(Throwable t, StringBuffer text){
		text.append(t.toString());
		text.append("\n");
		for(StackTraceElement st : t.getStackTrace()){			
			text.append("\tat ");
			String line = st.getClassName() + "." + st.getMethodName() + "(";
			line = line + st.getFileName() + ":" + st.getLineNumber();
			text.append(line);
			
            text.append(")\n");
		}

		Throwable cause = t.getCause();
		if(cause != null){
			toString(cause, text);
		}
	}
}