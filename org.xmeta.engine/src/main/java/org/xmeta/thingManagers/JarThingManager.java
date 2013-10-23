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
			String jarEntryName = "/" + thingName.replace('.', '/');
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
	            		String fileExt = fileName.substring(index + 1, jarEntryName.length());
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
		ThingIndex thingIndex = new ThingIndex();
		InputStream in = jFile.getInputStream(jarEntry);
		try{
			thingCoder.decodeIndex(thingIndex, in, jarEntry.getTime());
		}finally{
			in.close();
		}
		thingIndex.name = thingName;
		thingIndex.path = name + "." + thingName;
		thingIndex.thingManager = this;
		((CachedCategory) category).addThingIndex(thingIndex);
	}
		
	private Category initCategory(String packageName){
		Category category = null;
		if(packageName == null){
			category = rootCategory;			
		}else{
			Category parentPackage = rootCategory;
			for(String pkName : packageName.split("[,]")){
				category = parentPackage.getCategory(pkName);
				if(category == null){
					category = new JarCategory(this, parentPackage, pkName);
					((JarCategory) parentPackage).addCategory(category);
				}
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
			throw new XMetaException("get resource from jar error", e);
		}
		
		return null;
	}

	@Override
	public void init(Properties properties) {
	}
}