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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.xmeta.util.UtilData;

/**
 * <p>变量上下文是执行动作时的变量空间，起到函数调用时栈的作用，是一个Stack和Map的综合体。</p>
 *  
 * <p>变量上下文通过栈的深度来表示全局变量和局部变量，深度越浅则越靠近全局变量，范围0是全局变量。</p>
 * 
 * <p>变量上下文是线程绑定的，除了创建时的变量时公用的，其他变量是线程各自的。</p>
 * 
 * <p>如果一个变量上下文的构造参数是另一个变量上下文，那么会先把用于构造的变量上下文的
 * 所有变量绑定压入栈中，因此两个变量上下文将使用相同的全局变量，但使用各自的最新的
 * 局部变量。由于原先的变量上下文的变量绑定被新的变量上下文所引用，因此当原先的动作
 * 上下文把一个变量绑定移除栈的时候，新的变量上下文还保持这个变量绑定的引用，因此需要注意一下内存的问题。</p>
 * 
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 * 
 */
public class ActionContext implements Map<String, Object>{
	/** 正常的运行状态 */
	public static final int RUNNING = 0;

	/** 返回值的状态，返回到动作被初始调用的地方 */
	public static final int RETURN = 1;

	/** 取消的状态，取消当前的动作执行 */
	public static final int CANCEL = 2;

	/** 打断状态，一般返回到上一个循环处 */
	public static final int BREAK = 3;

	/** 继续从循环处执行 */
	public static final int CONTINUE = 4;

	/** 抛出异常的状态，一般到异常处理处结束 */
	public static final int EXCEPTION = 5;

	public final static String RESULT_SUCCESS = "success";
	public final static String RESULT_FAILURE = "failure";
	public final static String RUNTYPE_DEFAULT = "DEFAULT";
	public final static String RUNTYPE_SUCCESS = "SUCCESS";
	public final static String RUNTYPE_RANDOM = "RANDOM";
	public final static String RUNTYPE_RANDOM_ONE = "RANDOM_ONE";
	public final static String RUNTYPE_RANDOM_RANDOM = "RANDOM_RANDOM";
	public final static String RUNTYPE_RANDOM_SUCCESS = "RANDOM_SUCCESS";
	public final static String RUNTYPE_RANDOM_RATE = "RANDOM_RATE";

	/** 方法调用的变量堆栈每个线程使用各自的，除了初始化时公共的全局变量 */
	private ThreadLocal<Stack<Bindings>> threadStacks = new ThreadLocal<Stack<Bindings>>();
	
	/** 线程里Action执行的状态 */
	private ThreadLocal<Integer> threadStatus = new ThreadLocal<Integer>();
	
	/** 动作抛出的对象 */
	private ThreadLocal<Object> threadThrowedObject = new ThreadLocal<Object>();
	
	/** 基础公用的堆栈，时初始化时的变量栈 */
	private Stack<Bindings> baseStacks = new Stack<Bindings>();

	/** 创建次上下文的线程，具有维护基础公用变量堆栈的能力 */
	private Thread createThread = null;
	
	/** 哑Scope数量，当上下文是通过另一个上下文创建时，Scope索引从本上下文开始，另一个上下文的要通过负索引获取
	    这样做的目的是getScope(0)只会取本上下文的Scope，而不是创建时的另一个上下文的Scope */
	private int dummyScopeCount = 0;
	
	/** 
	 * 动作堆栈，为了代码（如脚本）能获取本动作变量而设的堆栈，代码通过actionContext.getAction()获取当前代码对应的动作。
	 * 
	 * 动作的解释事物需要设置本堆栈一边被解析的代码能够正确获得对应的动作，如GroovyAction实际执行脚本时的变量栈的-2位置
	 * 才是代码对应的动作层，需要重新把-2层的动作压入栈中groovy脚本中才能正确获取对应的动作。 
	 */
	private ThreadLocal<Stack<Action>> actionStacks = new ThreadLocal<Stack<Action>>();
	
	/**
	 * 变量上下文的标签，辅助用途，用于表示这个变量上下文是干什么的。
	 */
	private String label = null;
	
	/** 变量绑定 */
	// Bindings bindings;
	/**
	 * 默认构造函数，并创建一个所有线程都共用的全局变量栈。
	 * 
	 */
	public ActionContext() {
		this((Bindings) null);
	}

	public ActionContext(boolean managedByPool){
		this((Bindings) null);
		
		managedByPool = true;
	}
	/**
	 * 通过给定的变量绑定创建变量上下文，其中传入的变量上下文的栈全部放入不可push和pop的栈中，
	 * 并压入一个新的所有线程都共用的全局变量栈，传入的变量上下文比新的全局变量更加全局。
	 * 
	 * @param actionContext 变量上下文
	 */
	public ActionContext(ActionContext actionContext) {
		createThread  = Thread.currentThread();
		
		// bindings = new Bindings();
		for (Bindings binding : actionContext.getScopes()) {
			baseStacks.push(binding);
			dummyScopeCount++;
		}

		Bindings bindings = this.push(null);
		bindings.put("actionContext", this);
		bindings.put("_g", bindings);
	}

	/**
	 * 创建一个变量上下文，使用指定的全局变量Bindings。
	 * 
	 * @param bindings 变量范围
	 */
	public ActionContext(Bindings bindings) {
		createThread  = Thread.currentThread();
		
		Bindings bds = this.push(bindings);
		bds.put("actionContext", this);
		bds.put("_g", bds);
	}
	
	private Stack<Action> getActionStack(){
		Stack<Action> actionStack = actionStacks.get();
		if(actionStack == null){
			actionStack = new Stack<Action>();
			actionStacks.set(actionStack);
		}
		
		return actionStack;		
	}
	
	public void pushAction(Action action){
		getActionStack().push(action);
	}
	
	public void popAction(){
		getActionStack().pop();
	}
	
	public Action getAction(){
		return getActionStack().peek();
	}

	private Stack<Bindings> getBindingStack(){
		Stack<Bindings> stack = threadStacks.get();
		if(stack == null){
			if(createThread == Thread.currentThread()){
				return baseStacks;
			}else{
				stack = new Stack<Bindings>();
				for(int i=0; i<dummyScopeCount + 1; i++){
					stack.push(baseStacks.get(i));
				}
				threadStacks.set(stack);
			}
		}
		
		return stack;
	}

	/**
	 * 返回动作执行的状态。
	 * 
	 * @return 动作的执行状态
	 */
	public int getStatus() {
		Integer status = threadStatus.get();
		if(status == null){
			return RUNNING;
		}else{
			return status;
		}
	}

	/**
	 * 返回是否禁止了全局变量上下文。
	 * 
	 * @return 是否禁止
	 */
	public boolean isDisableGlobalContext(){
		return this.peek().disableGloableContext;
	}
	
	/**
	 * 设置动作的执行状态。
	 * 
	 * @param status  执行状态
	 */
	public void setStatus(int status) {
		threadStatus.set(status);
	}

	public void _break(){
		setStatus(ActionContext.BREAK);
	}
	
	public void _ontinue(){
		setStatus(ActionContext.CONTINUE);
	}
	
	public void _return(Object obj){
		setStatus(ActionContext.RETURN);		
	}
		
	public void _exception(Object throwedObject){
		setStatus(ActionContext.EXCEPTION);
		setThrowedObject(throwedObject);
	}
	
	public Bindings push() {
		return push(null);
	}

	/**
	 * 压入一个变量绑定到堆栈中，当传入的变量绑定为null时自动创建一个新的。
	 * 
	 * @param bindings  变量绑定
	 * @return 压入的变量绑定
	 */
	public Bindings push(Bindings bindings) {
		if (bindings == null) {
			bindings = new Bindings();//ActionContextPool.obtainBindings();
		}

		Stack<Bindings> stacks = getBindingStack();
		/*
		if(stacks.size() > 0){
			Bindings top = stacks.peek();
			if(top != null && top.disableGloableContext){
				bindings.disableGloableContext = true;
			}
		}*/
				
		stacks.push(bindings);
				
		return bindings;
	}

	public Bindings pushPoolBindings(){
		return push(new Bindings());
	}
	
	/**
	 * 返回最顶层的变量绑定。
	 * 
	 * @return 最顶层的变量绑定。
	 */
	public Bindings peek() {
		return getBindingStack().peek();
	}

	/**
	 * 弹出最顶层的变量绑定。
	 * 
	 * @return 弹出的最顶层的变量绑定
	 */
	public Bindings pop() {
		return getBindingStack().pop();
	}

	/**
	 * 返回变量绑定的列表。
	 * 
	 * @return 变量绑定列表
	 */
	public List<Bindings> getScopes() {
		List<Bindings> bindings = new ArrayList<Bindings>();
		Stack<Bindings> bindingsStack = getBindingStack();
		for (int i = 0; i < bindingsStack.size(); i++) {
			bindings.add(bindingsStack.get(i));
		}
		return bindings;
	}

	/**
	 * 获取当前的局部变量范围，如果没有这是局部变量范围将返回null。
	 * 
	 * @return 变量范围
	 */
	public Bindings getScope(){
		Stack<Bindings> bindingsStack = getBindingStack();
		for (int i = bindingsStack.size() - 1; i >= 0; i--) {
			Bindings bindings = bindingsStack.get(i);
			if(bindings.isVarScopeFlag()){
				return bindings;
			}
		}
		
		//如果没有返回null
		return null;
		//return bindingsStack.get(dummyScopeCount);
	}
	
	/**
	 * 获取当前本地变量.
	 * 
	 * @return 变量范围
	 */
	public Bindings getLocalScope(){
		return getScope();
	}
	
	/**
	 * 获取全局变量。
	 * 
	 * @return 变量范围
	 */
	public Bindings getGlobalScope(){
		return getScope(0);
	}
	
	/**
	 * 获取全局变量的缩写。
	 * 
	 * @return 全局变量范围
	 */
	public Bindings g(){
		return  getScope(0);
	}
	
	/**
	 * 获取局部变量的缩写。
	 * 
	 * @return 本地变量范围
	 */
	public Bindings l(){
		return getScope();
	}
	
	/**
	 * 返回指定索引的局部变量集合，用于多个局部变量包含的情形，如Begin套Begin。
	 * 
	 * @param index 索引，从0开始，0是最近的那个局部变量范围
	 * @return 局部变量集合
	 */
	public Bindings l(int index){
		int c = 0;
		Stack<Bindings> bindingsStack = getBindingStack();
		for (int i = bindingsStack.size() - 1; i >= 0; i--) {
			Bindings bindings = bindingsStack.get(i);
			if(bindings.isVarScopeFlag()){
				if(c == index){
					return bindings;
				}else{
					c++;
				}
			}
		}
		
		//如果没有返回null
		return null;
	}
	
	
	/**
	 * 获取当前堆栈信息。
	 * 
	 * @return 堆栈信息
	 */
	public String getStackTrace(){
		Stack<Bindings> bindingsStack = getBindingStack();
		String content = "ActionContext stacktrace, thread=" + Thread.currentThread().getName() + ":";
		for (int i = bindingsStack.size() - 1; i >= 0; i--) {
			String stack = "";			
			Bindings bindings = bindingsStack.get(i);
			if(bindings.getCaller() != null){
				Object caller = bindings.getCaller();
				if(caller instanceof Thing){
					stack = stack + "caller: thing: " + ((Thing) caller).getMetadata().getPath();
				}else if(caller instanceof Action){
					stack = stack + "caller: action: " + ((Action) caller).getThing().getMetadata().getPath();
				}else{
					stack = stack + "caller: object: " + caller.getClass();
				}
			}else{
				//content = content + "caller: null";
			}
			
			String method = bindings.getCallerMethod();
			if(method != null && !"".equals(method)){
				stack = stack + ", callerMethod: " + method;
			}
			
			if(bindings.getContexts() != null){
				String values = "";
				for(Thing key : bindings.getContexts().keySet()){
					values = values + key.getMetadata().getPath() + ",";
				}
				
				if(!"".equals(values)){
					stack = stack + ", contexts: " + values;
				}
			}
			
			if(!"".equals(stack)){
				content = content + "\n    " + stack;
			}
		}
		return content;
	}
	
	/**
	 * 打印堆栈信息到控制台。
	 * 
	 */
	public void printStackTrace(){			
		System.out.println(getStackTrace());
	}
	
	/**
	 * 返回指定范围的变量绑定。
	 * 
	 * @param index
	 *            变量范围
	 * @return 变量绑定
	 */
	public Bindings getScope(int index) {
		return getBindingStack().get(index + dummyScopeCount);
	}

	/**
	 * 根据动作事物的路径来获得变量范围。
	 * 
	 * @param actionThingPath 动作路径
	 * @return 动作路径对应的变量范围
	 */
	public Bindings getScope(String actionThingPath) {
		if (actionThingPath == null) {
			return null;
		}
		
		if("Global".equals(actionThingPath)){
			return getScope(0);
		}else if("Local".equals(actionThingPath)){
			Stack<Bindings> bindingsStack = getBindingStack();
			return bindingsStack.peek();
		}

		try{
			int stack = Integer.parseInt(actionThingPath);
			return getScope(stack);
		}catch(Exception e){
			Stack<Bindings> bindingsStack = getBindingStack();
			for (int i = bindingsStack.size() - 1; i >= 0; i--) {
				while (i >= bindingsStack.size())
					i--;
	
				Bindings bindings = bindingsStack.get(i);
				if (bindings.getCaller() instanceof Action) {
					Action action = (Action) bindings.getCaller();
					if (actionThingPath.equals(action.getThing().getMetadata().getPath()) ||
							actionThingPath.equals(action.getThing().getMetadata().getName())) {
						return bindings;
					}
				}else if(bindings.getCaller() instanceof Thing){
					Thing thing = (Thing) bindings.getCaller();
					if(thing != null && actionThingPath.equals(thing.getMetadata().getPath()) ||
							actionThingPath.equals(thing.getMetadata().getName())){
						return bindings;
					}
				}
			}
		}

		return null;
	}

	/**
	 * 返回变脸范围的迭代大小。
	 * 
	 * @return 变量范围的大小
	 */
	public int getScopesSize() {
		return getBindingStack().size() - dummyScopeCount;
	}
	
	/**
	 * 返回所有包括线程之前创建的变量范围大小。
	 * 
	 * @return 变量栈的大小
	 */
	public int getScopesSizeAll(){
		return getBindingStack().size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#clear()
	 */
	public void clear() {
		getBindingStack().peek().clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	public boolean containsKey(Object key) {
		boolean ck = false;
		Stack<Bindings> bindingsStack = getBindingStack();
		for (int i = bindingsStack.size() - 1; i >= 0; i--) {
			while (i >= bindingsStack.size())
				i--;

			if (bindingsStack.get(i).containsKey(key)) {
				ck = true;
				break;
			}
		}
		return ck;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	public boolean containsValue(Object value) {
		Stack<Bindings> bindingsStack = getBindingStack();
		for (int i = bindingsStack.size() - 1; i >= 0; i--) {
			while (i >= bindingsStack.size())
				i--;

			if (bindingsStack.get(i).containsValue(value)) {
				return true;
			}
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#entrySet()
	 */
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		Map<String, Object> map = new HashMap<String, Object>();

		Stack<Bindings> bindingsStack = getBindingStack();
		for (int i = 0; i < bindingsStack.size(); i++) {
			map.putAll(bindingsStack.get(i));
		}

		return map.entrySet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public Object get(Object key) {
		if(key == null || "".equals(key)){
			//所有空的key返回null
			return null;
		}
		
		Object value = null;
		Stack<Bindings> bindingsStack = getBindingStack();
		for (int i = bindingsStack.size() - 1; i >= 0; i--) {
			while (i >= bindingsStack.size())
				i--;

			Map<String, Object> map = bindingsStack.get(i);
			value = map.get(key);

			if (value != null) {
				break;
			} else if (map.containsKey(key)) {
				break;
			}
		}

		return value;
	}

	public Object get(Object key, String scopeThingPath) {
		Stack<Bindings> bindingsStack = getBindingStack();
		for (int i = bindingsStack.size() - 1; i >= 0; i--) {
			while (i >= bindingsStack.size())
				i--;

			Bindings bindings = bindingsStack.get(i);
			Thing currentThing = null;
			if (bindings.getCaller() instanceof Action) {
				currentThing = ((Action) bindings.getCaller()).getThing();
			}

			if (currentThing != null
					&& scopeThingPath.equals(currentThing.getMetadata()
							.getPath())) {
				return bindings.get(key);
			}
		}

		return null;
	}

	/**
	 * 返回动作调用列表，最开始的是堆栈的底部，最后的是堆栈的顶部。
	 * 
	 * @return 动作调用列表
	 */
	public List<Action> getActions() {
		List<Action> actions = new ArrayList<Action>();

		for (Bindings bindings : getBindingStack()) {
			if (bindings.getCaller() instanceof Action) {
				actions.add((Action) bindings.getCaller());
			}
		}

		return actions;
	}

	/**
	 * 返回调用动作的事物列表，最开始的是堆栈的底部，最后的堆栈的顶部的事物。
	 * 
	 * @return 调用动作的事物列表
	 */
	public List<Thing> getThings() {
		List<Thing> things = new ArrayList<Thing>();

		for (Bindings bindings : getBindingStack()) {
			if (bindings.getCaller() instanceof Thing) {
				things.add((Thing) bindings.getCaller());
			}
		}

		return things;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#isEmpty()
	 */
	public boolean isEmpty() {
		Stack<Bindings> bindingsStack = getBindingStack();
		for (int i = bindingsStack.size() - 1; i >= 0; i--) {
			if (!bindingsStack.get(i).isEmpty()) {
				return false;
			}
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#keySet()
	 */
	public Set<String> keySet() {
		Set<String> aset = new HashSet<String>();
		for (int i = getBindingStack().size() - 1; i >= 0; i--) {
			aset.addAll(getBindingStack().get(i).keySet());
		}
		return aset;
	}

	/**
	 * 设置变量上下文中一个变量的值，设置值的方法是从栈的顶端的Bindings向栈底端的Bindings找，如果Bindings包含
	 * 相应的键那么调用Bindings的put方法，如果所有的Bingings都不包含给定的键值，没有那么设置到顶端的Bindings中。
	 * 
	 * 要确保设置全局变量的本地变量，可以通过getScope取得相应的Bindings，然后在赋值。
	 * 
	 * @param key 变量key
	 * @param value 变量值
	 * @return 如果成功返回变量
	 */
	public Object put(String key, Object value) {
		Stack<Bindings> bindingsStack = getBindingStack();
		for (int i = bindingsStack.size() - 1; i >= dummyScopeCount; i--) {
			Bindings bindings = bindingsStack.get(i);
			if (bindings.containsKey(key)) {
				return bindings.put(key, value);
			}
		}
		
		Object v = getScope(0).put(key, value);
		return v;
	}

	/**
	 * 根据指定的事物从堆栈中寻找变量范围，并设置变量范围的值。
	 * 
	 * @param key 变量的key
	 * @param value 变量的值
	 * @param scopeThingPath 变量范围路径	
	 * @return 变量值
	 */
	public Object put(String key, Object value, String scopeThingPath) {
		Stack<Bindings> bindingsStack = getBindingStack();
		for (int i = bindingsStack.size() - 1; i >= dummyScopeCount; i--) {
			Bindings bindings = bindingsStack.get(i);
			Thing currentThing = null;
			if (bindings.getCaller() instanceof Action) {
				currentThing = ((Action) bindings.getCaller()).getThing();
			}

			if (currentThing != null
					&& scopeThingPath.equals(currentThing.getMetadata()
							.getPath())) {
				return bindings.put(key, value);
			}
		}

		return null;
	}

	/**
	 * 把某个值放到指定的变量范围中。
	 * 
	 * @param key 变量key
	 * @param scopeThingPath 路径
	 * @return 变量值
	 */
	public Object putTo(String key, String scopeThingPath) {
		Object value = this.get(key);

		return put(key, value, scopeThingPath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	public void putAll(Map<? extends String, ? extends Object> m) {
		for(String key : m.keySet()){
			put(key, m.get(key));
		}		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	public Object remove(Object key) {
		return getBindingStack().peek().remove(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#size()
	 */
	public int size() {
		return this.entrySet().size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Map#values()
	 */
	public Collection<Object> values() {
		List<Object> list = new ArrayList<Object>();
		for (Object key : this.keySet()) {
			list.add(get(key));
		}

		return list;
	}

	public void setThrowedObject(Object obj) {
		threadThrowedObject.set(obj);
	}

	public Object getThrowedObject() {
		return threadThrowedObject.get();
	}

	@Override
	public String toString() {
		return "ActionContext [hashCode=" + this.hashCode() + ",callers=" +  this.getThings() + "]";
	}

	public String getString(String key){
		return UtilData.getString(get(key), null);
	}
	
	public byte getByte(String key){
		return UtilData.getByte(get(key), (byte) 0);
	}
	
	public short getShort(String key){
		return UtilData.getShort(get(key), (short) 0);
	}
	
	public int getInt(String key){
		return UtilData.getInt(get(key), 0);
	}
	
	public long getLong(String key){
		return UtilData.getLong(get(key), 0);
	}
	
	public boolean getBoolean(String key){
		return UtilData.getBoolean(get(key), false);
	}
	
	public byte[] getBytes(String key){
		return UtilData.getBytes(get(key), null);
	}
	
	public Date getDate(String key){
		return UtilData.getDate(get(key), null);
	}
	
	public double getDouble(String key){
		return UtilData.getDouble(get(key), 0);
	}
	
	public float getFloat(String key){
		return UtilData.getFloat(get(key), 0);
	}
	
	
	public BigDecimal getBigDecimal(String key){
		return UtilData.getBigDecimal(get(key), null);
	}
	
	public BigInteger getBigInteger(String key){
		return UtilData.getBigInteger(get(key), null);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getObject(String key){
		Object obj = get(key);
		return (T) obj;		
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}