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
package org.xmeta;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * 动作的异常。
 * 
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public class ActionException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public ActionException(){
        super();
    }

    public ActionException(String message){
        super(message);
    }

    public ActionException(String message, Throwable cause){
        super(message, cause);        
    }
    
    public ActionException(Throwable cause){
        super(cause);        
    }
    
	@Override
	public void printStackTrace() {
		Throwable cause = getCause();
		if(cause != null){
			cause.printStackTrace();
		}else{
			super.printStackTrace();
		}
	}

	@Override
	public void printStackTrace(PrintStream s) {
		Throwable cause = getCause();
		if(cause != null){
			cause.printStackTrace(s);
		}else{
			super.printStackTrace(s);
		}
	}

	@Override
	public void printStackTrace(PrintWriter s) {
		Throwable cause = getCause();
		if(cause != null){
			cause.printStackTrace(s);
		}else{
			super.printStackTrace(s);
		}
	}

	@Override
	public String getLocalizedMessage() {
		if(super.getLocalizedMessage() == null || "".equals(super.getLocalizedMessage()) && getCause() != null){
			return getCause().getLocalizedMessage();
		}else{
			return super.getLocalizedMessage();
		}
	}

	@Override
	public String getMessage() {
		if(super.getMessage() == null || "".equals(super.getMessage()) && getCause() != null){
			return getCause().getMessage();
		}else{
			return super.getMessage();
		}
	}

	@Override
	public StackTraceElement[] getStackTrace() {
		if(super.getMessage() == null || "".equals(super.getMessage()) && getCause() != null){
			return getCause().getStackTrace();
		}else{
			return super.getStackTrace();
		}		
	}
}
