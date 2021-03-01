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
package org.xmeta.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.xmeta.World;

public class JavaCompiler15 {
	static World world = World.getInstance();
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void compile(String classPath, String sourcePath, String fileName, String targetDir) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		Class mainClass = Class.forName("com.sun.tools.javac.Main");
		Method compileMethod = mainClass.getDeclaredMethod("compile", new Class[]{String[].class});
		if(sourcePath != null && !"".equals(sourcePath)){
			compileMethod.invoke(mainClass, new Object[]{new String[]{
					"-cp", classPath,
					"-sourcepath", sourcePath, 
					"-d", targetDir,
					fileName + ".java"
			}});
		}else{
			compileMethod.invoke(mainClass, new Object[]{new String[]{
					"-cp", classPath,
					"-d", targetDir,
					fileName + ".java"
			}});
		}
	}
}