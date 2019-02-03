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

import java.util.HashMap;
import java.util.Map;

import org.xmeta.ActionContext;

/**
 * 会话管理者，表示的是UI交互中的会话，还需进一步设计。
 * 
 * @author zyx
 *
 */
public abstract class SessionManager {
	/**
	 * 默认的会话管理器。
	 */
	private static SessionManager defaultSessionManager = new DefaultSessionManager();
	
	/**
	 * 和环境相关的会话管理器。 如WEB、RAP等。
	 */
	private static Map<String, SessionManager> sessionManagers = new HashMap<String, SessionManager>();
	static {
		//本地会话管理器
		sessionManagers.put("DEFAULT", defaultSessionManager);
	}

	/**
	 * 通过指定的环境获取会话。
	 * 
	 * @param env
	 * @param actionContext
	 * @return
	 */
	public static Session getSession(String env, ActionContext actionContext) {		
		return getSessionManager(env).get(actionContext);
	}
	
	/**
	 * 获取默认环境的会话，是本地会话，公用一个Session。
	 * 
	 * @param actionContext
	 * @return
	 */
	public static Session getSession(ActionContext actionContext) {
		return getSessionManager(null).get(actionContext);
	}
	
	public static Session remove(String env, ActionContext actionContext){
		return getSessionManager(env).delete(actionContext);
	}
	
	public static void setSessionManager(String env, SessionManager sessionManager) {
		sessionManagers.put(env, sessionManager);
		//SessionManager.defaultSessionManager = sessionManager;
	}
	
	/**
	 * 根据环境返回会话管理器，如果不存在返回默认的会话管理器。
	 * 	
	 * @param env
	 * @return
	 */
	public static SessionManager getSessionManager(String env) {
		SessionManager sessionManager = sessionManagers.get(env);
		if(sessionManager != null) {
			return sessionManager;
		}else {
			return defaultSessionManager;
		}
	}
	
	/**
	 * 返回默认的会话管理器。
	 * 
	 * @return
	 */
	public static SessionManager getDefaultSessionManager() {
		return defaultSessionManager;
	}
	
	/**
	 * 获取一个指定的会话，其中name可以为null，如果Session不存在那么创建一个。。
	 * 
	 * @param name
	 * @return
	 */
	public abstract Session get(ActionContext actionContext);
	
	/**
	 * 删除一个会话。
	 * 
	 * @param name
	 * @return
	 */
	public abstract Session delete(ActionContext actionContext);
	
}