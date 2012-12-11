package org.xmeta.test;

import org.xmeta.Thing;
import org.xmeta.World;
import org.xmeta.codes.XmlCoder;

public class TestXmlCodec {
	public static void main(String[] args){
		try{
			World.getInstance().init("E:\\work\\xmeta_alpha\\");
			
			//使用事物
			Thing worldExplorer = World.getInstance().getThing("xmeta.ui.worldExplorer.swt.TaskManager/@shell");			
			
			//测试编码
			String xml = XmlCoder.encodeToString(worldExplorer);
			System.out.println(xml);
			Thing thing = new Thing();
			XmlCoder.parse(thing, xml);
			thing.doAction("run");			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
