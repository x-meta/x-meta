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
