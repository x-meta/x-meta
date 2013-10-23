/*
    X-Meta Engine。
    Copyright (C) 2013  zhangyuxiang

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
    For alternative license options, contact the copyright holder.

    Emil zhangyuxiang@tom.com
 */
package org.xmeta.codes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmeta.Thing;
import org.xmeta.ThingCoder;
import org.xmeta.ThingCoderException;
import org.xmeta.ThingIndex;

/**
 * Xer格式的事物编码器，是X-Meta自定义的事物编码器，为了快速度读取和存储事物。
 * 
 * @author  <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public class XerThingCoder implements ThingCoder{
	private static final Logger logger = LoggerFactory.getLogger(XerThingCoder.class);
	
	public static String TYPE = "xer";
	
	@Override
	public void decode(Thing thing, InputStream in, long loastModifyed) {
		thing.beginModify();
		try{
			XerCoder.decode(thing, in);			
		}catch(Exception e){
			logger.error("decode thing error, still return part decoded thing", e);
			//throw new ThingCoderException(e);
		}finally{
			thing.endModify(false);
		}
	}

	
	@Override
	public void decodeIndex(ThingIndex thingIndex, InputStream in, long lastModifyed) {
		try{
			//读取一部分应该足够包含索引信息的数据
			int length = in.available();
			byte[] bytes = new byte[length];
			in.read(bytes);
			
			Thing thing = XerCoder.decodeAttributeOnly(bytes, 0);
	        
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
			XerCoder.encode(thing, out, new HashMap<Thing, String>());
		} catch (IOException e) {
			throw new ThingCoderException(e);
		}
	}


	@Override
	public String getType() {
		return TYPE;
	}

}