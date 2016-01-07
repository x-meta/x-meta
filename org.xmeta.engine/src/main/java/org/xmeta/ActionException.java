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
import java.util.ArrayList;
import java.util.List;

/**
 * 动作的异常。
 * 
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public class ActionException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	/** XWorker自身的堆栈列表 */
	private static List<Bindings> bindingList = new ArrayList<Bindings>();
	
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
    
    public ActionException(String message, ActionContext actionContext){
        super(message);
        
        initBindings(actionContext);
    }

    public ActionException(String message, Throwable cause, ActionContext actionContext){
        super(message, cause);        
        
        initBindings(actionContext);
    }
    
    public ActionException(Throwable cause, ActionContext actionContext){
        super(cause);        
        
        initBindings(actionContext);
    }
    
    protected void initBindings(ActionContext actionContext){
    	for(int i=actionContext.getScopesSize() - 1; i>=0; i--){
    		Bindings bindings = actionContext.getScope(i);
    		bindingList.add(bindings);
    	}
    }
    
    /**
     * 如果构造ActionExeption时传入了ActionContext，那么可以获取它的所有Bindings此时返回的列表不为空，其他则为空。
     * 
     * @return 变量范围列表
     */
    public List<Bindings> getBindings(){
    	return bindingList;
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