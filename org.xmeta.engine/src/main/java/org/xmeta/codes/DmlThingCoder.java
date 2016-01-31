package org.xmeta.codes;

import java.io.InputStream;
import java.io.OutputStream;

import org.xmeta.Thing;
import org.xmeta.ThingCoder;
import org.xmeta.ThingCoderException;
import org.xmeta.ThingIndex;
import org.xmeta.World;

/**
 * 
 * 为了配置动态模型专有的文件名dml而编写的事物编码，实际上它是TxtThingCoder和XmlThingCoder的组合。
 * 
 * 
 * 
 * @author zyx
 *
 */
public class DmlThingCoder implements ThingCoder{
	private static ThingCoder xmlThingCoder = World.getInstance().getThingCoder("xer.xml");
	private static ThingCoder txtThingCoder = World.getInstance().getThingCoder("xer.txt");
	static{
		if(xmlThingCoder == null){
			xmlThingCoder = new XmlThingCoder();
		}
		
		if(txtThingCoder == null){
			txtThingCoder = new XmlThingCoder();
		}
	}
	private static String TYPE = "dml";
	private static String TYPE_TXT = "dml_txt";
	private static String TYPE_XML = "dml_xml";
	
	@Override
	public void encode(Thing thing, OutputStream out) {
		if(TYPE_XML.equals(thing.getMetadata().getCoderType())){
			xmlThingCoder.encode(thing, out);
		}else{
			txtThingCoder.encode(thing, out);
		}
	}

	@Override
	public void decode(Thing thing, InputStream in, long lastModifyed)  {
		in.mark(10);
		try{
			int firstChar = in.read();
			in.reset();
			
			if(firstChar == '^'){
				txtThingCoder.decode(thing, in, lastModifyed);
				thing.getMetadata().setCoderType(TYPE_TXT);
			}else{
				xmlThingCoder.decode(thing, in, lastModifyed);
				thing.getMetadata().setCoderType(TYPE_XML);
			}
		}catch(Exception e){
			throw new ThingCoderException(e);
		}
		
	}

	@Override
	public void decodeIndex(ThingIndex thingIndex, InputStream in,	long lastModifyed) {
	
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean acceptType(String type) {
		return TYPE.equals(type) || TYPE_TXT.equals(type) || TYPE_XML.equals(type);
	}

}
