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

import java.util.HashMap;
import java.util.Map;

import org.xmeta.World;

/**
 * 事物注册器，可以注册事物、项目、事物管理器和包等。<p/>
 * 
 * 事物管理器的作用是在Java代码中可能会引用到某些事物，而这些事物可能会变更，为了避免因
 * 事物变更而程序无法使用，故使用此类注册事物。<p/>
 * 
 * 注册的事物名靠使用者管理，所以可能会产生冲突，所以在命名时最好加上自身应用的前缀，并且
 * 名称需要自己维护。<p/>
 * 
 * 可以在项目初始化时注册事物，也可以在运行时注册事物。
 * 
 * @author zhangyuxiang
 *
 */
public class ThingRegistor {
	private static Map<String, String> things = new HashMap<String, String>();
	
	/**
	 * 注册一个路径。
	 * 
	 * @param key
	 * @param thingPath
	 */
	public static void regist(String key, String thingPath){
		things.put(key, thingPath);
	}
	
	/**
	 * 返回指定key的路径的对应事物。
	 * 
	 * @param key
	 * @return
	 */
	public static Object get(String key){
		String path = things.get(key);
		if(path != null){
			return World.getInstance().get(path);
		}else{
			return null;
		}
	}
	
	/**
	 * 返回路径。
	 * 
	 * @param key
	 * @return
	 */
	public static String getPath(String key){
		return things.get(key);
	}
}