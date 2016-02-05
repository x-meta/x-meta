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
package org.xmeta;

import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmeta.ui.session.Session;
import org.xmeta.ui.session.SessionManager;

/**
 * 事物元信息。
 * 
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public class ThingMetadata {
	private static Logger logger = LoggerFactory.getLogger(ThingMetadata.class);
	
	/** 事物的标识 */
	String id;
	
	/** 最后修改时间 */
	long lastModifed;
	
	/** 路径 */
	String path;
	
	/** 保留字段，一般为事物管理者所使用 */
	String reserve;
	
	/** 元数据对应的事物 */
	Thing thing;
	
	/** 事物所在的目录 */
	Category category;

	/** 事物文件版本号 */
	int fileVersion;
	
	/** 事物在存储是是否包含描述者所规定的默认值 */
	boolean includeDefaultValue = false;
	
	/** 事物是否已经被删除 */
	boolean removed = false;
	
	/** 编码类型 */
	String coderType = "xer.txt";
	/** 用户分组，一般是UI管理界面的用户自定义分组  */
	String userGroup = null;
	
	/**
	 * 构造函数
	 * 
	 * @param thing 事物
	 */
	public ThingMetadata(Thing thing){
		this.thing = thing;
	}
	
	/**
	 * 返回当前事物所在的目录。
	 * 
	 * @return 目录
	 */
	public Category getCategory(){
		return category;
	}
	
	public void setCategory(Category category){
		this.category = category;
	}
	
	public void initPath(){
		if(category != null){			
			String packageName = category.getName();
			String fileName = this.reserve;
			if(fileName == null){
				fileName = getName();
			}
			if(packageName != null){				
				this.path = packageName + "." + fileName;
			}else{
				this.path = fileName;
			}			
		}
	}
	
	/**
	 * 获取事物的最后修改时间。
	 * 
	 * @return 最后修改时间
	 */
	public long getLastModified(){
		return lastModifed;
	}
	
	/**
	 * 设置最后的修改时间。
	 * 
	 * @param lastModified 最后修改时间
	 */
	public void setLastModified(long lastModified){
		this.lastModifed = lastModified;	
	}
	
	/**
	 * 获得事物的标识，同一级的事物都具有不同的标识。
	 * 
	 * @return 事物的标识
	 */
	public String getId(){
		if(id != null){
			return id;
		}
		
		if(id == null || "".equals(id)){
			try{
				id = (String) thing.getAttributes().get("id");
			}catch(Exception e){				
			}
		}
		
		return id;
	}
	
	/**
	 * 事物的标识。
	 * 
	 * @param id 标识
	 */
	public void setId(String id){
		this.id = id;
		//重新设置路径
		if(path != null){
			int index = path.lastIndexOf("@");
			if(index != -1){
				path = path.substring(0, index) + "@" + id;
			}
		}
		
		for(Thing child : thing.getChilds()){
			thing.initChildMetadata(child);
		}
	}
	
	/**
	 * 返回事物的名称，有时事物的属性没有定义名称，那么此时返回事物的描述者的名称。
	 * 
	 * @return 事物的名成
	 */
	public String getName(){
		String name = (String) thing.attributes.get(Thing.NAME);
		if(name != null && !"".equals(name)){
			return name;
		}else{
			List<Thing> descriptors = thing.getDescriptors();
			if(descriptors.size() > 0){
				return (String) descriptors.get(0).attributes.get(Thing.NAME);
			}else{
				return "";
			}
		}		
	}
	
	/**
	 * 标签可理解为事物的名的另一种语言的翻译，比如事物的属性"name"，那么标签属性可谓“名称”。
	 * 通常在界面上显示“名称”，此外也可作多语言的翻译桥梁。
	 * 
	 * @return 事物的标签
	 */
	public String getLabel(){
		Session session = SessionManager.getSession(null);
		Locale locale = session.getLocale();
		return getLabel(locale);
	}
	
	public String getGroup(){
		if(userGroup != null && !"".equals(userGroup)){
			return userGroup;
		}
		
		Session session = SessionManager.getSession(null);
		Locale locale = session.getLocale();
		return getLocaleString("group", locale);
	}
	
	public void setUserGroup(String userGroup){
		this.userGroup = userGroup;
	}
	
	/**
	 * 根据指定的地区获取相应的标签。
	 * 
	 * @param locale 本地
	 * @return 标签
	 */
	public String getLabel(Locale locale){
		String label = getLocaleString(Thing.LABEL, locale);
		if(label == null){
			return getName();
		}else{
			return label;
		}
	}
	
	/**
	 * 返回本地化的事物对应属性的字符串值。
	 * 
	 * @param name 名称
	 * @param locale 本地
	 * @return 值
	 */
	public String getLocaleString(String name, Locale locale){
		if(locale == null){
			Session session = SessionManager.getSession(null);
			locale = session.getLocale();
		}
		
		String country = locale.getCountry();
		String language = locale.getLanguage();
		
		String label = thing.getString(country + "_" + language + "_" + name);
		if(label == null || "".equals(label)){
			label = thing.getString(language + "_" + name);
		}
		if(label == null || "".equals(label)){
			label = thing.getString(name);
		}
		if(label == null || "".equals(label)){
			return null;
		}else{
			return label;
		}
	}
	
	/**
	 * 返回事物的一段文本描述，此描述事物可能定义了也可能并未定义。
	 * 
	 * @return 事物的描述
	 */
	public String getDescription(){
		Session session = SessionManager.getSession(null);
		Locale locale = session.getLocale();
		String country = locale.getCountry();
		String language = locale.getLanguage();
		
		String description = thing.getString(country + "_" + language + "_" + Thing.DESCRIPTION);
		if(description == null || "".equals(description)){
			description = thing.getString(language + "_" + Thing.DESCRIPTION);
		}
		if(description == null || "".equals(description)){
			description = thing.getString(Thing.DESCRIPTION);
		}
		
		if((description == null || "".equals(description.trim())) && thing.getBoolean("inheritDescription")){
			//是否是继承描述
			List<Thing> extendsThing = thing.getExtends();
			if(extendsThing.size() > 0){
				return extendsThing.get(0).getMetadata().getDescription();
			}
		}
		return description;
	}
	
	/**
	 * 返回事物的路径。
	 * 
	 * @return 事物的路径
	 */
	public String getPath(){
		return path;
	}
	
	public String[] getPaths(){
		List<Thing> extendList = thing.getAllExtends();
    	String[] paths = new String[1 + extendList.size()];
    	paths[0] = getPath();
    	for(int i=1; i<=extendList.size(); i++){
    		Thing ext = (Thing) extendList.get(i-1);
    		paths[i] = ext.getMetadata().getPath();
    	}
    	
    	return paths;	
	}
	
	/**
	 * 设置本事物的路径。
	 * 
	 * @param path 事物的路径
	 */
	public void setPath(String path){
		this.path = path;
	}
	
	/**
	 * 取得事物所在的事物管理者。
	 * 
	 * @return 事物管理者
	 */
	public ThingManager getThingManager(){
		return  category.getThingManager();
	}

	public int getFileVersion() {
		return fileVersion;
	}

	public void setFileVersion(int fileVersion) {
		this.fileVersion = fileVersion;
	}

	public boolean isIncludeDefaultValue() {
		return includeDefaultValue;
	}

	public void setIncludeDefaultValue(boolean includeDefaultValue) {
		this.includeDefaultValue = includeDefaultValue;
	}

	/**
	 * 保留变量，通常是留给ThingManager等系统对象所用变量。
	 * @return 保留变量
	 */
	public String getReserve() {
		return reserve;
	}

	public void setReserve(String reserve) {
		this.reserve = reserve;
	}

	public boolean isRemoved() {
		if(removed == true){
			//logger.info("thing is removed: " + path);
		}
		return removed;
	}

	public void setRemoved(boolean removed) {
		this.removed = removed;
		
		for(Thing child : thing.childs){
			child.getMetadata().setRemoved(removed);
		}
	}

	public String getCoderType() {
		return coderType;
	}

	public void setCoderType(String coderType) {
		this.coderType = coderType;
	}
}