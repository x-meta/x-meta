package org.xmeta.ui.session;

import org.xmeta.ActionContext;

public class DefaultSessionManager extends SessionManager{
	private Session session = new DefaultSession();
	
	@Override
	public Session get(ActionContext actionContext) {
		return session;
	}

	@Override
	public Session delete(ActionContext actionContext) {
		return session;
	}

	@Override
	public boolean accept(ActionContext actionContext) {
		return true;
	}

}
