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
package org.xmeta.util;

import java.io.OutputStream;
import java.io.Writer;

import org.apache.log4j.Layout;
import org.apache.log4j.WriterAppender;

public class Log4jConsoleAppender extends WriterAppender {
	public static Log4jConsoleAppender instance;
	private static OutputStream out;
	
	public Log4jConsoleAppender() {
		super();
		
		instance = this;
	}

	public Log4jConsoleAppender(Layout layout, OutputStream os) {
		super(layout, os);
		
		instance = this;
		
	}

	public Log4jConsoleAppender(Layout layout, Writer writer) {
		super(layout, writer);
		
		instance = this;
	}
	
	@Override
	public void activateOptions() {
		if(out != null){
			this.setWriter(createWriter(out));
		}
		
		super.activateOptions();
	}

	public static void setStaticOutputStream(OutputStream out){
		Log4jConsoleAppender.out = out;
		if(instance != null){
			instance.closeWriter();
			instance.setWriter(instance.createWriter(out));
		}
	}

}