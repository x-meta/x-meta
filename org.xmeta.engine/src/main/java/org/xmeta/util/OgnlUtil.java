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

import ognl.Ognl;
import ognl.OgnlException;

import org.xmeta.Thing;

public class OgnlUtil {
	private static String CACHE = "__ognl_attrPathCache_";
	/**
	 * 通过Ognl表达式取值， 事物的属性是Ognl的表达式。使用这种方式缓存了Ognl表达式。
	 * 
	 * @param thing
	 * @param pathAttributeName
	 * @param root
	 * @return
	 * @throws OgnlException
	 */
	public static Object getValue(Thing thing, String pathAttributeName, Object root) throws OgnlException{
		return getValue(thing, pathAttributeName, thing.getString(pathAttributeName), root);
	}
	
	/**
	 * 通过Ognl表达式取值， 事物的属性是Ognl的表达式。使用这种方式缓存了Ognl表达式。
	 * 
	 * @param thing
	 * @param pathAttribute
	 * @param path  是事物的属性值，如果为空返回null
	 * @param root
	 * @return
	 * @throws OgnlException
	 */
	public static Object getValue(Thing thing, String pathAttributeName, String pathAttributeValue, Object root) throws OgnlException{
		if(pathAttributeValue == null || "".equals(pathAttributeValue)){
			return null;
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
		
		return Ognl.getValue(pathCache.expression, root);
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