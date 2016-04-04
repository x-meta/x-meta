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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
			}else{
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
				}else{
					InputStream fin = ThingRunner.class.getResourceAsStream("/dml.ini");
					if(fin != null){
						p.load(fin);
						fin.close();
					}				
				}
			}
			if(worldPath == null || thingPath == null){								
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
			
			if("new".equals(thingPath)){				
				if(actionName == null || "".equals(actionName) || args.length < 3){
					System.out.println("Please input project name, command: xer new <projectName>");
					return;
				}else{
					System.out.println("create new project: " + actionName);
					
					World world = World.getInstance();
					world.init(worldPath);
					
					Thing thing = world.getThing("xworker.tools.CreateProjectMini");
					
					ActionContext actionContext = new ActionContext();
					actionContext.put("args", args);
					thing.doAction("run", actionContext);
					return;
				}
			}else{
				System.out.println("thing path : " + thingPath);
				System.out.println("action name : " + actionName);
			}
			
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
			
			//根据事物路径确定事物的真正路径和项目目录，事物路径现在有可能是文件路径
			Info info = getInfo(thingPath);
			if(info == null){
				//如是在当前目录下运行事物
				File xworkerProperties = new File(".dmlprj");
				if(xworkerProperties.exists()){
					//当前目录下的projectName必须存在
					Properties prop = new Properties();
					FileInputStream fin = new FileInputStream(xworkerProperties);
					prop.load(fin);
					if(prop.get("projectName") != null){
						world.initThingManager(new File("."));
					}
					fin.close();
				}else{
					addLocalThingManager(world, new File("."));					
				}
			}else{
				thingPath = info.thingPath;
				
				if(thingPath != null && info.thingManagerPath != null){
					addLocalThingManager(world, new File(info.thingManagerPath));
				}
			}
			
			if("-war".equals(thingPath)){
				if(actionName == null || "".equals(actionName) || args.length < 3){
					System.out.println("Please input a war file, command: xer -war <warFile> <port>");
					return;
				}else{
					Thing startJetty = world.getThing("xworker.tools.StartJetty");
					if(startJetty == null){
						System.out.println("Thing xworker.tools.StartJetty not exists");
						return;
					}
					
					ActionContext actionContext = new ActionContext();
					actionContext.put("warFile", actionName);
					
					int port = 9003;
					if(args.length == 4){
						port = Integer.parseInt(args[3]);
					}
					actionContext.put("port", port);
					startJetty.doAction("run", actionContext);
				}
			}else{
				Thing thing = world.getThing(thingPath);
				if (thing == null) {
					System.out.println("thing not exists : " + thingPath);
					System.exit(0);
				} else {
					ActionContext actionContext = new ActionContext();
					actionContext.put("args", args);
					thing.doAction(actionName, actionContext);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private static void addLocalThingManager(World world, File dir) throws IOException{
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

		if(world.getThingManager(working_project) == null){
			FileThingManager working = new FileThingManager(working_project, dir, false);
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
	public static Info getInfo(String thingPath){
		File file = new File(thingPath);
		if(!file.exists()){
			return null;
		}else{
			File projectDir = getProjectDir(file);
			String thingP = getThingPath(projectDir, file);
			String managerPath = projectDir == null ? file.getParentFile().getAbsolutePath() : projectDir.getAbsolutePath();
			
			return new Info(thingP, managerPath);
		}
	}
	
	/**
	 * 通过项目路径和事物文件返回事物的真正路径。
	 * 
	 * @param projectDir 项目目录
	 * @param thingFile 事物文件
	 * 
	 * @return 事物路径
	 */
	public static String getThingPath(File projectDir, File thingFile){
		String path = thingFile.getAbsolutePath();
		String prjPath = projectDir != null ? projectDir.getAbsolutePath() : null;
		
		if(prjPath != null){
			path = path.substring(prjPath.length(), path.length());
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
	
	/**
	 * 获取项目路径。
	 * 
	 * @param file 当前文件
	 * 
	 * @return 如果返回File，否则返回null
	 */
	public static File getProjectDir(File file){
		if(file == null || !file.exists()){
			return null;
		}
		
		if(file.getName().equals(".dmlprj") && file.isDirectory()){
			return file;
		}else{
			return getProjectDir(file.getParentFile());
		}
	}
	
	public static class Info{		
		String thingPath;
		String thingManagerPath;
		
		public Info(String thingPath, String thingManagerPath){
			this.thingManagerPath = thingManagerPath;
			this.thingPath =  thingPath;
		}
	}
}