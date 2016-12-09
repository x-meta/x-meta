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
import org.xmeta.World;

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
	
	public static void initProjects(){
		World world = World.getInstance();
		
		File projectsDir = new File("./projects/");
		File worldPrjDir = new File(world.getPath() + "/projects/");
		try {
			//如果是XWorker字段的项目，不要初始化
			if(projectsDir.getCanonicalPath().equals(worldPrjDir.getCanonicalPath())){
				return;
			}
		} catch (IOException e1) {
			return;
		}
		
		if(projectsDir.exists() && projectsDir.isDirectory()){
			for(File prjDir : projectsDir.listFiles()){
				if(prjDir.isDirectory() && isProject(prjDir)){
					try{
						world.initThingManager(prjDir);
					}catch(Exception e){
						logger.warn("init project error", e);
					}
				}
			}
		}		
	}
	
	public static boolean isProject(File dir){
		return new File(dir, "config.properties").exists() || new File(dir, "xworker.properties").exists() ||
				new File(dir, "dml.prj").exists();		
	}
	
	public static void run(String args[]) {
		try {
			try{
				//为了事物管理器里的控制台能够输入log4j的日志等，以后应该想办法解决这个不优雅的方式
				//System.out.println("load SystemIoRedirector");
				Class<?> cls = Class.forName("xworker.io.SystemIoRedirector");
				cls.getMethod("init", new Class<?>[]{}).invoke(null, new Object[]{});
				//System.out.println("loaded SystemIoRedirector");
			}catch(Exception e){			
			}
			
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
			/*
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
			*/
			
			if(actionName == null){
				actionName = "run";
			}
			
			if(thingPath == null){
				System.out.println("Please input thing path or thing file");
				System.exit(0);
			}

			logger.info("world path : " + worldPath);
			logger.info("thing path : " + thingPath);
			logger.info("action name : " + actionName);
			
			World world = World.getInstance();
			world.init(worldPath);
			
			//初始化项目，如果当前目录存在projects目录的话
			initProjects();
			
			/*
			if(jarFile != null && jarFile.exists()){
				world.getClassLoader().addJarOrZip(jarFile);
			}*/
			
			for(String arg : args){
				if(arg.toLowerCase().equals("-verbose")){
					world.setVerbose(true);
				}				
			}
			
			boolean isFile = false;
			File thingFile = new File(thingPath);
			if(!thingFile.exists()){
				thingFile = new File("./" + thingPath);
				//logger.info("create temp thing");
			}
			if(thingFile.exists()){
				//打开的事物是一个文件
				//logger.info("thing is file :" + thingFile.getPath());
				thingPath = UtilFile.getThingPathByFile(thingFile.getAbsoluteFile());
				
				if(thingPath == null){
					logger.info("Cann't open, file is no a thing, file=" + thingPath);
					return;
				}
				isFile = true;
			}else{
				//打开的是事物的路径
				File file = new File(".");
				//查找项目目录和初始化项目
				File rootFile = UtilFile.getThingsRootAndInitProject(file);
				if(rootFile == null){
					rootFile = file;
					
					//是没有项目的孤立事物，把事物所在的目录加入的事物管理器中
					String tname = UtilFile.getThingManagerNameByDir(rootFile);
					if(world.getThingManager(tname) == null){
						//把它加到事物管理器的开头
						world.addFileThingManager(tname, rootFile, false, true);						
					}
				}
			}			
			
			if(isFile){
				//如果是文件，那么可以选择是编辑还是运行
				/*
				System.out.print("按任意键进入编辑器(Preess any key to edit)，等待3秒(Wait for 3 seconds): ");
				WaiterForEnter we = new WaiterForEnter();
				we.start();
				we.waitForEnter();
				
				if(we.ctrPressed){
					if(editThing(thingFile.getAbsolutePath())){
						return;
					}
				}*/
			}
			
			//XWorker是一个命令行的特殊事物，为了和目录xworker区分，所以用了前大写
			//但用户输入可能会大小写错误，所以再次规避
			if("XWORKER".equals(thingPath.toUpperCase())){
				thingPath = "XWorker"; //
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
		
		String baseUrl = globalConfig.getString("webUrl") + "do?sc=xworker.ide.worldExplorer.swt.http.IDETools";
		try{			
			URL checkIde = new URL(baseUrl + "&ac=isIdeOpened");
			URLConnection urlcon = checkIde.openConnection();
			InputStream in = urlcon.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String content = reader.readLine();
			if(!"true".equals(content)){
				System.out.println("XWorker has not run thing explorer, run thing......");
				return false;
			}
		}catch(Exception e){
			return startExplorerAndEditThing(thingPath);
		}
		
		try{
			//事物管理器打开的状态，下请求打开
			URL openThing =  new URL(baseUrl + "&ac=oepenThing&path=" + URLEncoder.encode(thingPath, "utf-8"));
			openThing.openConnection().getContent();
			
			return true;
		}catch(Exception e){
			System.out.println("Exception happend, run thing......, " + e.getLocalizedMessage());
			return false;
		}
	}
	
	private static boolean startExplorerAndEditThing(String filePath){
		Thing explorer = World.getInstance().getThing("xworker.ide.worldExplorer.swt.SimpleExplorerRunner");
		if(explorer == null){
			return false;
		}
		
		ActionContext actionContext = new ActionContext();
		actionContext.put("defaultOpenFile", new File(filePath));
		explorer.doAction("run", actionContext);
		return true;
	}
	
	
	public static class WaiterForEnter extends Thread{
		boolean ctrPressed = false;
		boolean stoped = false;
		Thread waitThread = Thread.currentThread();	
		
		public WaiterForEnter(){			
		}
		
		public void waitForEnter(){
			try{				
				while(!stoped){
					if(System.in.available() > 0){
						ctrPressed = true;
					}
					
					Thread.sleep(300);
				}
			}catch(Exception e){				
			}
		}
		
		public void run(){
			long timestart = System.currentTimeMillis();
			while(System.currentTimeMillis() - timestart < 3000){
				if(ctrPressed){
					break;
				}
				
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			stoped = true;
		}
	}
}