package org.xmeta.util;

import org.xmeta.World;

public class ReplaceAllThings {
	public static void main(String args[]){
		try{
			//World.getInstance().init("E:\\work\\xworker\\");
			//World.getInstance().init("D:\\xmeta\\xmeta1.2\\alpha\\");
			try{
				World.getInstance().init("D:\\xworker-1.3.3\\xworker\\");
			}catch(Exception e){
				e.printStackTrace();
			}
			
			RefactorUtil.replaceAll("xworker.app.utils.DbUtil", "xworker.dataObject.utils.DbUtil", new RefactorListener(){

				@Override
				public void onStart(int count) {
				}

				@Override
				public void onCopy(String sourcePath, String targetPath) {
				}

				@Override
				public void onDelete(String sourcePath) {
				}

				@Override
				public void onUpdated(String path) {
					System.out.println("updated: " + path);
				}

				@Override
				public void notMidify(String path) {
					//System.out.println("not modify: " + path);
				}

				@Override
				public void finish() {
					System.exit(0);
				}
				
			});
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
