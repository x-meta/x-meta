package org.xmeta.codes;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.xmeta.Thing;
import org.xmeta.ThingCoder;
import org.xmeta.ThingCoderException;
import org.xmeta.ThingIndex;

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
	ThingCoder xmlThingCoder = null;
	ThingCoder txtThingCoder = null;

	private static String TYPE = "dml";
	private static String TYPE_TXT = "dml_txt";
	private static String TYPE_XML = "dml_xml";
	
	public DmlThingCoder(ThingCoder xmlThingCoder, ThingCoder txtThingCoder){
		this.xmlThingCoder = xmlThingCoder;
		this.txtThingCoder = txtThingCoder;		
	}
	
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
		BufferedInputStream bin = new BufferedInputStream(in);
		bin.mark(10);
		try{
			int firstChar = bin.read();
			bin.reset();
			
			if(firstChar == '^'){
				txtThingCoder.decode(thing, bin, lastModifyed);
				thing.getMetadata().setCoderType(TYPE_TXT);
			}else{
				xmlThingCoder.decode(thing, bin, lastModifyed);
				thing.getMetadata().setCoderType(TYPE_XML);
			}
			
			bin.close();
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

	@Override
	public String[] getCodeTypes() {
		return new String[]{TYPE_TXT, TYPE_XML};
	}

}
