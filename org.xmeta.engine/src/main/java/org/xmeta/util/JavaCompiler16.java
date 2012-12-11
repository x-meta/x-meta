package org.xmeta.util;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.xmeta.World;

public class JavaCompiler16 {
	static World world = World.getInstance();
		
	public static boolean compile(String classPath, File codeFile){
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if(compiler != null){
			StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

			Iterable<? extends JavaFileObject> compilationUnits1 =
			fileManager.getJavaFileObjectsFromFiles(Arrays.asList(codeFile));
			
			List<String> options = Arrays.asList("-cp", classPath, "-d", world.getPath() + "/actionClasses");
			compiler.getTask(null, fileManager, null, options, null, compilationUnits1).call();
			
			return true;
		}else{
			return false;
		}
	}
	
}
