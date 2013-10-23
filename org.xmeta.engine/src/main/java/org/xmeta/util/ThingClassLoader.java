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