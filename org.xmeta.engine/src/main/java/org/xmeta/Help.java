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
 * 在运行中可以通过world添加的帮助。
 * 
 * @author zhangyuxiang
 *
 */
public class Help {
	/**
	 * 帮助的标题。
	 */
	String title;
	
	/** 源事物路径，通常是发生需要帮助的事物的路径 */
	String sourcePath;
	
	/** 帮助路径，提供帮助事物的路径 */
	String helpPath;

	public Help(String title, String sourcePath, String helPath) {
		super();
		this.title = title;
		this.sourcePath = sourcePath;
		this.helpPath = helPath;
	}

	public String getTitle() {
		return title;
	}

	public String getSourcePath() {
		return sourcePath;
	}

	public String getHelpPath() {
		return helpPath;
	}
	
	
}