package org.xmeta.ui.session;

import java.util.HashMap;
import java.util.Map;

import org.xmeta.ActionContext;

public class DefaultSessionManager extends SessionManager{
	private Map<String, Session> sessions = new HashMap<String, Session>();
	
	@Override
	public Session get(String name, ActionContext actionContext) {
		Session session = sessions.get(name);
		if(session == null){
			session = new DefaultSession();
			sessions.put(name, session);
		}
		
		return session;
	}

	@Override
	public Session delete(String name, ActionContext actionContext) {
		return sessions.remove(name);
	}

}
