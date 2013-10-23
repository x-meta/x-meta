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