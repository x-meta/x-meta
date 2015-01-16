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
package org.xmeta.cache;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.xmeta.Thing;

public class ThingCache {
	private static Map<String, SoftReference<Thing>> cache = new ConcurrentHashMap<String, SoftReference<Thing>>(600);
		
	/**
	 * 获取缓存。
	 * 
	 * @param path
	 * @return
	 */
	public static Thing get(String path){
		SoftReference<Thing> wr = cache.get(path);
		if(wr == null){
			return null;
		}
		
		Thing thing = wr.get();
		if(thing == null){
			cache.remove(path);
		}else if(thing.getMetadata().isRemoved()){
			//当事物被删除了或者通过文件系统更新的事物时
			cache.remove(path);
			thing = null;
		}
		
		return thing;
	}
	
	/**
	 * 放入缓存。
	 * 
	 * @param path
	 * @param thing
	 */
	public static void put(String path, Thing thing){
		SoftReference<Thing> wr = cache.get(path);
		if( wr == null || wr.get() == null){
			wr = new SoftReference<Thing>(thing);
			cache.put(path, wr);
		}
	}
	
	/**
	 * 删除缓存。
	 * 
	 * @param path
	 */
	public static void remove(String path){
		cache.remove(path);
	}
	
	public static void clear(){
		cache.clear();
	}
}