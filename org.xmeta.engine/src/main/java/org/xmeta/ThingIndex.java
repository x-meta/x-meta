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
package org.xmeta;

/**
 * 事物的索引。
 * 
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public class ThingIndex {
	/** 事物的名称 */
	public String name;
	
	/** 事物的标签 */
	public String label;
	
	/** 事物的路径 */
	public String path;
	
	/** 事物的目录名 */
	public String categoryName;
	
	/** 事物的描述者列表 */
	public String descriptors;
	
	/** 事物的继承者列表 */
	public String extendsStr;
	
	/** 事物的描述 */
	public String description;		
	
	/** 所属的事物管理器 */
	public ThingManager thingManager;
	
	/** 最后修改日期 */
	public long lastModified;
	
	/** 
	 * 返回包含目录的事物的全名。
	 * 
	 * @return 事物的全名
	 */ 
	public String getThingName(){
		if(categoryName == null || categoryName.equals("")){
			return name;
		}else{
			return categoryName + "." + name;
		}
	}
	
	public String getName(){
		return name;
	}
}