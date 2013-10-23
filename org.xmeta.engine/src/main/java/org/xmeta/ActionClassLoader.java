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
package org.xmeta;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * 动作事物的类装载器，每个动作都有一个自己的类装载器实例。
 * 
 * @author zyx
 *
 */
public class ActionClassLoader extends URLClassLoader {
	public ActionClassLoader(ClassLoader parent) {		
		super(new URL[] {}, parent);
		
		File classPath = new File(World.getInstance().getPath() + "/actionClasses/");
		try {
			if(!classPath.exists()){
				classPath.mkdirs();
			}
			
			super.addURL(classPath.toURI().toURL());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
}