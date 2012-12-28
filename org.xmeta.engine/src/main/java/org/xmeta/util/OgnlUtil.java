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
	 * @param path  是事物的属性值
	 * @param root
	 * @return
	 * @throws OgnlException
	 */
	public static Object getValue(Thing thing, String pathAttributeName, String pathAttributeValue, Object root) throws OgnlException{
		String key = CACHE + pathAttributeName;
		PathCache pathCache = (PathCache) thing.getData("key");
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
	
	static class PathCache{
		//事物缓存
		long lastModified;
		
		//Ognl表达式
		Object expression;
	}
}
