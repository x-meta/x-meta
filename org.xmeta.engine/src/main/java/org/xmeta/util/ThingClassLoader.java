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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import org.xmeta.World;

public class ThingClassLoader extends URLClassLoader {
	
	/**
	 * 构建一个世界级的类装载器。
	 * 
	 * @param parent
	 */
	public ThingClassLoader(ClassLoader parent) {
		super(new URL[] {}, parent);

		if(parent instanceof URLClassLoader){
			
		}
		File libFile = new File(World.getInstance().getPath() + "/lib/");
		addJarOrZip(libFile);
		
//		initClassPath();
	}

	/**
	 * 从一个目录或者jar或zip添加类库。
	 * 
	 * @param dir
	 */
	public void addJarOrZip(File dir){
		if(!dir.exists()){
			return;
		}
	
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				addJarOrZip(file);
			} else {
				String fileName = file.getName().toLowerCase();
				if (fileName.endsWith(".jar") || fileName.endsWith(".zip")) {
					try {
						this.addURL(file.toURI().toURL());
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public String getClassPath() {
		Map<String, String> context = new HashMap<String, String>();
		return getClassPathFormClassLoader(this, "", context);
	}
	
	public static String getClassPathFormClassLoader(URLClassLoader loader, String classPath, Map<String, String> context){		
		for(URL url : loader.getURLs()){
			String filePath = url.getFile();
			if(filePath != null && context.get(filePath) == null){
				context.put(filePath, filePath);
				classPath = classPath + File.pathSeparator + filePath;
			}
		}
		
		ClassLoader parent = loader.getParent();
		if(parent instanceof URLClassLoader){
			classPath = getClassPathFormClassLoader((URLClassLoader) parent, classPath, context);
		}
		
		return classPath;
	}
	
	public String getCompileClassPath() {
		return getClassPath();
	}
}