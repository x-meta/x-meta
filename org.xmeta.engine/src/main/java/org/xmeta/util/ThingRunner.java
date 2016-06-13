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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmeta.ActionContext;
import org.xmeta.Thing;
import org.xmeta.ThingCoder;
import org.xmeta.World;
import org.xmeta.thingManagers.FileThingManager;

/**
 * 执行指定事物的run方法。
 * 
 * @author zhangyuxiang
 * 
 */
public class ThingRunner {
	private static final Logger logger = LoggerFactory.getLogger(ThingRunner.class);
	
	public static final String working_project = "working_directory";
	public static void main(String args[]){
		run(args);
	}
	
	public static boolean loadXerFromJar(Properties p , File fileName){
		try{
			JarFile jarFile = new JarFile(fileName);
			try{
				 JarEntry entry = jarFile.getJarEntry("dml.ini");
				 if(entry == null){
					 return false;
				 }
				 
				 InputStream in = jarFile.getInputStream(entry);
				 p.load(in);
				 
				 return true;
			}finally{
				jarFile.close();
			}
		}catch(Exception e){
			return false;
		}
	}
	
	public static void run(String args[]) {
		try {
			//运行参数
			String worldPath = null;
			String thingPath = null;
			String actionName = null;
			
			//优先从输入参数中取
			if(args.length >= 1){
				worldPath = args[0];
			}
			if(args.length >= 2){
				thingPath = args[1];
			}
			if(args.length >= 3){
				actionName = args[2];
			}
			
			//读取dml.ini的参数配置
			Properties p = new Properties();
			File jarFile = null;
			if("-jar".equals(thingPath)){
				jarFile = new File(actionName);
			}
			if("-jar".equals(thingPath) && jarFile.exists() && loadXerFromJar(p, jarFile)){		
				thingPath = null;
				actionName = null;
			}
			
			if(worldPath == null || thingPath == null){				
				//从文件中读取
				String xerFileName = "dml.ini";
				File xerFile = new File(xerFileName);
				if(!xerFile.exists()){
					xerFile = new File(worldPath + "/dml.ini");
				}
				if(xerFile.exists()){
					FileInputStream fin = new FileInputStream(xerFile);
					p.load(fin);
					fin.close();
				}
				
				if(worldPath == null && thingPath == null && actionName == null){
					actionName = p.getProperty("actionName");
				}
				if(worldPath == null){
					worldPath = p.getProperty("worldPath");
				}
				if(thingPath == null){
					thingPath = p.getProperty("thingPath");
				}				
			}
			
			if(actionName == null){
				actionName = "run";
			}

			System.out.println("world path : " + worldPath);
			System.out.println("thing path : " + thingPath);
			System.out.println("action name : " + actionName);
			
			World world = World.getInstance();
			world.init(worldPath);
			if(jarFile != null && jarFile.exists()){
				world.getClassLoader().addJarOrZip(jarFile);
			}
			
			for(String arg : args){
				if(arg.toLowerCase().equals("-verbose")){
					world.setVerbose(true);
				}				
			}
			
			boolean isFile = false;
			File thingFile = new File(thingPath);
			if(thingFile.exists()){
				//打开的事物是一个文件
				Info info = getThingManagerAndThingInfo(thingFile, thingPath);
				if(info != null){
					thingPath = info.thingPath;
				}else{
					thingPath = getThingPath(null, thingFile, false);
					addWorkingDirAsThingManager(world, thingFile.getParentFile());
				}
				
				if(thingPath == null){
					System.out.println("Cann't open, file is no a thing, file=" + thingPath);
				}
				isFile = true;
			}else{
				//打开的是事物的路径
				addWorkingDirAsThingManager(world, new File("."));
			}			
			
			if(isFile){
				//如果是文件，那么可以选择是编辑还是运行
				System.out.print("编辑请按回车键，否则3秒后运行该事物(If edit press enter key)：");
				WaiterForEnter we = new WaiterForEnter();
				we.start();
				long timestart = System.currentTimeMillis();
				boolean edit = false;
				while(System.currentTimeMillis() - timestart < 3000){
					if(we.ctrPressed){
						edit = true;
						break;
					}
				}
				we.close();
				
				if(edit){
					if(editThing(thingFile.getAbsolutePath())){
						return;
					}
				}
			}
			
			//执行事物
			Thing thing = world.getThing(thingPath);
			if (thing == null) {
				System.out.println("thing not exists : " + thingPath);
				System.exit(0);
			} else {
				ActionContext actionContext = new ActionContext();
				actionContext.put("args", args);
				thing.doAction(actionName, actionContext);
			}			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private static boolean editThing(String thingPath){		
		Thing globalConfig = World.getInstance().getThing("_local.xworker.config.GlobalConfig");
		if(globalConfig == null){
			System.out.println("XWorker has not run thing explorer, run thing......");
			return false;
		}
		
		try{
			String baseUrl = globalConfig.getString("webUrl") + "do?sc=xworker.ide.worldExplorer.swt.http.IDETools";
			URL checkIde = new URL(baseUrl + "&ac=isIdeOpened");
			URLConnection urlcon = checkIde.openConnection();
			InputStream in = urlcon.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String content = reader.readLine();
			if(!"true".equals(content)){
				System.out.println("XWorker has not run thing explorer, run thing......");
				return false;
			}
			
			URL openThing =  new URL(baseUrl + "&ac=oepenThing&path=" + URLEncoder.encode(thingPath, "utf-8"));
			openThing.openConnection().getContent();
			
			return true;
		}catch(Exception e){
			System.out.println("Exception happend, run thing......, " + e.getLocalizedMessage());
			return false;
		}
	}
	
	public static void addWorkingDirAsThingManager(World world, File dir) throws IOException{
		if(!dir.isDirectory()){
			return;
		}
		
		File worldDir = new File(world.getPath());
		String path = dir.getCanonicalPath();
		String worldPath = worldDir.getCanonicalPath();

		if(path.equals(worldPath)){
			//不能把world目录添加成事物管理器
			return;
		}

		String name = "tempdir_" + dir.getName() + "_" + dir.getAbsolutePath().hashCode();
		if(world.getThingManager(name) == null){
			boolean things = new File(dir, "xworker.properties").exists();
			FileThingManager working = new FileThingManager(name, dir, things);
			world.addThingManagerFirst(working);
		}
	}
		
	/**
	 * 现在X-Meta引擎支持直接打开系统中的DML文件并运行，此时传入的可能事物路径而是文件名了，
	 * 这是需要找出事物的真正路径和项目路径。
	 * 
	 * @param thingPath 事物路径
	 * 
	 * @return 事物管理器和事物的路径信息
	 */
	public static Info getThingManagerAndThingInfo(File file, String thingPath){
		if(file == null || !file.exists()){
			return null;
		}
				
		//.dmlprj和xworker.properties都是xworker项目的标志
		if(file.isDirectory()){
			File prj = new File(file, ".dmlprj");
			if(prj.exists()){
				return new Info(thingPath, file, true, prj);
			}
			
			prj = new File(file, "xworker.properties");
			if(prj.exists()){
				return new Info(thingPath, file, false, prj);
			}
		
			return getThingManagerAndThingInfo(file.getParentFile(), thingPath);
		}else{
			return getThingManagerAndThingInfo(file.getParentFile(), thingPath);
		}
	}
	
	/**
	 * 通过项目路径和事物文件返回事物的真正路径。
	 * 
	 * @param projectDir 项目目录
	 * @param thingFile 事物文件
	 * @param isDmlPrj 是否是dmlprj项目
	 * 
	 * @return 事物路径
	 */
	public static String getThingPath(File projectDir, File thingFile, boolean isDmlPrj){
		String path = thingFile.getAbsolutePath();
		String prjPath =  null;
		if(projectDir != null){
			if(!isDmlPrj){
				prjPath = new File(projectDir, "things").getAbsolutePath();
			}else{
				prjPath = projectDir.getAbsolutePath();
			}
		}else{
			prjPath = null;
		}
		
		if(prjPath != null){
			path = path.substring(prjPath.length() + 1, path.length());
		}else{
			path = thingFile.getName();
		}
		
		//过滤文件名后缀
		for(ThingCoder coder : World.getInstance().getThingCoders()){
			String type = coder.getType();
			if(path.endsWith(type)){
				path = path.substring(0, path.length() - type.length() - 1);
				return path.replace(File.separatorChar, '.');
			}
		}
		
		return null;
	}
	
	public static class Info{		
		public String thingPath;
		public File projectDir;
		public String thingManagerName;
		
		public Info(String thingPath, File projectDir, boolean isDmlprj, File prj){
			this.thingPath =  thingPath;
			this.projectDir = projectDir;
			
			//事物的真实路径
			this.thingPath = getThingPath(projectDir, new File(thingPath), isDmlprj);
			
			//把项目加入到xworker中
			World world = World.getInstance();
			if(isDmlprj){
				Properties p = new Properties();
				try{
					FileInputStream fin = new FileInputStream(prj);
					p.load(fin);
					fin.close();
				}catch(Exception e){
					logger.error("Init .dmlprj file error", e);				
				}
				String name = p.getProperty("projectName");
				world.addThingManager(new FileThingManager(name, projectDir, false));
			}else{
				world.initThingManager(projectDir);
			}			
		}
	}
	
	public static class WaiterForEnter extends Thread{
		boolean ctrPressed = false;
		InputStreamReader ir;
		BufferedReader br;
		
		public WaiterForEnter(){
			ir = new InputStreamReader(System.in);
			br = new BufferedReader(ir);
		}
		
		public void run(){
			try{
				if(br.readLine() != null){
					ctrPressed = true;				
				}
			}catch(Exception e){				
			}
		}
		
		public void close(){
			try{
				br.close();
				ir.close();
			}catch(Exception e){				
			}			
		}
	}
}