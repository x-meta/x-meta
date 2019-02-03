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
package org.xmeta.thingManagers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.xmeta.Category;
import org.xmeta.ThingCoder;
import org.xmeta.ThingIndex;
import org.xmeta.ThingManager;
import org.xmeta.World;
import org.xmeta.XMetaException;

/**
 * Jar包分类。
 * 
 * @author zhangyuxiang
 *
 */
public class JarCategory extends CachedCategory{
	public JarCategory(ThingManager thingManager, Category parentPackage, String name){
		super(thingManager, parentPackage, name);
	}
	
	@Override
	public String getFilePath() {
		return null;
	}

	@Override
	public void refresh() {		
		JarThingManager jarThingManager = (JarThingManager) this.thingManager;
		if(jarThingManager.rootCategory == this){
			try{
				World world = World.getInstance();
				Enumeration<JarEntry> enumeration = jarThingManager.jarFile.entries();   
		        while (enumeration.hasMoreElements()) {   
		            JarEntry jarEntry = (JarEntry) enumeration.nextElement();
		            if(!jarEntry.isDirectory()){
		            	String jarEntryName = jarEntry.getName();   
		            	int index = jarEntryName.lastIndexOf("/");
		            	String packageName = null;
		            	String fileName = null;
		            	if(index != -1){
		            		packageName = jarEntryName.substring(0, index);
		            		packageName = packageName.replace('/', '.');
		            		fileName = jarEntryName.substring(index + 1, jarEntryName.length());
		            	}else{
		            		fileName = jarEntryName;
		            	}
		            	
		            	index = fileName.indexOf(".");
		            	if(index != -1){
		            		String fileExt = fileName.substring(index + 1, fileName.length());
		            		ThingCoder thingCoder = world.getThingCoder(fileExt);
		            		if(thingCoder != null){
		            			String thingName = fileName.substring(0, index);
		            			if("".equals(thingName)) {
		            				continue;
		            			}
		            			initThingIndex(jarThingManager.jarFile, jarEntry, thingCoder, packageName, thingName, fileExt);
		            		}
		            	}	            	
		            }
		        }
			}catch(IOException ioe){
				throw new XMetaException("refresh jarThingManager error, jarFile=" + jarThingManager.jarFile, ioe);
			}
		}
	}
	
	private void initThingIndex(JarFile jFile, JarEntry jarEntry, ThingCoder thingCoder, String packageName, String thingName, String fileExt) throws IOException{
		Category category = initCategory(packageName);
		CachedCategory cat = ((CachedCategory) category);
		for(ThingIndex index : cat.getThingIndexs()){
			//过滤目录下的重复事物，为什么会重复，目前还未知
			if(index.getThingName().equals(thingName)){
				return;
			}
		}
		
		ThingIndex thingIndex = new ThingIndex();
		InputStream in = jFile.getInputStream(jarEntry);
		try{
			//thingCoder.decodeIndex(thingIndex, in, jarEntry.getTime());
		}finally{
			in.close();
		}
		thingIndex.name = thingName;
		thingIndex.path = (packageName == null || "".equals(packageName)) ? thingName : (packageName + "." + thingName);
		thingIndex.thingManager = this.thingManager;
		thingIndex.lastModified = jarEntry.getTime();
		((CachedCategory) category).addThingIndex(thingIndex);
	}
		
	private Category initCategory(String packageName){
		JarThingManager jarThingManager = (JarThingManager) this.thingManager;
		Category category = null;
		if(packageName == null){
			category = jarThingManager.rootCategory;			
		}else{
			Category parentPackage = jarThingManager.rootCategory;
			for(String pkName : packageName.split("[.]")){
				category = parentPackage.getCategory(pkName);
				if(category == null){
					String name = parentPackage.getName();
					if(name != null && !"".equals(name)){
						name = name + "." + pkName;
					}else{
						name = pkName;
					}
					category = new JarCategory(jarThingManager, parentPackage, name);
					((JarCategory) parentPackage).addCategory(category);
				}
				parentPackage = category;				
			}
		}
		
		return category;
	}

	@Override
	public void refresh(boolean includeChild) {
		//这里不用调用refresh，JarThingManager只需要refresh一次
		refresh();
	}
}
