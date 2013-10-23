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