package org.xmeta.util;

import java.io.File;
import java.net.URL;

import org.xmeta.World;

public class ProjectClassLoader extends ThingClassLoader{
	public ProjectClassLoader(File projectDir) {
		super(new URL[0], World.getInstance().getClassLoader());
		
		//添加项目目录下可能存在的类库路径
		addJarOrZip(new File(projectDir, "lib"));
		addJarOrZip(new File(projectDir, "WEB-INF/lib"));
		addJarOrZip(new File(projectDir, "WEB-INF/classes"));
		addJarOrZip(new File(projectDir, "target/classes/"));
		addJarOrZip(new File(projectDir, "bin"));
				
		World world = World.getInstance();
		String worldPath = world.getPath();
		
		File libFile = new File(worldPath + "/lib/");
		addJarOrZip(libFile);
		
		addJarOrZip(new File(worldPath + "/os/lib/lib_" + world.getOS()));
		addJarOrZip(new File(worldPath + "/os/lib/lib_" + world.getOS() + "_" + world.getJVMBit()));
	}
}
