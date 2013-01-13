package org.xmeta.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UtilClass {
	/** 已知简单类型的缓存 */
	private static Map<String, Class<?>> classCache = new HashMap<String, Class<?>>();
	static{
		classCache.put("int", int.class);
		classCache.put("byte", byte.class);
		classCache.put("short", short.class);
		classCache.put("char", char.class);
		classCache.put("long", long.class);
		classCache.put("folat", float.class);
		classCache.put("double", double.class);
		classCache.put("int[]", int[].class);
		classCache.put("byte[]", byte[].class);
		classCache.put("short[]", short[].class);
		classCache.put("char[]", char[].class);
		classCache.put("long[]", long[].class);
		classCache.put("folat[]", float[].class);
		classCache.put("double[]", double[].class);
	}
	
	public static Class<?> getClassByName(String className) throws ClassNotFoundException{
		if(className == null || "".equals(className)){
			return Class.forName(className); //抛出错误的异常
		}
		
		className = className.trim();
		
		//先从缓存里找
		Class<?> cls = classCache.get(className);
		if(cls != null){
			return cls;
		}
		
		//根据字符串构造类型
		if(className.endsWith("[]")){
			int index = className.indexOf("[");
			String clsName = className.substring(0, index);
			String array = className.substring(index, className.length());
			String[] arrays = array.split("[\\[]"); //数组的维度
			
			cls = Class.forName(clsName);
			for(int i=0; i<arrays.length; i++){
				cls = Array.newInstance(cls, 0).getClass();
			}
		}else{
			cls = Class.forName(className);
		}
		
		//保存缓存
		classCache.put(className, cls);
		
		return cls;
	}
	
	public static void main(String args[]) {
		try{
			List<String> list = new ArrayList<String>();
			System.out.println(list.getClass());
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
