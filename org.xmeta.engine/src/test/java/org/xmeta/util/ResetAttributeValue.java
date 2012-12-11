package org.xmeta.util;

import java.io.FileInputStream;

import org.xmeta.Thing;
import org.xmeta.World;

public class ResetAttributeValue {
	public static void main(String args[]){
		try{
			//World.getInstance().init("E:\\work\\xmeta_alpha\\");
			World.getInstance().init("D:\\xmeta\\xmeta1.2\\alpha\\");
			
			Thing thing = World.getInstance().getThing("xmeta.ide.worldExplorer.swt.dataExplorerParts.ThingEditor/@rightComposite/@contentComposite/@menuBarComposite/@coolBar/@descButtonCoolItem/@descButtonToolBar/@descComboItem/@control/@160/@895/@descriptSelection/@GroovyAction");
			FileInputStream fin = new FileInputStream("code.txt");
			byte[] bytes = new byte[fin.available()];
			fin.read(bytes);
			fin.close();
			thing.set("code", new String(bytes));
			thing.save();
			
			System.exit(0);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
		
}
