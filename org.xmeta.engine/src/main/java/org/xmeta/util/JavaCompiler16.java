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
		
	public static boolean compile(String classPath, String sourcePath, File codeFile, String targetDir){
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if(compiler != null){
			StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

			Iterable<? extends JavaFileObject> compilationUnits1 =
			fileManager.getJavaFileObjectsFromFiles(Arrays.asList(codeFile));
			
			String bootClass = System.getProperty("sun.boot.class.path");
			String classPath_ = System.getProperty("java.class.path");
			String cp = bootClass + File.pathSeparator + classPath + File.pathSeparator + classPath_;
			List<String> options = null;
			if(sourcePath != null && !"".equals(sourcePath)){
				options = Arrays.asList("-cp", cp, "-sourcepath", sourcePath, "-d", targetDir);
			}else{
				options = Arrays.asList("-cp", cp, "-d", targetDir);
			}
			compiler.getTask(null, fileManager, null, options, null, compilationUnits1).call();
			
			return true;
		}else{
			return false;
		}
	}
	
}