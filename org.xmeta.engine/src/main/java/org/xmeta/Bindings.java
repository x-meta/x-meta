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

import java.util.HashMap;
import java.util.Map;

/**
 * 动作上下文中栈点保存的就是Bindings，Bindings是一个Map，用于保存变量和一些函数调用相关的数据。
 * 
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public class Bindings extends HashMap<String, Object>{
	private static final long serialVersionUID = 1L;
	
	/** 调用动作的对象，一般是actionContext压入栈时的对象 */
	private Object caller = null;
	/**
	 * 调用者执行的方法名称。
	 */
	private String callerMethod = null;

	/** 事物动作上下文的动作上下文 */
	public Map<Thing, ActionContext> contexts = new HashMap<Thing, ActionContext>();
		
	public World world = World.getInstance();
	
	/** 是否关闭全局事物动作上下文，如果关闭子函数也都关闭 */
	public boolean disableGloableContext = false;
	
	/** 
	 * 是否是变量范围的标志，通常在函数调用（动作执行）、条件、循环等处设置为true，以便划分一个
	 * 变量范围，一般新的变量都保存到此变量范围中。
	 * 
	 * 为了实现类似其他语言如java的变量范围的设置。
	 */
	public boolean isVarScopeFlag = false;
	
	/**
	 * 构造一个空的StackMap。
	 *
	 */
	public Bindings(){
		super();	
	}

	public Object getCaller() {
		return caller;
	}

	public void setCaller(Object caller, String method) {
		this.caller = caller;
		this.callerMethod = method;
	}	
	
	public Map<Thing, ActionContext> getContexts(){
		return contexts;
	}
	
	public String getCallerMethod(){
		return callerMethod;
	}
}