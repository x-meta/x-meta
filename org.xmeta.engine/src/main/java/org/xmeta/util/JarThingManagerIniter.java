package org.xmeta.util;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmeta.World;
import org.xmeta.thingManagers.JarThingManager;

/**
 * 当World初始化时，搜索ClassPath中的jar，如果jar中包含.dml，那么把它作为一个JarThingManager加载到XWorker中。
 * 
 * @author zyx
 *
 */
public class JarThingManagerIniter implements Runnable{
	private static Logger logger = LoggerFactory.getLogger(JarThingManagerIniter.class);
	
	private boolean hasProtocol(String path) {
		try {
			URL purl = new URL(path);
			return purl.getProtocol() != null;
		}catch(Exception e) {
			return false;
		}
	}
	
	@Override
	public void run() {
		try {
			World world = World.getInstance();
			for(String path : world.getClassLoader().getClassPath().split("[" + File.pathSeparator + "]")){
				String lpath = path.toLowerCase();
				if(lpath.endsWith(".jar")){
					try {
						URL url = null;
						if(!hasProtocol(path)) {
							url = new URL("jar:file:" + path + "!/.dml");
						} else {
							url = new URL("jar:" + path + "!/.dml");
						}
												
						InputStream in = url.openStream();
						if(in != null) {
							Properties p = new Properties();
							p.load(in);
							
							String name = p.getProperty("name");
							if(name != null) {
								JarThingManager jarThingManager = new JarThingManager(name, new File(path));
								world.addThingManager(jarThingManager);
							}
						}						
					}catch(Exception ee) {
						logger.debug("init jar file manager exception, jar={}, exception={}", path, ee.getMessage());
					}					
				}
			}
		}catch(Exception e) {
			logger.warn("Init jar file manager exception, " + e.getMessage());
		}				
	}

}
