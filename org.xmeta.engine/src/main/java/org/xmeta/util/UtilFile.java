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