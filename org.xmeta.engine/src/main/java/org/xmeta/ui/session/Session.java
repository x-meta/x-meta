package org.xmeta.ui.session;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.xmeta.util.UtilResource;

public class Session {
	Locale locale = Locale.getDefault();
	UtilResource i18nResource = UtilResource.getInstance(locale);
	
	Map<String, Object> values =  new HashMap<String, Object>();

	public Object getAttribute(String name) {
		return values.get(name);
	}

	public void setAttribute(String name, Object value) {
		values.put(name, value);
	}

	public UtilResource getI18nResource() {
		return i18nResource;
	}

	public void setI18nResource(UtilResource resource) {
		i18nResource = resource;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;		
	}
}
