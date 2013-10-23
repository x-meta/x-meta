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

import java.util.Locale;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmeta.World;

/**
 * @author zhangyuxiang
 *
 */
public class Resources {
	private static Logger log = LoggerFactory.getLogger(Resources.class);
	
	ResourceBundle messages;
	Locale locale;
	String name;
	ClassLoader bundleLoader;
	
	public Resources(String name, Locale local){
		this.name = name;
		if(local == null){
			local = Locale.getDefault();
		}
		
		this.locale = local;
		messages = ResourceBundle.getBundle(name, local, World.getInstance().getClassLoader());
		bundleLoader = World.getInstance().getClassLoader();
	}
	
	public String get(String name){
		try{
			return messages.getString(name);
		}catch(Exception e){
			//e.printStackTrace();
			if(log.isDebugEnabled()){
				log.debug("锟斤拷锟斤拷源" + this.name + "锟揭诧拷锟斤拷锟斤拷源锟斤拷息锟斤拷" + name);
			}
			return null;
		}
	}
	
	public String get(String name, String defaultValue){
		String message = get(name);
		
		if(message == null){
			return defaultValue;
		}else{
			return message;
		}
	}
	
	public void clear(){
		ResourceBundle.clearCache(bundleLoader);
	}
}