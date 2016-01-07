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

import ognl.Ognl;
import ognl.OgnlException;

import org.xmeta.Thing;

public class OgnlUtil {
	private static String CACHE = "__ognl_attrPathCache_";
	/**
	 * 通过Ognl表达式取值， 事物的属性是Ognl的表达式。使用这种方式缓存了Ognl表达式。
	 * 
	 * @param thing 事物
	 * @param pathAttributeName 属性
	 * @param root 根
	 * @return 值
	 * @throws OgnlException 异常
	 */
	public static Object getValue(Thing thing, String pathAttributeName, Object root) throws OgnlException{
		PathCache pathCache = getPathCache(thing, pathAttributeName);
		if(pathCache == null){
			return null;
		}
		return Ognl.getValue(pathCache.expression, root);
	}
	
	/**
	 * 通过Ognl表达式取值， 事物的属性是Ognl的表达式。使用这种方式缓存了Ognl表达式。
	 * 
	 * @param thing 事物
	 * @param pathAttributeName 属性
	 * @param pathAttributeValue  是事物的属性值，如果为空返回null
	 * @param root 根对象
	 * @return 值
	 * @throws OgnlException 异常
	 */
	public static Object getValue(Thing thing, String pathAttributeName, String pathAttributeValue, Object root) throws OgnlException{
		PathCache pathCache = getPathCache(thing, pathAttributeName);
		if(pathCache == null){
			return null;
		}
		
		return Ognl.getValue(pathCache.expression, root);
	}
	
	public static PathCache getPathCache(Thing thing, String attributeName) throws OgnlException{
		String key = CACHE + attributeName;
		PathCache pathCache = (PathCache) thing.getData(key);
		if(pathCache == null || pathCache.lastModified != thing.getMetadata().getLastModified()){
			String path = thing.getStringBlankAsNull(attributeName);
			if(path == null){
				return null;
			}
			
			if(pathCache == null){
				pathCache = new PathCache();				
			}
			
			pathCache.lastModified = thing.getMetadata().getLastModified();
						
			if(path.startsWith("ognl:")){
				path = path.substring(5, path.length());
			}
			pathCache.expression = Ognl.parseExpression(path);			
			thing.setData(key, pathCache);
		}
		
		return pathCache;
	}
	
	public static Object getCachedExpression(Thing thing, String attributeName) throws OgnlException{
		PathCache pathCache = getPathCache(thing, attributeName);
		
		return pathCache.expression;
	}
	
	public static void setValue(Thing thing, String pathAttributeName, String pathAttributeValue, Object value, Object root) throws OgnlException{
		if(pathAttributeValue == null || "".equals(pathAttributeValue)){
			return;
		}
		
		String key = CACHE + pathAttributeName;
		PathCache pathCache = (PathCache) thing.getData(key);
		if(pathCache == null || pathCache.lastModified != thing.getMetadata().getLastModified()){
			if(pathCache == null){
				pathCache = new PathCache();
				thing.setData(key, pathCache);
			}
			
			pathCache.lastModified = thing.getMetadata().getLastModified();
			pathCache.expression = Ognl.parseExpression(pathAttributeValue);
		}
		
		Ognl.setValue(pathCache.expression, root, value);
		
	}
	
	static class PathCache{
		//事物缓存
		long lastModified;
		
		//Ognl表达式
		Object expression;
	}
}