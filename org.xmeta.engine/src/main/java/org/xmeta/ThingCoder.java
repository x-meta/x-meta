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