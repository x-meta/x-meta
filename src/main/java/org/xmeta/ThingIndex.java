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