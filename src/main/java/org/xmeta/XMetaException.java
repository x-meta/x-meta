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

import java.io.PrintStream;
import java.io.PrintWriter;


public class XMetaException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	/** ActionContext中的StackTrace，如果存在 */
	String stackTrace = null;
	
	public XMetaException(){
        super();
    }

    public XMetaException(String message){
        super(message);
    }

    public XMetaException(String message, Throwable cause){
        super(message, cause);        
    }
    
    public XMetaException(Throwable cause){
        super(cause);        
    }
    
    public XMetaException(ActionContext actionContext){
        super();
        
        initActionContextStackTrace(actionContext);
    }

    public XMetaException(String message, ActionContext actionContext){
        super(message);
        
        initActionContextStackTrace(actionContext);
    }

    public XMetaException(String message, Throwable cause, ActionContext actionContext){
        super(message, cause);        
        
        initActionContextStackTrace(actionContext);
    }
    
    public XMetaException(Throwable cause, ActionContext actionContext){
        super(cause);        
        
        initActionContextStackTrace(actionContext);
    }
    
    private void initActionContextStackTrace(ActionContext actionContext){
    	stackTrace = actionContext.getStackTrace();
    }

	@Override
	public void printStackTrace() {
		super.printStackTrace();
		if(stackTrace != null){
			System.out.println(stackTrace);
		}
		
	}

	@Override
	public void printStackTrace(PrintStream s) {
		super.printStackTrace(s);
		if(stackTrace != null){
			s.println(stackTrace);
		}

	}

	@Override
	public void printStackTrace(PrintWriter s) {	
		super.printStackTrace(s);
		if(stackTrace != null){
			s.println(stackTrace);
		}
	}
}