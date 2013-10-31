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
	public static void main(String args[]) {
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
			String xerFileName = "xer.ini";
			File xerFile = new File(xerFileName);
			if(xerFile.exists()){
				FileInputStream fin = new FileInputStream(xerFileName);
				p.load(fin);
				fin.close();
			}else{
				InputStream fin = ThingRunner.class.getResourceAsStream("/xer.ini");
				if(fin != null){
					p.load(fin);
					fin.close();
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
			System.out.println("thing path : " + thingPath);
			System.out.println("action name : " + actionName);
			
			World world = World.getInstance();
			world.init(worldPath);
			
			for(String arg : args){
				if(arg.toLowerCase().equals("-verbose")){
					world.setVerbose(true);
				}				
			}
			
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
}