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

import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmeta.Thing;
import org.xmeta.ThingCoder;
import org.xmeta.ThingCoderException;
import org.xmeta.ThingIndex;

/**
 * XML格式的事物编码器，是X-Meta自定义的事物编码器，为了快速度读取和存储事物。
 * 
 * @author  <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public class XmlThingCoder implements ThingCoder{
	public static String TYPE = "dml.xml";

	@Override
	public void decode(Thing thing, InputStream in, long lastModified) {
		try{
			Document document = null;
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	
	        factory.setValidating(false);
	        DocumentBuilder builder = factory.newDocumentBuilder();     
	        document = builder.parse(in);
	        
	        Element root = document.getDocumentElement();
	        if(lastModified == 0){
	        	//如果lastModified==0，尝试从根节点的属性lastModified中获取
	        	String lm = root.getAttribute("lastModified");
	        	if(lm != null && !"".equals(lm)){
	        		try{
	        			SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        			lastModified = sf.parse(lm).getTime();
	        		}catch(Exception e){
	        			//e.printStackTrace();
	        		}
	        	}
	        }
	        XmlCoder.parse(thing, null, root, lastModified);
	        XmlCoder.setLastModified(thing, lastModified);
	        //thing.getMetadata().setLastModified(lastModified);
		}catch(Exception e){
			throw new ThingCoderException(e);
		}
	}

	
	@Override
	public void decodeIndex(ThingIndex thingIndex, InputStream in, long lastModified) {
		try{
			Document document = null;
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	
	        factory.setValidating(false);
	        DocumentBuilder builder = factory.newDocumentBuilder();     
	        document = builder.parse(in);
	        
	        Element root = document.getDocumentElement();
	        thingIndex.descriptors = root.getAttribute(Thing.DESCRIPTORS);
	        thingIndex.description = root.getAttribute(Thing.DESCRIPTION);
	        thingIndex.name = root.getAttribute("name");
	        thingIndex.label = root.getAttribute("label");
	        thingIndex.lastModified = lastModified;
		}catch(Exception e){
			throw new ThingCoderException(e);
		}
	}

	@Override
	public void encode(Thing thing, OutputStream out) {
		try{
			XmlCoder.encode(thing, out);
		}catch(Exception e){
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