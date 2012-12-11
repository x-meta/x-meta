package org.xmeta.alpha;

import org.xmeta.Thing;
import org.xmeta.World;

public class TestRunShell {
	public static void main(String[] args){
		try{
			//World.getInstance().init("E:\\work\\xworker\\");
			World.getInstance().init("D:\\xworker-1.3.3\\xworker\\");
			//World.getInstance().init("E:\\work\\dist\\xworker-1.3.3\\xworker\\");
			
			//使用事物
			Thing worldExplorer = World.getInstance().getThing("xworker.ide.worldExplorer.swt.SimpleExplorerRunner");			
			worldExplorer.doAction("run");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
 