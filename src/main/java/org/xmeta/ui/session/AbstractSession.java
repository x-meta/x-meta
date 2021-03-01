package org.xmeta.ui.session;

import java.util.Locale;

import org.xmeta.util.UtilResource;

public abstract class AbstractSession implements Session{
	protected Locale locale = Locale.getDefault();
	protected UtilResource i18nResource = UtilResource.getInstance(locale);
	
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
		i18nResource = UtilResource.getInstance(locale);
	}
}
