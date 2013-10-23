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