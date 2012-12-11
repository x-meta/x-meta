package org.xmeta.test;

import org.xmeta.World;
import org.xmeta.util.RefactorListener;
import org.xmeta.util.RefactorUtil;

public class TestRefactory {
	public static void main(String args[]){
		try{
			World.getInstance().init("D:\\xmeta\\xmeta1.2\\alpha\\");
			String sourcePath = "test:test:temp.test";
			String targetPath = "test:test:test";
			
			RefactorUtil.getInstance().refactor(sourcePath, targetPath, new RefactorListener(){
				@Override
				public void onCopy(String sourcePath, String targetPath) {
					System.out.println("Copy " + sourcePath + " to " + targetPath);
				}

				@Override
				public void onDelete(String sourcePath) {
					System.out.println("Delete " + sourcePath);
				}

				@Override
				public void notMidify(String path) {
					System.out.println("NotModify " + path);
				}

				@Override
				public void onStart(int count) {
					System.out.println("Start...");
				}

				@Override
				public void onUpdated(String path) {
					System.out.println("Update " + path);
				}

				@Override
				public void finish() {
					System.out.println("finished");
					System.exit(0);
				}
				
			});
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
