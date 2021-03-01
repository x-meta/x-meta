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

import java.util.Map;

import org.xmeta.World;

import ognl.ClassResolver;

/**
 * 一般启动后Ognl找不到world的lib下的静态类。
 * 
 * @author zhangyuxiang
 *
 */
public class OgnlClassResolver implements ClassResolver{
	private static OgnlClassResolver instance = new OgnlClassResolver();
	
	public static OgnlClassResolver getInstance(){
		return instance;
	}
	
    @SuppressWarnings("rawtypes")
	public Class<?> classForName(String className, Map context) throws ClassNotFoundException{
    	return World.getInstance().getClassLoader().loadClass(className);
    }
}