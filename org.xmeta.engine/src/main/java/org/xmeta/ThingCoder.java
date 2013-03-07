/*
 * Copyright 2007-2009 The X-Meta.org.
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
package org.xmeta;

import java.io.InputStream;
import java.io.OutputStream;


/**
 * 事物编码器。
 * 
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public interface ThingCoder {
	/**
	 * 编码一个事物到输出流中。
	 * 
	 * @param thing
	 * @param out
	 * @throws ThingCoderException
	 */
	public void encode(Thing thing, OutputStream out);
		
	/**
	 * 解码一个输入流到事物中。
	 * 
	 * @param in
	 * @param lastModifyed 如果为0表示解码时不能获取时间，可能需要在格式中去解
	 * @return
	 * @throws ThingCoderException
	 */
	public void decode(Thing thing, InputStream in, long lastModifyed);
	
	/**
	 * 只解码用于索引事物的部分，事物索引通常用于UI的导航中。
	 * 
	 * @param in
	 * @return
	 */
	public void decodeIndex(ThingIndex thingIndex, InputStream in, long lastModifyed);
	
	/**
	 * 返回编码器的类型，通常是文件的后缀名。
	 * 
	 * @return
	 */
	public String getType();
}
