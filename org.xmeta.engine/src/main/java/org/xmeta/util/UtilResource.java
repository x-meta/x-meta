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
package org.xmeta.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 多语言资源工具，现在通过事物本身来解决,如UtilString.get("label:thingpath");。
 * 
 * @author zhangyuxiang
 *
 */
public class UtilResource {
	private static Logger log = LoggerFactory.getLogger(UtilResource.class);
	
	static Map<String, UtilResource> resouceCache = new HashMap<String, UtilResource>();
	Map<String, Resources> cache = new HashMap<String, Resources>();
	Locale locale = null;
	
	public static UtilResource getInstance(Locale locale){
		if(locale == null){
			locale = Locale.getDefault();
		}
		
		UtilResource res = resouceCache.get(locale.toString());
		if(res == null){
			res = new UtilResource(locale);
			resouceCache.put(locale.toString(), res);
		}
		
		return res;
	}
	
	public static UtilResource getDefaultInstance(){
		Locale locale = null;//(Locale) SessionManager.getSession(null).getAttribute("xmeta_current_user_locale");
		if(locale == null){
			locale = Locale.getDefault();
			//SessionManager.getSession(null).setAttribute("xmeta_current_user_locale", locale);
		}
		
		UtilResource resource = UtilResource.getInstance(locale);
		return resource;
	}
	
	public UtilResource(Locale locale){
		this.locale = locale;
	}
	
	public Resources get(String name){
		Resources rs = (Resources) cache.get(name);
		if(rs == null){
			try{
				rs = new Resources(name, locale);
				cache.put(name, rs);
			}catch(Exception e){
				if(log.isWarnEnabled()){
					log.warn("找不到资源:" + name);
				}
				//e.printStackTrace();
			}
		}
		
		return rs;
	}
	
	public String get(String resourceName, String name, String defaultValue){
		Resources rs = get(resourceName);
		if(rs == null){
			return defaultValue;
		}else{
			return rs.get(name, defaultValue);
		}
	}
	
	public static void clear(){	
		try{
			for(String key : resouceCache.keySet()){
				UtilResource ur = resouceCache.get(key);
				ur.clearCache();
			}
			//ResourceBundle.clearCache();
			//resouceCache.clear();
		}catch(Exception e){
			log.warn("i18n resource clear error", e);
		}			
	}
	
	public void clearCache(){
		for(String key : cache.keySet()){
			Resources r = cache.get(key);
			r.clear();
		}
	}
}