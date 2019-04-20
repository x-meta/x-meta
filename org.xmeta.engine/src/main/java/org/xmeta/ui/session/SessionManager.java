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

import java.util.ArrayList;
import java.util.List;

import org.xmeta.ActionContext;

/**
 * 会话管理者，表示的是UI交互中的会话，还需进一步设计。
 * 
 * 一个系统里可能会存在多种会话环境，比如HTTP，NETTY，RAP等等，判断当前属于哪一种会话的方法如下：
 * 1.是否设置了ThreadLocal<SessionManager>，如HTTP和NETTY的情况。
 * 2.遍历所有已注册的SessionManager，当有SessionManager.accept(actionContext)时使用。
 * 3.使用默认的defaultSessionManager。
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
	 * 会话管理器的列表。
	 */
	private static List<SessionManager> sessionManagers = new ArrayList<SessionManager>();
	
	/**
	 * 会话管理器Local。
	 */
	private static ThreadLocal<SessionManager> sessionManagerLocal = new ThreadLocal<SessionManager>();

	/**
	 * 获取默认环境的会话，是本地会话，公用一个Session。
	 * 
	 * @param actionContext
	 * @return
	 */
	public static Session getSession(ActionContext actionContext) {
		return getSessionManager(actionContext).get(actionContext);
	}
	
	public static Session remove(ActionContext actionContext){
		return getSessionManager(actionContext).delete(actionContext);
	}
	
	public static void registSessionManager(SessionManager sessionManager) {		
		if(!sessionManagers.contains(sessionManager)) {
			sessionManagers.add(sessionManager);
		}
		//sessionManagers.put(env, sessionManager);
		//SessionManager.defaultSessionManager = sessionManager;
	}
	
	public static List<SessionManager> getSessionManagers(){
		return sessionManagers;
	}
	
	/**
	 * 根据环境返回会话管理器，如果不存在返回默认的会话管理器。
	 * 	
	 * @param env
	 * @return
	 */
	public static SessionManager getSessionManager(ActionContext actionContext) {
		SessionManager sessionManager = sessionManagerLocal.get();
		if(sessionManager != null) {
			return sessionManager; 
		}
		
		for(SessionManager sm : sessionManagers) {
			if(sm.accept(actionContext)) {
				return sm;
			}
		}
		
		return defaultSessionManager;		
	}
	
	public static void setLocalSessionManager(SessionManager sessionManager) {
		sessionManagerLocal.set(sessionManager);
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
	
	/**
	 * 是否接受当前环境，即会话管理器是否是当前环境的会话管理器。
	 * 
	 * @return
	 */
	public abstract boolean accept(ActionContext actionContext);
	
}