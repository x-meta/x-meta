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
import java.util.Iterator;
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
	private Map<Thing, ActionContext> contexts = null;//new HashMap<Thing, ActionContext>();
	
	/** Bindings可以设置动作的上下文，2016-10-26 */
	private Thing contextThing = null;
		
	public World world = World.getInstance();
	
	/** 是否关闭全局事物动作监听器，如果关闭子函数也都关闭 */
	public boolean disableGloableContext = false;
	
	/** 
	 * 是否是变量范围的标志，通常在函数调用（动作执行）、条件、循环等处设置为true，以便划分一个
	 * 变量范围，一般新的变量都保存到此变量范围中。
	 * 
	 * 为了实现类似其他语言如java的变量范围的设置。
	 */
	private boolean isVarScopeFlag = false;
	
	/**
	 * 是否是参数Bindings。
	 */
	private boolean parameterScope = false;
	
	/**
	 * 构造一个空的StackMap。
	 *
	 */
	public Bindings(){
		super();	
	}

	public boolean isVarScopeFlag(){
		return isVarScopeFlag;
	}
	
	public void setVarScopeFlag(){
		isVarScopeFlag = true;
	}
	
	public Object getCaller() {
		return caller;
	}

	public void setCaller(Object caller, String method) {
		this.caller = caller;
		this.callerMethod = method;
	}	
	
	public Map<Thing, ActionContext> getContexts(){
		if(contexts == null){
			contexts = new HashMap<Thing, ActionContext>();
		}
		return contexts;
	}
	
	public String getCallerMethod(){
		return callerMethod;
	}

	@Override
	public int hashCode() {
		int h = 0;
        Iterator<Entry<String,Object>> i = entrySet().iterator();
        while (i.hasNext()){
        	Entry<String,Object> obj = i.next();
        	if(obj.getValue() instanceof Bindings || obj.getValue() instanceof ActionContext){
        		//因为可能会引起递归，所以屏蔽这些类型了
        		continue;
        	}
        	
        	if(obj.getValue() != this){
        		h += obj.hashCode();
        	}
        }
        return h;
	}
	
	/**
	 * 设置上下文事物，上下文事物会在执行动作时加入到动作上下文中，动作上下文会在动作执行前和执行后执行相关动作。
	 * 
	 * @param contextThing
	 */
	public void setContextThing(Thing contextThing){
		this.contextThing = contextThing;
	}
	
	public Thing getContextThing(){
		return this.contextThing;
	}
		
	public String toString(){
		return super.toString();
		/*
		String str = "";
		if(caller != null){
			str = str + caller.getClass().getSimpleName() + ": " + (callerMethod != null ? callerMethod : "") + ", ";
		}
		
		str = str + "变量个数：" + this.size();
		return str;*/
	}

	public boolean isParameterScope() {
		return parameterScope;
	}

	public void setParameterScope(boolean parameterScope) {
		this.parameterScope = parameterScope;
	}
	
	
}