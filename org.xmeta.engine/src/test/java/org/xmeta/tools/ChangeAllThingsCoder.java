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