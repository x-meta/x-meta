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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.xmeta.World;

public class JavaCompiler15 {
	static World world = World.getInstance();
	
	@SuppressWarnings("unchecked")
	public static void compile(String classPath, String fileName) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		Class mainClass = Class.forName("com.sun.tools.javac.Main");
		Method compileMethod = mainClass.getDeclaredMethod("compile", new Class[]{String[].class});
		compileMethod.invoke(mainClass, new Object[]{new String[]{
				"-cp", classPath,
				"-d", world.getPath() + "/actionClasses",
				fileName + ".java"
		}});
	}
}