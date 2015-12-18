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
import java.io.InputStream;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
	public static void main(String args[]){
		run(args);
	}
	
	public static boolean loadXerFromJar(Properties p , File fileName){
		try{
			JarFile jarFile = new JarFile(fileName);
			try{
				 JarEntry entry = jarFile.getJarEntry("xer.ini");
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
			
			//读取xer.ini的参数配置
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
				String xerFileName = "xer.ini";
				File xerFile = new File(xerFileName);
				if(!xerFile.exists()){
					xerFile = new File(worldPath + "/xer.ini");
				}
				if(xerFile.exists()){
					FileInputStream fin = new FileInputStream(xerFile);
					p.load(fin);
					fin.close();
				}else{
					InputStream fin = ThingRunner.class.getResourceAsStream("/xer.ini");
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
			
			//如果当前目录是XWorker项目，那么加入
			File xworkerProperties = new File("xworker.properties");
			if(xworkerProperties.exists()){
				//当前目录下的projectName必须存在
				Properties prop = new Properties();
				FileInputStream fin = new FileInputStream(xworkerProperties);
				prop.load(fin);
				if(prop.get("projectName") != null){
					world.initThingManager(new File("."));
				}
				fin.close();
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
}