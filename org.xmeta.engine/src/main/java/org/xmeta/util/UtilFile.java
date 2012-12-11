/*
 * Copyright 2007-2008 The X-Meta.org.
 * 
 * Licensed to the X-Meta under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The X-Meta licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xmeta.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import org.xmeta.World;

/**
 * 文件工具类。
 * 
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public class UtilFile {
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
	 * 
	 * @param src
	 * @param target
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
	 * @param fileName
	 * @return
	 */
	public static String toXWorkerFilePath(String fileName){
		File file = new File(fileName);		
		String filePath = file.getAbsolutePath();
		
				
		File worldFile = new File(World.getInstance().getPath());
		String worldFilePath = worldFile.getAbsolutePath();
		if(filePath.startsWith(worldFilePath)){
			filePath = "world|" + filePath.substring(worldFilePath.length(), filePath.length());
			return filePath;
		}else{
			return filePath;
		}
	}
	
	/**
	 * 获取文件路径，符合XWorker文件路径规则的。
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getFilePath(String fileName){
		String path = null;
		if(fileName == null){
			return fileName;
		}else if(fileName.startsWith("world|")){
			//相对于World的路径
			path = World.getInstance().getPath() + "/" + fileName.substring(6, fileName.length());			
		}else if(fileName.startsWith("project|") || fileName.indexOf(":") != -1){
			//相对于项目的路径
			int index = fileName.indexOf(":");
			String projectName = null;
			if(fileName.startsWith("project|")){
				projectName = fileName.substring(8, index);
			}else{
				projectName = fileName.substring(0, index);
			}
			fileName = fileName.substring(index + 1, fileName.length());
			
		}else{
			path = fileName;
		}
		
		if(path != null){
			path = path.replace('\\', '/');
		}
		return path;
	}
}
