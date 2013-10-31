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