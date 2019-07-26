/*******************************************************************************
* Copyright 2007-2013 See AUTHORS file.
 * 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
******************************************************************************/
package org.xmeta.codes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;

import org.xmeta.Thing;
import org.xmeta.ThingCoder;
import org.xmeta.ThingCoderException;
import org.xmeta.ThingIndex;

/**
 * 提供一种文本格式的编码，目的是保持读写性能以及可以方便的在版本管理中合并。
 * 
 * @author Administrator
 *
 */
public class TxtThingCoder implements ThingCoder{
	//private static final Logger logger = LoggerFactory.getLogger(XerThingCoder.class);
	
	public static String TYPE = "xer.txt";
	
	@Override
	public void decode(Thing thing, InputStream in, long lastModifyed) {
		thing.beginModify();
		try{
			TxtCoder.decode(thing, in, true, lastModifyed);			
		}catch(Exception e){
			//logger.error("decode thing error, still return part decoded thing, thing=" + thing.getMetadata().getPath(), e);
			throw new ThingCoderException("thing=" + thing.getMetadata().getPath(), e);
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
			TxtCoder.decode(thing, in, false, 0);	
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
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, TxtCoder.STRING_ENCODING));
			
			TxtCoder.encode(thing, writer, new HashMap<Thing, String>());
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