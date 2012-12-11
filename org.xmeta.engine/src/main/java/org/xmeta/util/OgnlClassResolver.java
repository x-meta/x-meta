package org.xmeta.util;

import java.util.Map;

import ognl.ClassResolver;

import org.xmeta.World;

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
	
    @SuppressWarnings("unchecked")
	public Class<?> classForName(String className, Map context) throws ClassNotFoundException{
    	return World.getInstance().getClassLoader().loadClass(className);
    }
}