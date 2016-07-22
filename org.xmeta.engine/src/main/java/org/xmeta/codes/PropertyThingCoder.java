package org.xmeta.codes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmeta.Thing;
import org.xmeta.ThingCoder;
import org.xmeta.ThingCoderException;
import org.xmeta.ThingIndex;

public class PropertyThingCoder  implements ThingCoder{
	private static final Logger logger = LoggerFactory.getLogger(PropertyThingCoder.class);
	
	public static String TYPE = "xer.properties";
	
	@Override
	public void decode(Thing thing, InputStream in, long lastModifyed) {
		thing.beginModify();
		try{
			PropertyCoder.decode(thing, in, true, lastModifyed);			
		}catch(Exception e){
			logger.error("decode thing error, still return part decoded thing, thing=" + thing.getMetadata().getPath(), e);
			//throw new ThingCoderException(e);
		}finally{
			thing.endModify(false);
		}
	}

	
	@Override
	public void decodeIndex(ThingIndex thingIndex, InputStream in, long lastModifyed) {
		
		try{
			Thing thing = new Thing();
			thing.getMetadata().setPath(thingIndex.getName());
			thing.beginModify();
			PropertyCoder.decode(thing, in, false, 0);	
			thing.endModify(false);
			
			thingIndex.label = thing.getMetadata().getLabel();
			thingIndex.description = thing.getString("description");
			thingIndex.descriptors = thing.getString("descriptors");
			thingIndex.lastModified = lastModifyed;
		}catch(Exception e){
			throw new ThingCoderException(e);
		}
 	}

	@Override
	public void encode(Thing thing, OutputStream out) {
		try {
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, PropertyCoder.STRING_ENCODING));
			
			PropertyCoder.encode(thing, writer, new HashMap<Thing, String>());
			writer.flush();
		} catch (IOException e) {
			throw new ThingCoderException(e);
		}
	}


	@Override
	public String getType() {
		return TYPE;
	}


	@Override
	public boolean acceptType(String type) {
		return TYPE.equals(type);
	}


	@Override
	public String[] getCodeTypes() {
		return new String[]{TYPE};
	}


}
