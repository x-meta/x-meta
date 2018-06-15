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
package org.xmeta.ui.session;

import org.xmeta.ActionContext;

/**
 * 会话管理者，表示的是UI交互中的会话，还需进一步设计。
 * 
 * @author zyx
 *
 */
public abstract class SessionManager {
	private static SessionManager sessionManager = new DefaultSessionManager();

	public static Session getSession(String name, ActionContext actionContext) {
		return sessionManager.get(name, actionContext);
	}
	
	public static Session remove(String name, ActionContext actionContext){
		return sessionManager.delete(name, actionContext);
	}
	
	public static Session getSession(ActionContext actionContext) {
		return getSession(null, actionContext);
	}
	
	public static void setSessionManager(SessionManager sessionManager) {
		SessionManager.sessionManager = sessionManager;
	}
	
	public static SessionManager getSessionManager() {
		return sessionManager;
	}
	
	/**
	 * 获取一个指定的会话，其中name可以为null，如果Session不存在那么创建一个。。
	 * 
	 * @param name
	 * @return
	 */
	public abstract Session get(String name, ActionContext actionContext);
	
	/**
	 * 删除一个会话。
	 * 
	 * @param name
	 * @return
	 */
	public abstract Session delete(String name, ActionContext actionContext);
	
}