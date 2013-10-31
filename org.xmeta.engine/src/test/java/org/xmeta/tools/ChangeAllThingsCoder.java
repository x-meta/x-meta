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
package org.xmeta.tools;

import java.io.File;
import java.util.Iterator;

import org.xmeta.Thing;
import org.xmeta.ThingManager;
import org.xmeta.World;

/**
 * 以指定的格式重新保存所有事物。
 * 
 * @author Administrator
 *
 */
public class ChangeAllThingsCoder {
	/**
	 * 转换格式。
	 * 
	 * @param codeType
	 */
	public static void convertCoder(String codeType){
		World world = World.getInstance();
		
		for(ThingManager thingManager : world.getThingManagers()){
			Iterator<Thing> iter = thingManager.iterator("", true);
			while(iter.hasNext()){
				Thing thing = iter.next();
				thing.getMetadata().setCoderType(codeType);
				thing.save();
				
				System.out.println(thing.getMetadata().getPath());
			}
		}	
		
		System.out.println("格式转换完毕");
	}
	
	/**
	 * 删除后缀为ext的所有文件。
	 * 
	 * @param root
	 * @param ext
	 */
	public static void deleteFile(File root, String ext){
		System.out.println(root.getAbsolutePath());
		if(root.isDirectory()){
			for(File child :root.listFiles()){
				deleteFile(child, ext);
			}
		}else{
			if(root.getName().endsWith(ext)){
				System.out.println("deleted:" +  root.getAbsolutePath());
				root.delete();
			}
		}
	}
	

	public static void main(String args[]){
		World world = World.getInstance();
		
		//String worldPath = "D:\\dist\\xworker-1.3.3\\"; 
		String worldPath = "..\\..\\xworker\\xworker\\";
		world.init(worldPath);
		
		convertCoder("xer.txt");
		//deleteFile(new File(worldPath), ".xer");
		
		long start = System.currentTimeMillis();

		//Thing thing = world.getThing("xworker.ide.worldExplorer.swt.SimpleExplorerRunner");
		//thing.doAction("run");
		System.out.println("time=" + (System.currentTimeMillis() - start));
		//System.exit(0);
	}
}