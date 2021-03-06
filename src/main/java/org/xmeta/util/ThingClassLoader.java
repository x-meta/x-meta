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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xmeta.Thing;
import org.xmeta.World;

public class ThingClassLoader extends URLClassLoader {
	//private static Logger logger = LoggerFactory.getLogger(ThingClassLoader.class);
	private static Logger logger = Logger.getLogger(ThingClassLoader.class.getName());
	
	/** 未初始化的类库，在类库中以.lib为后缀名的类库配置文件 */
	List<Lib> libs = new ArrayList<Lib>();
	
	public ThingClassLoader(URL[] urls){
		super(urls);
	}
	
	public ThingClassLoader(URL[] urls, ClassLoader parent){
		super(urls, parent);
	}
	
	/**
	 * 构建一个世界级的类装载器。
	 * 
	 * @param parent 父类装载器
	 */
	public ThingClassLoader(ClassLoader parent) {
		super(new URL[] {}, parent);

		World world = World.getInstance();
		String worldPath = world.getPath();
		//优先加载的类库
		for(String lib : world.getLibList()) {
			File libFile = new File(lib);
			addJarOrZip(libFile);
		}
		
		File libFile = new File(worldPath + "/lib/");
		addJarOrZip(libFile);
		
		addJarOrZip(new File(worldPath + "/os/lib/lib_" + world.getOS()));
		addJarOrZip(new File(worldPath + "/os/lib/lib_" + world.getOS() + "_" + world.getJVMBit()));
		
		//System.out.println(new File(worldPath + "/lib_" + world.getOS()).getAbsolutePath());
		//System.out.println(new File(worldPath + "/lib_" + world.getOS() + "_" + world.getJVMBit()).getAbsolutePath());
		//System.out.println(this.getClassPath());
//		initClassPath();
	}
	
	/**
	 * 初始化化lib文件中的类库，在world初始化的最后初始化。
	 */
	public void initLibs(){
		Thing config = World.getInstance().getThing("_local.xworker.config.GlobalConfig");

		while(libs.size() > 0){
			Lib lib = libs.remove(0);
			String filePath = lib.path;
			while(true){
				int index1 = filePath.indexOf("'");
				if(index1 != -1){
					int index2 = filePath.indexOf("'", index1 + 1);
					if(index2 != -1){
						String replaceFor = filePath.substring(index1 + 1, index2);
						
						String replacement = config != null ? config.getString(replaceFor) : "";
						if(replacement == null){
							replacement = "";
						}
						
						filePath = replaceAll(filePath, replaceFor, replacement);
					}else{
						break;
					}
				}else{
					break;
				}				
			}
			
			File file = new File(filePath);
			if("lib".equals(lib.type)){
				addJarOrZip(file);
			}else{
				if(file.exists()){
					try {
						this.addURL(file.toURI().toURL());
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
		
	private String replaceAll(String str, String replaceFor, String replacement){
		String fstr = str;
		while(true){
			int index1 = fstr.indexOf("'" + replaceFor);
			if(index1 != -1){
				int index2 = fstr.indexOf(replaceFor + "'");
				if(index2 != -1){
					fstr = fstr.substring(0, index1) + replacement + fstr.substring(index2 + replaceFor.length() + 1, fstr.length());
				}else{
					break;
				}
			}else{
				break;
			}
		}
		
		return fstr;
	}

	/**
	 * 从一个目录或者jar或zip添加类库。
	 * 
	 * @param dir 目录
	 */
	public void addJarOrZip(File dir){
		if(!dir.exists()){
			return;
		}
	
		if(dir.isDirectory()){
			for (File file : dir.listFiles()) {
				addJarOrZip(file);
			}
		}else{
			String fileName = dir.getName().toLowerCase();
			if (fileName.endsWith(".jar") || fileName.endsWith(".zip")) {
				try {
					this.addURL(dir.toURI().toURL());
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}else if(fileName.endsWith(".lib")){
				//logger.info("init lib from file: " + dir.getAbsolutePath());
				initLib(dir);
			}
		}
	}

	public void addClassPath(URL url){
		if(url != null){
			this.addURL(url);
		}
	}
	
	private void initLib(File file){
		FileInputStream fin = null;
		try{
			fin = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fin));
			String line = null;
			while((line = br.readLine()) != null){
				line = line.trim();
				String[] ls = line.split("[=]");
				if(ls.length == 2){
					ls[0].trim();
					ls[1].trim();
					if(!"".equals(ls[0])){
						if(!("lib".equals(ls[0]) || "path".equals(ls[0]))){
							logger.info("unkown lib type, only 'lib' or 'path' suppored, line=" + line + ",libFile=" + file.getAbsolutePath());
						}else{
							libs.add(new Lib(ls[0], ls[1]));
						}
					}
				}
			}
			br.close();
		}catch(Exception e){
			logger.log(Level.WARNING, "init lib error", e);
		}finally{
			try {
				if(fin != null){
					fin.close();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public String getClassPath() {
		Map<String, String> context = new HashMap<String, String>();
		
		String javaClassPath = System.getProperty("java.class.path");		
		
		String path = getClassPathFormClassLoader(this, "", context);
		return javaClassPath + File.pathSeparator + path;
	}
	
	/**
	 * 返回所有以目录为主的类库路径。
	 * 
	 * @return 类路径列表
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
	 * @return jar列表
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
			if(filePath != null && context.get(filePath) == null && !"".equals(filePath)){
				context.put(filePath, filePath);
				if(classPath == null){
					classPath = filePath;
				}else{
					classPath = classPath + File.pathSeparator + filePath;
				}
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
	
	static class Lib{
		String type;
		String path;
		
		public Lib(String type, String path){
			this.type = type;
			this.path = path;
		}
	}
}