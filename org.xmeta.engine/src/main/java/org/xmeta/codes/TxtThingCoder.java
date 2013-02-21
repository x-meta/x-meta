package org.xmeta.codes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmeta.Thing;
import org.xmeta.ThingCoder;
import org.xmeta.ThingCoderException;
import org.xmeta.ThingIndex;

public class TxtThingCoder implements ThingCoder{
	private static final Logger logger = LoggerFactory.getLogger(XerThingCoder.class);
	
	public static String TYPE = "xer.txt";
	
	@Override
	public void decode(Thing thing, InputStream in, long loastModifyed) {
		thing.beginModify();
		try{
			TxtCoder.decode(thing, in, true);			
		}catch(Exception e){
			logger.error("decode thing error, still return part decoded thing", e);
			//throw new ThingCoderException(e);
		}finally{
			thing.endModify(false);
		}
	}

	
	@Override
	public void decodeIndex(ThingIndex thingIndex, InputStream in) {
		
		try{
			Thing thing = new Thing();
			thing.beginModify();
			TxtCoder.decode(thing, in, false);	
			thing.endModify(false);
			
			thingIndex.label = thing.getMetadata().getLabel();
			thingIndex.description = thing.getString("description");
			thingIndex.descriptors = thing.getString("descriptors");
		}catch(Exception e){
			throw new ThingCoderException(e);
		}
 	}

	@Override
	public void encode(Thing thing, OutputStream out) {
		try {
			TxtCoder.encode(thing, new PrintWriter(out), new HashMap<Thing, String>());
		} catch (IOException e) {
			throw new ThingCoderException(e);
		}
	}


	@Override
	public String getType() {
		return TYPE;
	}

}
