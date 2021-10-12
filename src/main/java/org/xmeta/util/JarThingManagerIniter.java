package org.xmeta.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xmeta.World;
import org.xmeta.thingManagers.JarThingManager;

/**
 * 当World初始化时，搜索ClassPath中的jar，如果jar中包含.dml，那么把它作为一个JarThingManager加载到XWorker中。
 * 
 * @author zyx
 *
 */
public class JarThingManagerIniter implements Runnable{
	//private static Logger logger = LoggerFactory.getLogger(JarThingManagerIniter.class);
	private static final Logger logger = java.util.logging.Logger.getLogger(JarThingManagerIniter.class.getName());
	private static boolean inited = false;

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
						Properties p = getProperties(path, ".dml");
						if(p == null) {
							p = getProperties(path, "dml.properties");
						}
						if(p == null) {
							p = getProperties(path, "dml.prj");
						}
						if(p == null) {
							p = getProperties(path, "META-INF/dml.properties");
						}
						if(p == null) {
							p = getProperties(path, "META-INF/dml.prj");
						}
						if(p != null) {
							String name = p.getProperty("name");
							if(name != null) {
								JarThingManager jarThingManager = new JarThingManager(name, new File(path));
								jarThingManager.init(p);
								world.addThingManager(jarThingManager);
							}
						}						
					}catch(Exception ee) {
						logger.log(Level.FINE, "init jar file manager exception, jar=" + path + ", exception=" + ee.getMessage());
					}					
				}
			}
		}catch(Exception e) {
			logger.log(Level.WARNING, "Init jar file manager exception, " + e.getMessage());
		}finally {
			inited = true;
		}
	}

	public static boolean isInited(){
		return inited;
	}

	public Properties getProperties(String path, String dmlPrjFile) throws IOException {
		URL url = null;
		if(!hasProtocol(path)) {
			url = new URL("jar:file:" + path + "!/" + dmlPrjFile);
		} else {
			url = new URL("jar:" + path + "!/" + dmlPrjFile);
		}
					
		try {
			InputStream in = url.openStream();
			if(in != null) {
				Properties p = new Properties();
				p.load(in);
				in.close();
				return p;
			}else {
				return null;
			}
		}catch(Exception e) {
			return null;
		}
	}
	
}
