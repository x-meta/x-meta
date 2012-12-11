package org.xmeta.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.xmeta.ThingManager;
import org.xmeta.World;

public class ThingClassLoader extends URLClassLoader {
	/** 类装载的路径 */
	protected String classPath = "";
	
	/**
	 * 构建一个世界级的类装载器。
	 * 
	 * @param parent
	 */
	public ThingClassLoader(ClassLoader parent) {
		super(new URL[] {}, parent);

		File libFile = new File(World.getInstance().getPath() + "/lib/");
		addJarOrZip(libFile);
//		initClassPath();
	}

	/**
	 * 从一个目录或者jar或zip添加类库。
	 * 
	 * @param dir
	 */
	public void addJarOrZip(File dir){
		if(!dir.exists()){
			return;
		}
	
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				addJarOrZip(file);
			} else {
				String fileName = file.getName().toLowerCase();
				if (fileName.endsWith(".jar") || fileName.endsWith(".zip")) {
					classPath += File.pathSeparator + file.getAbsolutePath();
					
					try {
						this.addURL(file.toURI().toURL());
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public String getClassPath() {
		return classPath;
	}
	
	public String getCompileClassPath() {
		return classPath ;
	}
}
