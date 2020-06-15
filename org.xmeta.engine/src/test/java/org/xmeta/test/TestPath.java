package org.xmeta.test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.xmeta.Path;

public class TestPath {
	private Map<String, Path> pathCache1 = new ConcurrentHashMap<String, Path>(5000);
	private Map<String, Path> pathCache2 = new ConcurrentHashMap<String, Path>(5000);
	
	public Path getPathByCache(String pathStr) {
		Path path = pathCache1.get(pathStr);
		if(path == null){
			path = pathCache2.get(pathStr);
			if(path == null){
				path = new Path(pathStr);
			}
			pathCache1.put(pathStr, path);
		}
		
		return path;
	}
	

	public Path getPath(String pathStr) {
		return new Path(pathStr);
	}
	
	public static void main(String[] args) {
		try {
			TestPath test = new TestPath();
			
			String path = "wingtech.test.http.TestProtocol/@12379/@12382/@12384/@12385/@GroovyAction";	
			long start = System.currentTimeMillis();
			int count = 1000000;
			/*for(int i=0; i<count; i++) {
				test.getPathByCache(path + i);
			}
			System.out.println("GetPath by cache , count=" + count + ", time = " + (System.currentTimeMillis() - start));
			
			start = System.currentTimeMillis();
			for(int i=0; i<count; i++) {
				test.getPathByCache(path + i);
			}
			System.out.println("GetPath by cache2 , count=" + count + ", time = " + (System.currentTimeMillis() - start));
			
			start = System.currentTimeMillis();*/
			System.out.println("Test");
			for(int i=0; i<count; i++) {
				test.getPath(path + i);
			}
			System.out.println("GetPath , count=" + count + ", time = " + (System.currentTimeMillis() - start));
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
