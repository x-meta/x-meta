package org.xmeta.codes;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.xmeta.Thing;
import org.xmeta.ThingCoder;
import org.xmeta.ThingCoderException;
import org.xmeta.ThingIndex;
import org.xmeta.World;
import org.xmeta.cache.ThingCache;

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
	ThingCoder propertyThingCoder = null;

	public static String TYPE = "dml";
	public static String TYPE_TXT = "dml_txt";
	public static String TYPE_XML = "dml_xml";
	public static String TYPE_PROPERTY = "dml_property";
	
	public DmlThingCoder(ThingCoder xmlThingCoder, ThingCoder txtThingCoder, ThingCoder propertyThingCoder){
		this.xmlThingCoder = xmlThingCoder;
		this.txtThingCoder = txtThingCoder;		
		this.propertyThingCoder = propertyThingCoder;
		
	}
	
	@Override
	public void encode(Thing thing, OutputStream out) {
		if(TYPE_XML.equals(thing.getMetadata().getCoderType())){
			xmlThingCoder.encode(thing, out);
		}else if(TYPE_PROPERTY.equals(thing.getMetadata().getCoderType())){
			propertyThingCoder.encode(thing, out);
		}else{
			txtThingCoder.encode(thing, out);
		}
	}

	@Override
	public void decode(Thing thing, InputStream in, long lastModifyed)  {				
		try{
			BufferedInputStream bin = null;
			
			if(in.available() == 0){
				//有的输入流available=0并不代表没有数据
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				if(thing.getMetadata().getThingManager() == World.getInstance().getClassThingManager()) {				
					byte[] bytes = new byte[1024 * 8];
					int length = -1;
					while((length = in.read(bytes)) != -1) {
						bout.write(bytes, 0, length);
					}
				}
				if(bout.toByteArray().length == 0) {
					//确实为空，允许空的文件为事物，这样编辑器中可以先创建文件
					return;
				}else {
					bin = new BufferedInputStream(new ByteArrayInputStream(bout.toByteArray()));
				}
			}else {				
				bin = new BufferedInputStream(in);
			}
			bin.mark(10);
		
			int firstChar = bin.read();
			bin.reset();
			
			if(firstChar == '^'){
				txtThingCoder.decode(thing, bin, lastModifyed);
				thing.getMetadata().setCoderType(TYPE_TXT);
			}else if(firstChar == PropertyCoder.TYPE_LASTMODIFIED){
				propertyThingCoder.decode(thing, bin, lastModifyed);
				thing.getMetadata().setCoderType(TYPE_PROPERTY);
			}else{
				ThingCache.put(thing.getMetadata().getPath(), thing);
				xmlThingCoder.decode(thing, bin, lastModifyed);				
				XmlCoder.initDefaultValues(thing);
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
		return TYPE.equals(type) || TYPE_TXT.equals(type) || TYPE_XML.equals(type) || TYPE_PROPERTY.equals(type);
	}

	@Override
	public String[] getCodeTypes() {
		return new String[]{TYPE_XML, TYPE_PROPERTY, TYPE_TXT};
	}

}
