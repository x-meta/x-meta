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

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.xmeta.World;

public class JavaCompiler16 {
	static World world = World.getInstance();
		
	public static boolean compile(String classPath, File codeFile){
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if(compiler != null){
			StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

			Iterable<? extends JavaFileObject> compilationUnits1 =
			fileManager.getJavaFileObjectsFromFiles(Arrays.asList(codeFile));
			
			List<String> options = Arrays.asList("-cp", classPath, "-d", world.getPath() + "/actionClasses");
			compiler.getTask(null, fileManager, null, options, null, compilationUnits1).call();
			
			return true;
		}else{
			return false;
		}
	}
	
}