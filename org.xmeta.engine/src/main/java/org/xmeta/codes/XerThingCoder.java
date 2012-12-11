/*
 * Copyright 2007-2008 The X-Meta.org.
 * 
 * Licensed to the X-Meta under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The X-Meta licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xmeta.codes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

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
	public static String TYPE = "xer";
	
	@Override
	public void decode(Thing thing, InputStream in, long loastModifyed) {
		thing.beginModify();
		try{
			XerCoder.decode(thing, in);			
		}catch(Exception e){
			throw new ThingCoderException(e);
		}finally{
			thing.endModify(false);
		}
	}

	
	@Override
	public void decodeIndex(ThingIndex thingIndex, InputStream in) {
		try{
			//读取一部分应该足够包含索引信息的数据
			int length = in.available();
			byte[] bytes = new byte[length];
			in.read(bytes);
			
			Thing thing = XerCoder.decodeAttributeOnly(bytes, 0);
	        
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
