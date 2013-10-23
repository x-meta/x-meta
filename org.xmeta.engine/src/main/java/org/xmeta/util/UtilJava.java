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

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
	 * @param collection
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Iterable<Object> getIterable(Object collection){
		if(collection == null){
			//空返回一个空列表
			return Collections.emptyList();
		}else if(collection.getClass().isArray()){
			//数组转列表
			List<Object> list = new ArrayList<Object>();
			int length = Array.getLength(collection); 
			for(int i=0; i<length; i++){
				list.add(Array.get(collection, i));
			}
			return list;
		}else if(collection instanceof Iterable){
			//本身就是Iterable
			return (Iterable<Object>) collection; 
		}else{
			//构造一个Iterable
			List<Object> list = new ArrayList<Object>();
			list.add(collection);
			return list;
		}
	}
	
	/**
	 * 通过参数值列表获取对象的方法。
	 * 
	 * @param object
	 * @param methodName
	 * @param params
	 * @return
	 */
	public static Method getMethod(Class<?> cls, String methodName, List<Object> params){
		//不能直接使用cls.getDeclaredMethod，因为int,boolean等会编程Integer,Boolean类，使用这个方法取不到方法
        //Method method = cls.getDeclaredMethod(methodName, parameterTypes);
		
		Method method = null;
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
                method = m;
                break;
            }
        }
        
        return method;
	}
}