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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Properties;

import org.xmeta.ActionContext;
import org.xmeta.ActionException;
import org.xmeta.ThingCoder;
import org.xmeta.ThingManager;
import org.xmeta.World;
import org.xmeta.thingManagers.FileThingManager;

/**
 * 文件工具类。
 * 
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public class UtilFile {
	//private static Logger logger = LoggerFactory.getLogger(UtilFile.class);
	
	/**
	 * 递归删除一个文件或目录。
	 * 
	 * @param afile 要删除的文件或目录
	 */
	public static void delete(File afile){
		if(afile.isDirectory()){
			for(File subFile : afile.listFiles()){
				delete(subFile);
			}
			
			afile.delete();
		}else{
			afile.delete();
		}
	}
	
	/**
	 *  拷贝文件。
	 *  
	 * @param src 源文件  
	 * @param target 目标文件
	 * @param monitor 监控器
	 * @throws IOException IO异常
	 */
	public static void copyFile(File src, File target, FileCopyMonitor monitor) throws IOException{
		if(!src.exists()){
			throw new IOException(src.getAbsolutePath() + " is not exists!");
		}
		
		if(!target.exists()){
			File parent = target.getParentFile();
			if(!parent.mkdirs()){
				throw new IOException("can not create dir " + parent.getAbsolutePath());
			}
		}else{
			if(monitor != null){
				target = monitor.onOverWrite(src, target);
				if(target == null && monitor.cancel()){
					return;
				}
			}
		}		
		
		if(src.isFile()){			
			if(target != null && target.isDirectory()){
				throw new IOException("source is file but target is directory!");
			}
			
			copy(src, target);
			if(monitor != null){
				monitor.onCopyed(src, target);
			}			
		}else{
			if(!target.exists()){
				if(!target.mkdirs()){
					throw new IOException("can not create dir " + target.getAbsolutePath());
				}
			}
			
			for(File child : src.listFiles()){
				copyFile(child, new File(target, child.getName()), monitor);
			}
		}
	}
	
	private static void copy(File src, File target) throws IOException{
		FileInputStream fin = null;
		FileOutputStream fout = null;
		try{
			fin = new FileInputStream(src);
			fout = new FileOutputStream(target);
			
			WritableByteChannel outChannel =  Channels.newChannel(fout);
			fin.getChannel().transferTo(0, fin.available(), outChannel);
		}finally{
			if(fin != null){
				fin.close();
			}
			if(fout != null){
				fout.close();
			}
		}
	}
	
	/**
	 * 把一个正常的文件路径转化成XWorker表示下的文件路径。
	 * 
	 * @param fileName 文件名
	 * @return 字符串
	 */
	public static String toXWorkerFilePath(String fileName){
		File file = new File(fileName);		
		String filePath = file.getAbsolutePath();
		
				
		File worldFile = new File(World.getInstance().getPath());
		String worldFilePath = worldFile.getAbsolutePath();
		if(filePath.startsWith(worldFilePath)){
			filePath = "world:" + filePath.substring(worldFilePath.length(), filePath.length());
			return filePath;
		}else{
			return filePath;
		}
	}
	
	/**
	 * 获取文件路径，符合XWorker文件路径规则的。
	 * 
	 * @param fileName 文件名
	 * @return 路径
	 */
	public static String getFilePath(String fileName){
		String path = null;
		if(fileName == null){
			return fileName;
		}else if(fileName.startsWith("world|") || fileName.startsWith("world:")){
			//相对于World的路径
			path = World.getInstance().getPath() + "/" + fileName.substring(6, fileName.length());			
		}else if(fileName.startsWith("project|") || fileName.indexOf(":") != -1){
			//相对于项目的路径
			int index = fileName.indexOf(":");
			/*
			String projectName = null;
			if(fileName.startsWith("project|")){
				projectName = fileName.substring(8, index);
			}else{
				projectName = fileName.substring(0, index);
			}*/
			fileName = fileName.substring(index + 1, fileName.length());
			
		}else{
			path = fileName;
		}
		
		if(path != null){
			path = path.replace('\\', '/');
		}
		return path;
	}
	
	/**
	 * 从路径获取文件或输入流。
	 * 
	 * @param path 路径
	 * @param actionContext 变量上下文
	 * @return 对象
	 */
	public static Object getFileOrInputStream(String path, ActionContext actionContext){
		String filePath = path;
		try{
	    	//如果是http的数据
	    	URL url = new URL(path);
	    	if(url.getProtocol() != null && url.getProtocol().startsWith("http")){
	    		URLConnection con = url.openConnection();
	    		try{
		    		con.connect();
	    			return con.getInputStream();
	    		}catch(Exception ee){
	    			//logger.warn("Create image from http error", ee);
	    			throw new ActionException("Get http content error", ee);
	    		}
	    	}
	    }catch(Exception e){		    			    	
	    }
	    
		World world = World.getInstance();
	
	    //直接从文件系统取
	    File file = new File(filePath);
	    
	    //从world根目录
	    if(!file.exists() || !file.isFile()){
	        filePath = world.getPath() + "/" + path;
	        file = new File(filePath);
	    }
	    
	    //从world/webroot下取
	    if(!file.exists() || !file.isFile()){
	    	String webRoot = world.getWebFileRoot();
	    	if(webRoot == null){
	    		webRoot = world.getPath() + "/webroot";
	    	}
	        filePath = webRoot + "/" + path;
	        file = new File(filePath);
	    }
	    
	    //从系统资源的变量上下文取
	    if(!file.exists() || !file.isFile()){
	    	try {
	    		if(!path.startsWith("/") || !path.startsWith("\\")){
	    			filePath = "/" + path;
	    		}else{
	    			filePath = path;
	    		}
	    		
				InputStream rin = world.getResourceAsStream(filePath);
				if(rin != null){
					return rin;
				}
			} catch (IOException e) {					
			}
	    	
	       	Object obj = actionContext.get(path);
	       	return obj;
	    }else{
	    	return file;
	    }
	}
	
	/**
	 * 如果目标文件是一个模型文件，那么返回它的事物路径。
	 * 
	 * 在操作中还会通过.dmlprj和xworker.properties文件自动查找它所属的项目，如果找不到项目，那么把模型事物所在的目录作为它的项目，
	 * 如果项目存在那么还会加入到XWorker的事物管理器中。
	 * 
	 * @param file 模型文件
	 * @return 模型的路径，如果文件不是模型，那么返回null
	 * @throws IOException 
	 */
	public static String getThingPathByFile(File file) throws IOException{
		if(file.isFile() == false){
			return null;
		}
		
		//是否是模型
		String name = file.getName();
		String ext = null;
		int index = name.indexOf(".");
		if(index != -1){
			ext = name.substring(index + 1, name.length());
		}else{
			return null;
		}		
		boolean isModel = false;
		for(ThingCoder coder : World.getInstance().getThingCoders()){			
			if(coder.acceptType(ext)){
			//if(name.endsWith("." + coder.getType())){
				isModel = true;
				break;
			}
		}
		//System.out.println("isModel=" + isModel);
		if(!isModel){
			return null;
		}
		
		//查找项目目录和初始化项目
		File rootFile = getThingsRootAndInitProject(file.getParentFile());
		if(rootFile == null){
			rootFile = file.getParentFile();
			if(rootFile == null){
				return null;
			}
			
			//是没有项目的孤立事物，把事物所在的目录加入的事物管理器中
			String tname = UtilFile.getThingManagerNameByDir(rootFile);
			World world = World.getInstance();
			if(world.getThingManager(tname) == null){
				//把它加到事物管理器的开头
				World.getInstance().addThingManagerFirst(new FileThingManager(tname, rootFile, false));
			}
		}
		
		//返回事物的路径
		return getThingPath(rootFile, file);
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
		String prjPath =  null;
		if(projectDir != null){
			prjPath = projectDir.getAbsolutePath();
		}else{
			prjPath = null;
		}
		
		if(prjPath != null){
			path = path.substring(prjPath.length() + 1, path.length());
		}else{
			path = thingFile.getName();
		}
		
		//过滤文件名后缀
		for(ThingCoder coder : World.getInstance().getThingCoders()){
			String type = coder.getType();
			if(path.endsWith(type)){
				path = path.substring(0, path.length() - type.length() - 1);				
				path = path.replace(File.separatorChar, '.');
				if(path.startsWith(".")){
					path = path.substring(1, path.length());
				}
				
				return path;
			}
		}
		
		return null;
	}
	
	public static File getThingsRootAndInitProject(File dir) throws IOException{
		if(dir == null){
			return null;
		}
		
		File prj = new File(dir, ".dmlprj");
		if(prj.exists()){
			initProject(prj, true);
			return dir;
		}
		
		prj = new File(dir, ".dml");
		if(prj.exists()){
			initProject(prj, true);
			return dir;
		}
		
		prj = new File(dir, "dml.prj");
		if(prj.exists()){
			initProject(prj, true);
			return dir;
		}
		
		prj = new File(dir, "dml.properties");
		if(prj.exists()){
			initProject(prj, true);
			return dir;
		}
				
		prj = new File(dir, "xworker.properties");
		if(prj.exists()){
			initProject(prj, true);
			return new File(dir, "things");
		}
		
		File parent = dir.getParentFile();
		if(parent != null){
			return getThingsRootAndInitProject(parent);
		}
		
		return null;
	}
	
	public static void initProject(File prjFile) throws IOException{
		initProject(prjFile, false);
	}
	
	public static void initProject(File prjFile, boolean addToFirst) throws IOException{
		Properties p = new Properties();
		try{
			FileInputStream fin = new FileInputStream(prjFile);
			p.load(fin);
			fin.close();
		}catch(Exception e){
			throw new ActionException("Read project error, file=" + prjFile.getAbsolutePath(), e);
		}
		
		String name = p.getProperty("projectName");
		if(name == null || "".equals(name.trim())){
			name = getThingManagerNameByDir(prjFile.getParentFile());//.getCanonicalPath();
		}
		
		World world = World.getInstance();
		ThingManager thingManager = world.getThingManager(name); 
		if(thingManager != null){
			//logger.warn("Thing manager already exists, name=" + name);
		}else{
			thingManager = world.initThingManager(prjFile.getParentFile(), name);
			if(addToFirst) {
				world.moveThingManagerToFirst(thingManager);
			}
			/*
			if(".dmlprj".equals(prjFile.getName()) || "dml.prj".equals(prjFile.getName())){
				world.addFileThingManager(name, prjFile.getParentFile(), false, true);
			}else{
				//xworker.properties
				world.addFileThingManager(name, prjFile.getParentFile(), true, true);
			}*/
		}
	}
	
	/**
	 * 从一个dir中获取事物管理器的名称。
	 * 
	 * @param dir
	 * @return
	 * @throws IOException 
	 */
	public static String getThingManagerNameByDir(File dir) throws IOException{
		String name = dir.getCanonicalPath();
		name = name.replace(':', '_');
		name = name.replace('/', '_');
		name = name.replace('\\', '_');
		name = name.replace('.', '_');
		
		if(name.length() > 45){
			name = name.substring(0, 20) + "_" + name.substring(name.length() - 20, name.length());
		}
		
		return name;
	}
	
	/**
	 * 判断childFile是否是parentFile的一个子文件，包括更深的子目录等。
	 * 
	 * @param parentFile
	 * @param childFile
	 * @return
	 * @throws IOException 
	 */
	public static boolean isParent(File parentFile, File childFile) throws IOException{
		if(parentFile == null || childFile == null){
			return false;
		}
		
		return childFile.getCanonicalPath().startsWith(parentFile.getCanonicalPath());
	}
}