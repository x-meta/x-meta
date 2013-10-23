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

/**
 * 事物编码器异常。
 * 
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public class ThingCoderException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public ThingCoderException(Throwable cause){
		super(cause);		
	}
	
	public ThingCoderException(String message, Throwable cause){
		super(message, cause);
	}
	
	public ThingCoderException(String message){
		super(message);
	}
}