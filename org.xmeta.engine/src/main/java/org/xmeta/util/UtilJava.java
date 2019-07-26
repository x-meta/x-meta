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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.xmeta.ActionContext;

/**
 * Java相关的一些工具类。
 * 
 * @author Administrator
 *
 */
public class UtilJava {
	/**
	 * 把指定对象转换成Iterable，如果是数据或Iterable本身的条目也是返回的Iteralbe的条目，
	 * null返回一个空List，其他包装成一个Iterable，把对象放到Iterable条目中。
	 * @param collection 集合
	 * @return 可遍历对象
	 */
	@SuppressWarnings("unchecked")
	public static <T> Iterable<T> getIterable(Object collection){
		if(collection == null){
			//空返回一个空列表
			return Collections.emptyList();
		}else if(collection.getClass().isArray()){
			//数组转列表
			List<T> list = new ArrayList<T>();
			int length = Array.getLength(collection); 
			for(int i=0; i<length; i++){
				list.add((T) Array.get(collection, i));
			}
			return list;
		}else if(collection instanceof Iterable){
			//本身就是Iterable
			return (Iterable<T>) collection; 
		}else{
			//构造一个Iterable
			List<T> list = new ArrayList<T>();
			list.add((T) collection);
			return list;
		}
	}
	
	public static <T> Iterator<T> getIterator(Object collection){
		Iterable<T> it = getIterable(collection);
		return it.iterator();
	}
	
	/**
	 * 通过参数值列表获取对象的方法。
	 * 
	 * @param cls 类 
	 * @param methodName 方法名称
	 * @param params 参数
	 * @return 方法
	 */
	public static Method getMethod(Class<?> cls, String methodName, List<Object> params){
		//不能直接使用cls.getDeclaredMethod，因为int,boolean等会编程Integer,Boolean类，使用这个方法取不到方法
        //Method method = cls.getDeclaredMethod(methodName, parameterTypes);
		
		List<Method> methods = new ArrayList<Method>();
        for(Method m : cls.getDeclaredMethods()){    
            if(!m.getName().equals(methodName)){
                continue;
            }
            
            boolean ok = true;
            Class<?>[] ptypes = m.getParameterTypes();
            if(ptypes.length == params.size()){
                for(int i=0; i<ptypes.length; i++){
                    if(params.get(i) != null){
                        if(!ptypes[i].isInstance(params.get(i))){
                            //String pname = ptypes[i].getName();
                            Object param = params.get(i);
                            if("boolean".equals(ptypes[i]) && param instanceof Boolean){
                                continue;
                            }
                            if("int".equals(ptypes[i]) && param instanceof Number){
                                continue;
                            }
                            if("byte".equals(ptypes[i]) && param instanceof Number){
                                continue;
                            }
                            if("short".equals(ptypes[i]) && param instanceof Number){
                                continue;
                            }
                            if("long".equals(ptypes[i]) && param instanceof Number){
                                continue;
                            }
                            if("float".equals(ptypes[i]) && param instanceof Number){
                                continue;
                            }
                            if("double".equals(ptypes[i]) && param instanceof Number){
                                continue;
                            }
                            if("char".equals(ptypes[i]) && param instanceof Number){
                                continue;
                            }
                            break;
                        }                
                    }
                }
            }else{
                ok = false;
            }
            
            if(ok){
                methods.add(m);
                //break;
            }
        }
        
        if(methods.size() == 0){
        	return null;
        }else if(methods.size() == 1){
        	return methods.get(0);
        }else{
        	//多个method符合条件，选择最适合的哪一个
        	int[][] lvs = new int[methods.size()][params.size() + 1];
        	for(int k=0; k<methods.size(); k++){
        		Method m = methods.get(k);
        		Class<?>[] ptypes = m.getParameterTypes();
        		for(int i=0; i<params.size(); i++){
	        		if(params.get(i) != null){
	        			Object param = params.get(i);
	        			Class<?> parentCls = param.getClass();
	        			while(parentCls != null){
	        				lvs[k][i] ++;
	        				if(parentCls ==ptypes[i] ){
	        					break;
	        				}
	        				parentCls = parentCls.getSuperclass();
	        			}	        				        			
	        		}
        		}
        	}
        	for(int i=0; i<lvs.length; i++){
        		int maxLv = 0;
        		for(int n=0; n<lvs[i].length - 1; n++){
        			if(maxLv < lvs[i][n]){
        				maxLv = lvs[i][n];
        			}
        		}
        		lvs[i][lvs[i].length-1] = maxLv;
        	}
        	
        	//找出层数最少的那个
        	int index = 0;
        	int minLv = 10000;
        	for(int i=0; i<lvs.length; i++){
        		if(lvs[i][lvs[i].length -1] < minLv){
        			index = i;
        			minLv = lvs[i][lvs[i].length - 1];
        		}
        	}
        	
        	return methods.get(index);
        }
	}	
	
	/**
	 * 调用参数是ActionContext的静态方法。
	 * 
	 * @param className 类名
	 * @param methodName 方法名
	 * @param actionContext 变量上下文
	 * 
	 * @return 方法的执行结果
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws ClassNotFoundException 
	 */
	public static Object invokeMethod(String className, String methodName, ActionContext actionContext) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException{
		Class<?> cls = Class.forName(className);
		if(cls != null){
			Method method = cls.getDeclaredMethod(methodName, ActionContext.class);
			if(method != null){
				return method.invoke(null, actionContext);
			}
		}
		
		return null;
	}
	
	/**
	 * 调用参数是ActionContext的静态方法。
	 * 
	 * @param className 类名
	 * @param methodName 方法名
	 * @param paramTypes 参数类型
	 * @param args 参数列表
	 * 
	 * @return 方法的执行结果
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws ClassNotFoundException 
	 */
	public static Object invokeMethod(String className, String methodName, Class<?>[] paramTypes, Object[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException{
		Class<?> cls = Class.forName(className);
		if(cls != null){
			Method method = cls.getDeclaredMethod(methodName, paramTypes);
			if(method != null){
				return method.invoke(null, args);
			}
		}
		
		return null;
	}
	
	/**
	 * 一个简单的实例化对象的方法。
	 * 
	 * @param className 类名
	 * @param objects 参数列表
	 * @return
	 * @throws ClassNotFoundException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	@SuppressWarnings("unchecked")
	public static <T> T newInstance(String className, Object ...objects) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?> cls = Class.forName(className);
		if(cls != null){
			Constructor<?> cons = getConstructor(cls, objects);
			if(cons != null) {
				return (T) cons.newInstance(objects);
			}else {
				return null;
			}
		}
		
		return null;
	}
	
	/**
	 * 根据参数获取相应的构造函数。
	 * 
	 * @param cls
	 * @param objects
	 * @return 构造函数
	 */
	public static Constructor<?> getConstructor(Class<?> cls, Object ... objects){
		Class<?> params[] = new Class<?>[objects.length];
		for(int i=0; i<objects.length; i++) {
			if(objects[i] != null) {
				params[i] = objects[i].getClass();
			}
		}
		
		for(Constructor<?> cons : cls.getConstructors()) {			
			if(cons.getParameterCount() == objects.length) {
				Class<?> paramsTypes[] = cons.getParameterTypes();
				boolean match = true;
				for(int i=0; i<params.length; i++) {
					if(params[i] != null && !paramsTypes[i].isAssignableFrom(params[i])) {
						match = false;
						break;
					}
				}
				
				if(match) {
					return cons;
				}
			}
		}
		
		return null;
	}
	
	
}