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
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmeta.Category;
import org.xmeta.ThingCoder;
import org.xmeta.ThingCoderException;
import org.xmeta.ThingIndex;
import org.xmeta.ThingManager;
import org.xmeta.World;
import org.xmeta.XMetaException;

/**
 * 文件目录。
 * 
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public class FileCategory extends CachedCategory{
	private static Logger logger = LoggerFactory.getLogger(FileCategory.class);
	
	/**
	 * 构造函数。
	 * 
	 * @param name 目录名称
	 * @param thingManager 事物管理者
	 * @param parent 父目录
	 */
	public FileCategory(String name, ThingManager thingManager, Category parent){		
		super(thingManager, parent, name);
		
		//refresh();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.xmeta.Category#refresh()
	 */
	public void refresh(){
		try{
			//目录的路径
			String categoryName = name;
			if(categoryName == null){
				categoryName = "";
			}
			String filePath = ((FileThingManager) thingManager).getFilePath() + "/" + categoryName.replace('.', '/');
			File categoryFile = new File(filePath);
			if(categoryFile.isDirectory()){
				List<Category> _childs = new ArrayList<Category>();
				List<ThingIndex> _thingIndexs = new ArrayList<ThingIndex>();
				
				for(File file : categoryFile.listFiles()){
					String childName = file.getName();
					String path = childName;
					if(name != null && !"".equals(name)){
						path = name + "." + childName;
					}
					if(file.isDirectory()){
						//判断子目录是否已经存在
						Category child = null;
						for(int i=0; i<childCategorys.size(); i++){
							Category c = childCategorys.get(i);
							if(c.getSimpleName().equals(childName)){
								child = c;
								break;
							}
						}
						if(child == null){
							child = new FileCategory(path, thingManager, this);
						}
						
						//按名字顺序添加子目录
						boolean added = false;
						for(int i=0; i<_childs.size(); i++){
							Category c = _childs.get(i);
							if(c.getSimpleName().compareTo(child.getSimpleName()) > 0){
								_childs.add(i, child);
								added = true;
								break;
							}
						}
						if(!added){
							_childs.add(child);
						}
					}else{
						//判断是否是事物
						int index = childName.indexOf(".");
						if(index != -1){
							String type = childName.substring(index + 1, childName.length());
							ThingCoder thingCoder = World.getInstance().getThingCoder(type);
							if(thingCoder != null){
								//判断事物索引是否已经存在
								ThingIndex child = null;
								String thingName = childName.substring(0, index);
								path = path.substring(0, path.lastIndexOf("."));
								for(int i=0; i<thingIndexs.size(); i++){
									ThingIndex c = thingIndexs.get(i);
									if(c.getName().equals(thingName)){
										child = c;
										break;
									}
								}
								if(child == null){
									FileInputStream fin = new FileInputStream(file);
									try{
										child = new ThingIndex();
										//thingCoder.decodeIndex(child, fin, file.lastModified());										
										child.name = thingName;
										if(name != null){
											child.path = name + "." + thingName;
										}else{
											child.path = thingName;
										}
										child.thingManager = thingManager;
										child.lastModified = file.lastModified();
									}catch(ThingCoderException e){
										logger.error("File category load index error, " + file.getPath(), e);
										throw e;
									}finally{
										fin.close();
									}
								}else{
									child.lastModified = file.lastModified();
								}
								
								if(child != null){
									//按名字顺序添加事物索引
									boolean added = false;
									for(int i=0; i<_thingIndexs.size(); i++){
										ThingIndex c = _thingIndexs.get(i);
										if(c.getName().compareTo(child.getName()) > 0){
											_thingIndexs.add(i, child);
											added = true;
											break;
										}
									}
									if(!added){
										_thingIndexs.add(child);
									}
								}
							}
						}					
					}
				}
				
				//更新索引
				childCategorys = _childs;
				thingIndexs = _thingIndexs;
			}
		}catch(Exception e){
			throw new XMetaException("refresh file category error", e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.xmeta.Category#refresh(boolean)
	 */
	public void refresh(boolean includeChilds){
		this.refresh();
		
		if(includeChilds){
			for(Category child : childCategorys){
				child.refresh(includeChilds);
			}
		}
	}

	@Override
	public String getFilePath() {
		return ((FileThingManager) thingManager).getFilePath() + "/" + name.replace('.', '/');
	}
}