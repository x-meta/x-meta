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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
	
	/**
	 * 返回所有以目录为主的类库路径。
	 * 
	 * @return
	 */
	public List<String> getAlClassPathDirs(){
		Map<String, String> dirContext = new HashMap<String, String>();
		for(String path : getClassPath().split("[" + File.pathSeparator + "]")){
			if(path.trim().equals("")){
				continue;
			}
			
			File file = new File(path);
			if(file.exists() && file.isDirectory()){
				dirContext.put(file.getPath(), file.getPath());				
			}
		}
		
		List<String> jars = new ArrayList<String>();
		for(String path : dirContext.keySet()){
			jars.add(path);
		}
		
		Collections.sort(jars);
		return jars;
	}
	
	/**
	 * 返回所有的Jar名称列表。
	 * 
	 * @return
	 */
	public List<String> getAllJarsByName(){
		Map<String, String> jarContext = new HashMap<String, String>();
		for(String path : getClassPath().split("[" + File.pathSeparator + "]")){
			if(path.toLowerCase().endsWith(".jar")){
				int index = -1;
				if(path.indexOf("/") != -1){
					index = path.lastIndexOf("/");
				}else{
					index = path.lastIndexOf("\\");
				}
				
				if(index != -1){
					path = path.substring(index + 1, path.length());
				}
				
				jarContext.put(path, path);
			}
		}
		
		List<String> jars = new ArrayList<String>();
		for(String path : jarContext.keySet()){
			jars.add(path);
		}
		
		Collections.sort(jars);
		return jars;
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