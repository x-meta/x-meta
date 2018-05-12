package org.xmeta.ui.session;

import java.util.HashMap;
import java.util.Map;

public class DefaultSession extends AbstractSession {
	Map<String, Object> values =  new HashMap<String, Object>();
	
	public Object getAttribute(String name) {
		return values.get(name);
	}

	public void setAttribute(String name, Object value) {
		values.put(name, value);
	}
}
