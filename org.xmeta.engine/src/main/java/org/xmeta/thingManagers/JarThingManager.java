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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.xmeta.Category;
import org.xmeta.Thing;
import org.xmeta.ThingCoder;
import org.xmeta.ThingIndex;
import org.xmeta.ThingMetadata;
import org.xmeta.World;
import org.xmeta.XMetaException;
import org.xmeta.util.ThingClassLoader;

/**
 * 从Jar文件读取事物和包分类事物管理器，该事物管理器不能保存和修改事物和包。
 * 
 * @author zhangyuxiang
 *
 */
public class JarThingManager extends AbstractThingManager{
	JarFile jarFile;
	String name;
	String jarFilePath = null;
	
	public JarThingManager(String name, File jarFile){
		super(name);
		
		try {
			//打开JarFile并一直保持打开状态，由于可能会经常获取Jar中的流，故还没有更好的办法关闭它
			this.jarFile = new JarFile(jarFile);
			jarFilePath = jarFile.getAbsolutePath();
		} catch (IOException e) {
			throw new XMetaException("open jar error, jarFile=" + jarFile, e);
		}
		rootCategory = new JarCategory(this, null, null);
		
		refresh();
	}
	
	@Override
	public Thing doLoadThing(String thingName) {
		try{
			String jarEntryName = thingName.replace('.', '/');
			for(ThingCoder thingCoder : World.getInstance().getThingCoders()){
				JarEntry jarEntry = jarFile.getJarEntry(jarEntryName + "." + thingCoder.getType());
				if(jarEntry != null){
					InputStream fin = jarFile.getInputStream(jarEntry);
					try{
						Thing thing = new Thing(null, null, null, false);
						ThingMetadata metadata = thing.getMetadata();
						metadata.setPath(thingName);
						String category = null;
						String thingFileName = thingName;
						int lastDotIndex = thingName.lastIndexOf(".");
						if(lastDotIndex != -1){
							category = thingName.substring(0, lastDotIndex);
							thingFileName = thingName.substring(lastDotIndex + 1, thingName.length());
						}
						metadata.setCategory(getCategory(category));
						metadata.setCoderType(thingCoder.getType());
						metadata.setReserve(thingFileName);
						
						thingCoder.decode(thing, fin, jarEntry.getTime());
						return thing;
					}finally{
						fin.close();
					}
				}
			}
		
			return null;
		}catch(IOException e){
			throw new XMetaException("load thing from jar error, jarFile=" + jarFile, e);
		}
	}

	@Override
	public boolean doRemoveThing(Thing thing) {
		throw new XMetaException("remove thing is not supported by JarThingManager");
	}

	@Override
	public boolean doSaveThing(Thing thing) {
		throw new XMetaException("save thing is not supported by JarThingManager");
	}

	@Override
	public ThingClassLoader getClassLoader() {
		return World.getInstance().getClassLoader();
	}

	@Override
	public void refresh() {
		try{
			World world = World.getInstance();
			Enumeration<JarEntry> enumeration = jarFile.entries();   
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
	            			initThingIndex(jarFile, jarEntry, thingCoder, packageName, thingName, fileExt);
	            		}
	            	}	            	
	            }
	        }
		}catch(IOException ioe){
			throw new XMetaException("refresh jarThingManager error, jarFile=" + jarFile, ioe);
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
		thingIndex.path = packageName + "." + thingName;
		thingIndex.thingManager = this;
		thingIndex.lastModified = jarEntry.getTime();
		((CachedCategory) category).addThingIndex(thingIndex);
	}
		
	private Category initCategory(String packageName){
		Category category = null;
		if(packageName == null){
			category = rootCategory;			
		}else{
			Category parentPackage = rootCategory;
			for(String pkName : packageName.split("[.]")){
				category = parentPackage.getCategory(pkName);
				if(category == null){
					String name = parentPackage.getName();
					if(name != null && !"".equals(name)){
						name = name + "." + pkName;
					}else{
						name = pkName;
					}
					category = new JarCategory(this, parentPackage, name);
					((JarCategory) parentPackage).addCategory(category);
				}
				parentPackage = category;				
			}
		}
		
		return category;
	}

	@Override
	public boolean remove() {
		return true;
	}

	@Override
	public boolean removeCategory(String categoryName) {
		throw new XMetaException("remove package is not supported by JarThingManager");
	}

	@Override
	public boolean createCategory(String categoryName) {
		throw new XMetaException("create category is not supported by JarThingManager");
	}

	@Override
	public String getClassPath() {
		return World.getInstance().getClassLoader().getClassPath();
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		try{
			JarEntry jarEntry = jarFile.getJarEntry(name);
			if(jarEntry != null){
				return jarFile.getInputStream(jarEntry);
			}
		}catch(Exception e){
			throw new XMetaException("get resource stream from jar error", e);
		}
		
		return null;
	}

	@Override
	public URL findResource(String name) {
		try{
			JarEntry jarEntry = jarFile.getJarEntry(name);
			if(jarEntry != null){
				return new URL("jar:file:/" + jarFilePath + "!" + name);
			}
		}catch(Exception e){
			//throw new XMetaException("get resource from jar error", e);
			return null;
		}
		
		return null;
	}

	@Override
	public void init(Properties properties) {
	}

	@Override
	public boolean isSaveable() {
		return false;
	}
}