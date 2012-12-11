package org.xmeta.ui.session;

import java.util.HashMap;
import java.util.Map;

/**
 * 会话管理者，表示的是UI交互中的会话，还需进一步设计。
 * 
 * @author zyx
 *
 */
public class SessionManager {
	static Map<String, Session> sessions = new HashMap<String, Session>();
	
	public static Session getSession(String name) {
		Session session = sessions.get(name);
		if(session == null){
			session = new Session();
			sessions.put(name, session);
		}
		
		return session;
	}
	
	public static void remove(String name){
		sessions.remove(name);
	}
}
