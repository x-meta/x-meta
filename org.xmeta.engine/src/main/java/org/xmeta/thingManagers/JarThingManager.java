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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

import org.xmeta.Thing;
import org.xmeta.ThingCoder;
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
	boolean refreshed = false;
	ThingClassLoader classLoader;
	
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
		
		try {
			classLoader = new ThingClassLoader(new URL[] {jarFile.toURI().toURL()}, World.getInstance().getClassLoader());
		} catch (MalformedURLException e) {
			classLoader = World.getInstance().getClassLoader();
			e.printStackTrace();
		}
		//refresh();
	}
	
	@Override
	public Thing doLoadThing(String thingName) {
		try{
			synchronized(this){
				if(!refreshed){
					this.refresh();
					refreshed = true;
				}
			}
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
		return classLoader;
	}

	@Override
	public void refresh() {
		this.rootCategory.refresh();
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
	
	/**
	 * 释放资源文件到XWorker的根目录下。
	 * @throws IOException 
	 */
	public void deflateResources() throws IOException {
		Properties p = getProperties();
		String resources = p.getProperty("resources");
		if(resources != null) {
			List<String[]> resList = new ArrayList<String[]>();
			for(String res : resources.split("[,]")) {
				String[] ress = res.split(":");
				resList.add(ress);
			}
			
			if(resList.size() > 0) {
				FileInputStream fin = new FileInputStream(jarFilePath);
				JarInputStream jin = new JarInputStream(fin);
				try {
					JarEntry entry = null;
					while((entry = jin.getNextJarEntry()) != null) {
						if(entry.isDirectory()) {
							continue;
						}
						
						String name = entry.getName();						
						String[] ress = null;
						for(String[] res : resList) {
							if(name.startsWith(res[0]) || name.startsWith("/" + res[0])) {
								ress = res;
								break;
							}
						}
						
						if(ress != null) {
							String target = World.getInstance().getPath();
							if(ress.length > 1) {
								target = target + "/" + ress[1];
							}
							if(!(target.endsWith("/") || target.endsWith("\\"))) {
								target = target + "/";
							}
							target = target + name;
							
							File targetFile = new File(target);
							if(targetFile.exists() == false) {
								targetFile.getParentFile().mkdirs();
							}
							
							//long size = entry.getSize();
							FileOutputStream fout = new FileOutputStream(targetFile);
							try {
								byte[] bytes = new byte[1024 * 256];
								int length = -1;
								while((length = jin.read(bytes)) != -1) {
									fout.write(bytes, 0, length);
								}
							}finally {
								fout.close();
							}
							targetFile.setLastModified(entry.getLastModifiedTime().toMillis());
						}
						
					}
				}finally {
					jin.close();
					fin.close();					
				}
			}
		}
	}
	
	/**
	 * 返回JarThingManager的配置，从.dml或dml.properties文件中获取。
	 * 
	 * @return 如果配置文件不存在，返回一个空的Properties  
	 */
	public Properties getProperties() {
		InputStream dmlIn = this.getResourceAsStream("/.dml");
		if(dmlIn == null) {
			dmlIn = this.getResourceAsStream("/dml.properties");
		}
		if(dmlIn == null) {
			dmlIn = this.getResourceAsStream(".dml");
		}
		if(dmlIn == null) {
			dmlIn = this.getResourceAsStream("dml.properties");
		}
		
		if(dmlIn == null) {
			return new Properties();
		}else {
			Properties p = new Properties();
			try {
				p.load(dmlIn);
			}catch(Exception e){				
			}finally {			
				try {
					dmlIn.close();
				} catch (IOException e) {
				}
			}
			return p;
		}
	}

	@Override
	public String toString() {
		return "JarThingManager [jarFile=" + jarFile + ", name=" + name + ", jarFilePath=" + jarFilePath
				+ ", refreshed=" + refreshed + "]";
	}
	
	
}