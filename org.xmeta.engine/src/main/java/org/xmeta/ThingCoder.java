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
	 * @param thing 事物
	 * @param out 输出流
	 */
	public void encode(Thing thing, OutputStream out);
		
	/**
	 * 解码一个输入流到事物中。
	 * 
	 * @param thing 事物
	 * @param in 输入流
	 * @param lastModifyed 如果为0表示解码时不能获取时间，可能需要在格式中去解
	 */
	public void decode(Thing thing, InputStream in, long lastModifyed);
	
	/**
	 * 只解码用于索引事物的部分，事物索引通常用于UI的导航中。
     * 
	 * @param thingIndex 事物索引
	 * @param in 输入流
	 * @param lastModifyed 如果为0表示解码时不能获取时间，可能需要在格式中去解
	 */
	public void decodeIndex(ThingIndex thingIndex, InputStream in, long lastModifyed);
	
	/**
	 * 返回编码器的类型，通常是文件的后缀名。
	 * 
	 * @return 类型
	 */
	public String getType();
	
	/**
	 * 返回编码器所支持的所有类型。
	 * 
	 * @return 编码器所支持的所有类型。
	 */
	public String[] getCodeTypes();
	
	/**
	 * 是否是制定类型的编码者。
	 * 
	 * @param type 类型
	 * @return 如果是返回ture，否则返回false
	 */
	public boolean acceptType(String type);
}