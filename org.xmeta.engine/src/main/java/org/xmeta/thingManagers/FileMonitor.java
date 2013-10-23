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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.xmeta.Thing;
import org.xmeta.World;

/**
 * 监控事物文件是否被改动或者删除。
 * 
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public class FileMonitor extends TimerTask{
	private static FileMonitor instance = null;
	
	Map<String, FileEntry> things = new ConcurrentHashMap<String, FileEntry>();
	Timer timer = new Timer("Thing file monitor", true);
	World world = World.getInstance();
	
	private FileMonitor(){
		timer.schedule(this, 0, 2000);
	}
	
	public static FileMonitor getInstance(){
		if(instance == null){
			instance = new FileMonitor();
		}
		
		return instance;
	}
	
	/**
	 * 添加一个事物文件监控。
	 * 
	 * @param path  事物的路径
	 * @param thing 事物
	 * @param file  事物的文件
	 */
	public void addFile(String path, Thing thing, File file){
		FileEntry entry = new FileEntry();
		entry.path = path;
		entry.check = true;
		entry.thingReference = new WeakReference<Thing>(thing);
		entry.file = file;
		entry.lastModified = entry.file.lastModified();
		
		things.put(path, entry);
	}
	
	public FileEntry getFileEntry(String path){
		return things.get(path);
	}
	
	public void updateLastModified(FileEntry entry, long lastmodified){
		entry.lastModified = lastmodified;
	}
		
	public void run(){
		try{
			List<FileEntry> removed = new ArrayList<FileEntry>();
			
			for(String key : things.keySet()){
				FileEntry entry = things.get(key);
				Thing entryThing = entry.thingReference.get();
				if(!entry.file.exists() || entryThing == null){
					//文件已被删除或者事物已被回收站回收
					removed.add(entry);
					if(entryThing != null){
						changeRemoved(entryThing);
					}
				}else{
					Thing worldThing = world.getThing(entry.path);
					if(entryThing != worldThing || (entry.check && entry.file.lastModified() != entry.lastModified)){
						//事物发生的改动
						changeRemoved(entryThing);
						if(worldThing == null){
							removed.add(entry);
						}else{
							entry.thingReference = new WeakReference<Thing>(worldThing);
							entry.lastModified = entry.file.lastModified();
						}
					}
				}
			}
			
			for(FileEntry entry : removed){
				if(!entry.file.exists() || entry.thingReference.get() == null){
					things.remove(entry.path);
				}
			}
		}catch(Throwable th){
			//th.printStackTrace();
		}
	}
	
	private void changeRemoved(Thing thing){
		thing.getMetadata().setRemoved(true);
	}
	
	public class FileEntry{
		String path;
		public boolean check;
		public File file;
		public long lastModified;
		WeakReference<Thing> thingReference;
	}
}