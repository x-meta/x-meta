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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.xmeta.Category;
import org.xmeta.Thing;
import org.xmeta.ThingCoder;
import org.xmeta.ThingMetadata;
import org.xmeta.World;
import org.xmeta.XMetaException;
import org.xmeta.thingManagers.FileMonitor.FileEntry;
import org.xmeta.util.ThingClassLoader;
import org.xmeta.util.UtilFile;

public class FileThingManager extends AbstractThingManager{
	/** 事物的根目录 */
	File thingRootFile = null;
	
	/** 事物管理器的根目录 */
	File rootFile = null;
	
	public FileThingManager(String name, File rootFile){
		this(name, rootFile, true);
	}
	
	public FileThingManager(String name, File rootFile, boolean hasThingsDir){
		super(name);
		
		this.rootFile = rootFile;
		if(hasThingsDir){
			this.thingRootFile = new File(rootFile, "things");
		}else{
			this.thingRootFile = rootFile;
		}
		rootCategory = new FileCategory(null, this , null);
	}
	
	@Override
	public Thing doLoadThing(String thingName) {
		try{
			if("xworker.lang.actions.GroovyAction".equals(name)){
				System.out.println("FileTHingManager : groovy action reloaded");
			}
			String thingPath = thingName.replace('.', '/');
			World world = World.getInstance();
			for(ThingCoder thingCoder : world.getThingCoders()){
				File thingFile = new File(thingRootFile, thingPath + "." + thingCoder.getType());
				if(thingFile.exists()){
					FileInputStream fin = new FileInputStream(thingFile);
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
						metadata.setCategory(getCategory(category, true));
						metadata.setCoderType(thingCoder.getType());
						metadata.setReserve(thingFileName);
						
						thingCoder.decode(thing, fin, thingFile.lastModified());
						
						FileMonitor.getInstance().addFile(thingName, thing, thingFile);
						return thing;
					}finally{
						fin.close();
					}
				}
			}
			
			return null;
		}catch(Exception e){
			throw new XMetaException("load thing from FileThingManager error, managePath=" + rootFile + ",thing=" + thingName, e);
		}
	}

	@Override
	public boolean doRemoveThing(Thing thing) {
		Thing rootThing = thing.getRoot();
		if(rootThing.getMetadata().getThingManager() == this){
			File thingFile = new File(thingRootFile, rootThing.getMetadata().getPath().replace('.', '/') + "." + thing.getMetadata().getCoderFileType());
			if(thingFile.exists()){
				thingFile.delete();
				return true;
			}
			
			return false;
		}
		return false;
	}

	@Override
	public boolean doSaveThing(Thing thing) {
		try{
			Thing rootThing = thing.getRoot();
			String rootThingPath = rootThing.getMetadata().getPath();
			FileEntry fileEntry = FileMonitor.getInstance().getFileEntry(rootThingPath);
			if(fileEntry != null){
				fileEntry.check = false; //避开文件监控
			}
			try{
				if(rootThing.getMetadata().getThingManager() == this){
					ThingCoder thingCoder = World.getInstance().getThingCoder(rootThing.getMetadata().getCoderType());
					File thingFile = new File(thingRootFile, rootThingPath.replace('.', '/') + "." + thingCoder.getType());
					if(!thingFile.exists()){
						thingFile.getParentFile().mkdirs();
					}
					FileOutputStream fout = new FileOutputStream(thingFile);
					try{
						thingCoder.encode(rootThing, fout);
						fout.flush();
					}finally{
						fout.close();						
					}
					thingFile.setLastModified(rootThing.getMetadata().getLastModified());
					if(fileEntry != null){
						fileEntry.lastModified = thingFile.lastModified();
					}else{						
						FileMonitor.getInstance().addFile(rootThingPath, rootThing, thingFile);
					}
				}
				return true;
			}finally{
				if(fileEntry != null){
					fileEntry.check = true;
				}
			}
		}catch(Exception e){
			throw new XMetaException("save thing by FileThingManager error, managePath=" + thingRootFile + ",thing=" + thing, e);
		}
	}

	@Override
	public boolean createCategory(String categoryName) {
		if(categoryName == null || categoryName.equals("")){
			return false;
		}
			
		if(getCategory(categoryName) != null){
			return true;
		}else{
			File dir = new File(thingRootFile, categoryName.replace('.', '/'));
			if(!dir.exists()){
				if(dir.mkdirs()){
					this.refreshParentCategory(categoryName);
					//refresh();
					return true;
				}else{
					return false;
				}
			}else{
				//目录已经存在
				this.refreshParentCategory(categoryName);
				return true;
			}
		}			
	}

	@Override
	public ThingClassLoader getClassLoader() {
		return World.getInstance().getClassLoader();
	}

	@Override
	public void refresh() {
		rootCategory.refresh(false);
	}

	@Override
	public boolean remove() {
		UtilFile.delete(thingRootFile);
		
		return true;
	}

	@Override
	public boolean removeCategory(String categoryName) {
		if(categoryName == null || categoryName.equals("")){
			//根目录不允许删除
			return false;
		}else{
			Category category = getCategory(categoryName);
			Category parent = category.getParent();
			File dir = new File(thingRootFile + "/" + categoryName.replace('.', '/'));
			UtilFile.delete(dir);
			
			if(parent != null){
				parent.refresh();
			}
			
			return true;
		}	
	}

	public String getFilePath(){
		return thingRootFile.getAbsolutePath();
	}

	@Override
	public String getClassPath() {
		return World.getInstance().getClassLoader().getClassPath();
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		File resource = new File(thingRootFile, name);
		if(resource.exists() && resource.isFile()){
			try {
				return new FileInputStream(resource);
			} catch (FileNotFoundException e) {
				throw new XMetaException("get resource from fileThingManager error, resource=" + name + ", thingManager=" + rootFile, e);
			}
		}
		
		return getClassLoader().getResourceAsStream(name);
		//return null;
	}

	@Override
	public URL findResource(String name) {
		File file = new File(thingRootFile, name);
		if(file.exists()){
			try {
				return file.toURI().toURL();
			} catch (MalformedURLException e) {
				throw new XMetaException("FileThingManager find resource error, thingManager=" + name, e);
			}
		}
		
		return null;
	}

	@Override
	public void init(Properties properties) {
	}
	
	public String toString(){
		return "FileThingManger[name:" + getName() + ", rootFile: " + rootFile.getAbsolutePath() + "]";
	}

	@Override
	public boolean isSaveable() {
		return true;
	}
}