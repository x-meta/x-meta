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

import java.util.Map;

import ognl.ClassResolver;

import org.xmeta.World;

/**
 * 一般启动后Ognl找不到world的lib下的静态类。
 * 
 * @author zhangyuxiang
 *
 */
public class OgnlClassResolver implements ClassResolver{
	private static OgnlClassResolver instance = new OgnlClassResolver();
	
	public static OgnlClassResolver getInstance(){
		return instance;
	}
	
    @SuppressWarnings("unchecked")
	public Class<?> classForName(String className, Map context) throws ClassNotFoundException{
    	return World.getInstance().getClassLoader().loadClass(className);
    }
}