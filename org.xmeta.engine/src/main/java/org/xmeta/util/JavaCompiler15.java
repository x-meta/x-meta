package org.xmeta.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.xmeta.World;

public class JavaCompiler15 {
	static World world = World.getInstance();
	
	@SuppressWarnings("unchecked")
	public static void compile(String classPath, String fileName) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		Class mainClass = Class.forName("com.sun.tools.javac.Main");
		Method compileMethod = mainClass.getDeclaredMethod("compile", new Class[]{String[].class});
		compileMethod.invoke(mainClass, new Object[]{new String[]{
				"-cp", classPath,
				"-d", world.getPath() + "/actionClasses",
				fileName + ".java"
		}});
	}
}
