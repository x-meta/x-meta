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

import java.net.URL;
import java.net.URLClassLoader;

/**
 * 动作事物的类装载器，每个动作都有一个自己的类装载器实例，动作加载器指定的类有自己来加载。
 * 
 * @author zyx
 *
 */
public class ActionClassLoader extends URLClassLoader {
	public ActionClassLoader(URL[] urls, ClassLoader parent) {		
		super(urls, parent);		
		
	}
	
}