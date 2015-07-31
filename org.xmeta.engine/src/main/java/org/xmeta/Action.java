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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmeta.cache.ThingEntry;
import org.xmeta.thingManagers.ClassThingManager;
import org.xmeta.thingManagers.FileThingManager;
import org.xmeta.util.JavaCompiler15;
import org.xmeta.util.JavaCompiler16;
import org.xmeta.util.Semaphore;
import org.xmeta.util.ThingClassLoader;
import org.xmeta.util.UtilAction;

/**
 * 动作是可以运行的程序，是事物的另一种形态。<p/>
 * 
 * 动作的原生语言是Java，但可以通过动作的动作方式实现其他语言支持，可以在动作的结构中编写一个run方法，
 * 在run方法里解释执行动作中定义的代码。<p/>
 * 
 * 有些语言可能会编译成类或者动作发生了变化后会重新生成，此时为了避免事物发生变化了（如版本回退）而类不会重新编译，
 * 需要在编译类成功后调用动作的updateCompileTime()方法保存编译时间。
 *  
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public class Action extends Semaphore{
	/** 日志 */
	private static Logger log = LoggerFactory.getLogger(Action.class);	
	
	private static World world = World.getInstance();
	
	/** 记录类编译时间的文件类集合 */
	private static Map<String, SoftReference<ClassCompileTimeFile>> classTimeFiles = new HashMap<String, SoftReference<ClassCompileTimeFile>>();
	
	/** Java关键字列表，不能作为类和包的名称 */
	public final static String[] javaKeyWords = new String[]{
		"abstract", "boolean", "break", "byte", "case",
		"catch", "char", "class", "continue", "default",
		"do", "double", "else", "extends", "false",
		"final", "finally", "float", "for", "if",
		"implements", "import", "instanceof", "int", "interface",
		"long", "native", "new", "null", "package",
		"private", "protected", "public", "return", "short",
		"static", "super", "switch", "synchronized", "this",
		"throw", "throws", "transient", "true", "try",
		"void", "volatile", "while", "#"	
	};
	
	//------------公共动作的属性 -----------------
	/** 定义动作的事物 */
	public ThingEntry thingEntry;
	
	/** 是否是Java程序 */
	private boolean isJava = false;
	
	/** 是否抛出异常当对动作执行发生异常时 */
	private boolean throwException; 
	
	/** 是否要同步执行，如果是那么调用这个动作事物将会被同步执行 */
	private boolean isSynchronized;
	
	/** 最后一次修改时间，用来判断是否事物已经更新 */
	public long lastModified;
	
	/** 是否使用其他事物定义的动作 */
	private boolean useOtherAction;
	
	/** 解释方式 */
	private boolean isSelfInterpretationType;
	
	/** 其他事物的路径 */
	private String otherActionPath;
	
	/** 动作事物的上下文列表 */
	private List<ThingEntry> contexts;
	
	/** Java类装载器，动态装载类 */
	public ClassLoader classLoader;
	
	/** 编译Java源文件时要用到的类库路径 */
	//public String classPath;
	
	/** 编译后的类名 */
	public String className;
	
	/** 编译后的类存放的目录 */
	public String classFileName;
	
	/** 类的包名 */
	public String packageName;
	
	/** 保存Java代码的文件名 */
	public String fileName;	
	
	/** 代码 */
	public String code;
	
	/** 要运行的方法名 */
	private String methodName;
	
	/** 是否使用系统外部的Java，即使用其他Java组件 */
	private boolean useOuterJava;	
	
	/** 外部的Java类名 */
	private String outerClassName;
	
	/** 是否在执行时替换属性模板 */
	private boolean attributeTemplate;	
	
	/** 编译后并且装载了的类 */
	public Class<?> actionClass = null;
	
	/** 动作的定义是否已经改变 */
	public boolean changed = false;
	
	/** 禁止全局上下文 */
	private boolean disableGlobalContext = false;
	
	/** 外部动作，如果有引用*/
	public Action outerAction = null;
	
	/** 如果是Java动作，则有方法 */
	public Method method = null;
	
	/** 用户数据，在代码或脚本理可以设置和Action绑定的数据 */
	Map<String, Object> userData = new HashMap<String, Object>();
	
	/** 动作对应的日志 */
	Logger logger;
	
	/** 子动作列表 */
	List<ActionResult> results;
	
	/** 输入参数的定义 事物 */
	Thing params = null;
	
	/** 是否保存返回值 */
	boolean saveReturn;
	
	/** 返回值变量名 */
	String returnVarName;
	
	/** 是否要创建本地变量范围  */
	boolean isCreateLocalVarScope;
		
	//----------构造函数和其他方法------------
	/**
	 * 构造函数，传入定义动作的事物。
	 * 
	 * @param thing 定义动作的事物
	 */
	public Action(Thing thing){
		this.thingEntry = new ThingEntry(thing);
		
		try{
			init();
		}catch(Exception e){
			throw new ActionException("init action error, action=" + thing, e);
		}
	}
		
	/**
	 * 检查动作是否已经变更。
	 */
	public void checkChanged(){
		if(lastModified != thingEntry.getThing().getMetadata().getLastModified()){
			try{
				init();
			}catch(Exception e){
				throw new ActionException("", e);
			}
			
			changed = true;
		}
	}
	
	/**
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	private void init() throws ClassNotFoundException, IOException, SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException{
		Thing thing = thingEntry.getThing();
		
		params = thing.getThing("ins@0");
		
		//初始化共有的变量		
		if(thing.getAttribute("throwException") == null){
			throwException = true;
		}else{
			throwException = thing.getBoolean("throwException");
		}
		isSynchronized = thing.getBoolean("isSynchronized");
		lastModified = thing.getMetadata().getLastModified();

		contexts = new ArrayList<ThingEntry>();
		Thing contextsThing = thing.getThing("contexts@0");
		if(contextsThing != null){
			List<Thing> contextList = contextsThing.getChilds();
			for(Thing contextThing : contextList){
				contexts.add(new ThingEntry(contextThing));
			}
		}
		
		//属性模板
		attributeTemplate = thing.getBoolean("attributeTemplate");
		
		useOtherAction = thing.getBoolean("useOtherAction");
		otherActionPath = thing.getString("otherActionPath");		
		disableGlobalContext = thing.getBoolean("disableGlobalContext");
		
		isSelfInterpretationType = "Self".equals(thing.getString("interpretationType"));
		
		//返回值
		returnVarName = thing.getString("returnVarName");
		saveReturn = thing.getBoolean("saveReturn");
		
		//设置代码文件名、类名和编译后的类路径等
		Thing parent = thing.getParent();			
		Thing rootParent = thing.getRoot();
		if(parent == null){
			parent = thing;
		}
		className = rootParent.getMetadata().getPath();
		if(rootParent != thing){
			className = className + ".p" + thing.getMetadata().getPath().hashCode();
			className = className.replace('-', '_');
			
			String cName = thing.getString("className");			    
			if(cName == null || "".equals(cName)){
			    className = className + "." + thing.getMetadata().getName();
			}else{
				className = className + "." + cName;
			}
		}
				
		className = getClassName(className);
		
		int dotIndex = className.lastIndexOf(".");
		if(dotIndex != -1){
			packageName = className.substring(0, dotIndex);
		}
		
		fileName = className.replace('.', '/');
		//fileName += ".java";
					
		fileName = World.getInstance().getPath() + "/actionSources/" + fileName;
		classFileName = World.getInstance().getPath() + "/actionClasses/" + className.replace('.', '/') + ".class";
		
		//设置代码和方法名
		code = thing.getString("code");
		if(code == null){
			code = "";
		}
		
		methodName = thing.getString("methodName");
		
		//设置是否使用外部的Java
		useOuterJava  = thing.getBoolean("useOuterJava");
		outerClassName = thing.getString("outerClassName");
					
		//需要重新编译
		actionClass = null;
		if("JavaAction".equals(thing.getThingName())){
			isJava = true;
		}else{
			isJava = false;
		}	
		
		//初始化类装载器和编译时的类库路径
		ThingClassLoader pclssLoader = thing.getMetadata().getThingManager().getClassLoader();
		
		String compleClassPath = pclssLoader.getCompileClassPath();
		classLoader = new ActionClassLoader(pclssLoader);
		
		results = new ArrayList<ActionResult>();
		for(Thing child : thing.getAllChilds("Result")){
			ActionResult result = new ActionResult(child);		
			results.add(result);
		}
		
		isCreateLocalVarScope = thing.getBoolean("createLocalVarScope");
		
		long classCompileTime = 0;
		
		if(thing.getMetadata().getThingManager() instanceof ClassThingManager){
			//如果是从类路径装载的事物，尽量直接读取class，因为可能是打包运行的而不是开发环境的
			try{
				classCompileTime = getClassCompileTime(classFileName);
				if(classCompileTime != 0 && classCompileTime == lastModified){
					//不需要重新编译才直接读取，2013-03-12
					actionClass = classLoader.loadClass(this.className);
					if(actionClass != null){
						classCompileTime = lastModified;
					}
				}
			}catch(Throwable t){	
				classCompileTime = getClassCompileTime(classFileName);
			}
		}else{
			classCompileTime = getClassCompileTime(classFileName);
		}
		
		if(isJava){
			//查看是否需要编译获得重新装载
			if(actionClass == null){
				changed = false;
				
				//需要编译或重新装载
				if(useOuterJava){
					actionClass = classLoader.loadClass(outerClassName); 						
				}else{					
					boolean recompile = false;
					File classFile = new File(classFileName);
					if(!classFile.exists()){
						recompile = true;
					}
					
					if(lastModified != classCompileTime){
						recompile = true;							
					}
					
					if(recompile){
						if(thing.getBoolean("useInnerJava")){
							ThingManager thingManager = thing.getMetadata().getThingManager();
							if(thingManager instanceof FileThingManager){
								FileThingManager fileThingManager = (FileThingManager) thingManager;
								String sourcePath = fileThingManager.getFilePath();
								File codeFile = new File(sourcePath, outerClassName.replace('.', '/') + ".java");
								boolean use16 = true;
								boolean compiled = false;
								try{							
									Class.forName("javax.tools.JavaCompiler");
								}catch(Exception e){
									use16 = false;
								}
								
								if(use16){
									compiled = JavaCompiler16.compile(compleClassPath, sourcePath, codeFile);
								}							
								if(!compiled){
									JavaCompiler15.compile(compleClassPath, sourcePath, codeFile.getAbsolutePath());								
								}	
							}else{
								throw new ActionException("useInnerJava is only fit for FileThingManager, actionThing=" + thing.getMetadata().getPath());
							}
						}else{
							File codeFile = new File(fileName + ".java");
							if(!codeFile.exists()){
								codeFile.getParentFile().mkdirs();
							}
							
							FileOutputStream fout = new FileOutputStream(codeFile);
							try{
								//文件头增加一个事物路径的标识
								fout.write(("/*path:" + thing.getMetadata().getPath() + "*/\n").getBytes());
								fout.write(("package " + packageName + ";\n\n").getBytes());
								fout.write(code.getBytes());								
							}finally{
								fout.close();
							}
							
							File classDir = new File(world.getPath() + "/actionClasses");
							if(!classDir.exists()){
								classDir.mkdirs();
							}
								
							boolean use16 = true;
							boolean compiled = false;
							try{							
								Class.forName("javax.tools.JavaCompiler");
							}catch(Exception e){
								use16 = false;
							}
							
							if(use16){
								compiled = JavaCompiler16.compile(compleClassPath, null, codeFile);
							}							
							if(!compiled){
								JavaCompiler15.compile(compleClassPath, null, fileName);								
							}	
						}
						
						updateCompileTime();
					}
					
					if(thing.getBoolean("useInnerJava")){
						actionClass = classLoader.loadClass(outerClassName); 	
					}else{
						actionClass = classLoader.loadClass(className);
					}
				}
				java.lang.Compiler.compileClass(actionClass);
				try{	
					if(methodName != null && !"".equals(methodName)){
						method = actionClass.getDeclaredMethod(methodName, new Class[]{ActionContext.class});
					}
				}catch(Exception e){		
					throw new ActionException("", e);
				}
			}else if(method == null){
				try{	
					if(methodName != null && !"".equals(methodName)){
						method = actionClass.getDeclaredMethod(methodName, new Class[]{ActionContext.class});
					}
				}catch(Exception e){		
					throw new ActionException("", e);
				}
			}
		}else if(useOtherAction){
			outerAction = world.getAction(otherActionPath);
		}else{
			//由动作使用的动作，判断是否存在类，如果类的日期小于当前事物的日期，那么判定已更新
			if(lastModified != classCompileTime){
				changed = true;
			}			
			
			//非Java调用才初始化日志
			logger = LoggerFactory.getLogger(this.className);
		}
	}
	
	/**
	 * 返回动作的类，如果存在。有些动作可能会返回null。
	 * 
	 * @param actionContext
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Class getActionClass(ActionContext actionContext){
		Thing thing = thingEntry.getThing();
		if(lastModified != thing.getMetadata().getLastModified()){
			try{
				init();
			}catch(Exception e){
				throw new ActionException("", e);
			}
			
			changed = true;
		}
		
		if(isJava){
			return actionClass;
		}else{
			if(actionContext == null){
				actionContext = new ActionContext();
			}
			try{
				actionContext.pushPoolBindings().put("actionThing", thing);
				return (Class) thing.doAction("getActionClass", actionContext);
			}finally{
				actionContext.pop();
			}
		}
	}
	
	public void updateCompileTime(){
		updateClassCompileTime(classFileName, lastModified);
	}
	
	public Method getMethod(){
		return method;
	}
	
	public Object run(){
		return runArrayParams(new ActionContext(), (Object[]) null, null, false);
	}
	
	public Object run(ActionContext context){
		return runMapParams(context, null, null, false);
	}
	
	public Object exec(Object... params){
		return runArrayParams(null, params, null, false);
	}
	
	public Object exec(ActionContext context, Object... params){
		return runArrayParams(context, params, null, false);
	}
	
	public Object run(ActionContext context, Map<String, Object> parameters){
		return runMapParams( context, parameters, null, false);
	}
	
	public Object run(ActionContext context, Map<String, Object> parameters, boolean isSubAction){
		return runMapParams(context, parameters, null, isSubAction);
	}
	
	public Object run(ActionContext context, Map<String, Object> parameters, Object caller, boolean isSubAction) {
		return runMapParams(context, parameters, caller, isSubAction);
	}
	
	public Object runArrayParams(ActionContext context, Object[] params_, Object caller,  boolean isSubAction){
		if(context == null){
			context = new ActionContext();
		}
		
		Bindings bindings = new Bindings();
		if(params != null){
			List<Thing> ps = params.getChilds();
			for(int i=0; i<ps.size(); i++){
				Thing p = ps.get(i);
				if(params_ != null && params_.length > i){
					bindings.put(p.getString("name"), params_[i]);
				}else{
					bindings.put(p.getString("name"), null);
				}
			}			
		}
		
		return this.dorun(context, bindings, bindings, caller, isSubAction);
	}
	
	public Object runMapParams(ActionContext context, Map<String, Object> parameters, Object caller, boolean isSubAction) {
		if(context == null){
			context = new ActionContext();
		}
		
		Bindings bindings = new Bindings();
		if(parameters != null){
			bindings.putAll(parameters);
		}
		
		return this.dorun(context, bindings, parameters, caller, isSubAction);
	}
	
	/**
	 * 执行动作。
	 * 
	 * @param methodName 方法名称
	 * @param context 动作的上下文
	 * @param isSubAction 是否是子动作
	 * 
	 * @return 执行的结果
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	private Object dorun(ActionContext context, Bindings bindings, Map<String, Object> parameters, Object caller, boolean isSubAction) {
		//long start = System.nanoTime();
		//是否禁止全局上下文具有继承的性质
		if(context.peek().disableGloableContext ||  this.disableGlobalContext){
			bindings.disableGloableContext = this.disableGlobalContext;
		}
		
		//动作记录
		if(!bindings.disableGloableContext && world.isHaveActionListener()){
			ActionListener listener = world.getActionListener();
			try{
				listener.actionExecuted(this, caller, context, parameters, -1, true);
			}catch(Throwable t){
				log.error("ActionRecorder error", t);
			}
		}
		
		//判断事物是否已经是否变更，如果变更了那么重新初始化
		Thing thing = thingEntry.getThing();
		if(lastModified != thing.getMetadata().getLastModified()){
			try{
				init();
			}catch(Exception e){
				throw new ActionException("", e);
			}
			
			changed = true;
		}
		
		//如果需要同步
		boolean thisIsSynchronized = false;
		if(isSynchronized){
			try {
				this.use();
				thisIsSynchronized = true;
			} catch (InterruptedException e) {
				throw new ActionException("try to synchronize action : " + thing.getMetadata().getPath(), e);
			}
		}
		
		//外面的调用已经压入
		context.push(bindings);		
		if(!isSubAction || isCreateLocalVarScope){
			bindings.isVarScopeFlag = true;//可以判定是函数调用的入口，因此设置一个局部变量范围标志
		}
		context.pushAction(this);
				
		//总是输入一些常量，以后要取消
		bindings.put("world", world);
		if(logger != null){
			bindings.put("log", logger);
		}
				
		Object result = null;
		List<ThingEntry> allContexts = new ArrayList<ThingEntry>();
		if(!disableGlobalContext){
			allContexts.addAll(world.globalContexts);
		}
		if(contexts.size() > 0){
			allContexts.addAll(contexts);
		}
		
		//初始化脚本内的上下文
		bindings.setCaller(this, null);
		bindings.world = world;	
		
		try{
			//首先是全局上下文		
			if(allContexts.size() > 0){
				for(ThingEntry thingContext : allContexts){
					initContext(thingContext.getThing(), context);
				}
			}
			
			if(useOtherAction){
				//如果一个动作是引用其他动作的，那么执行被引用的动作 
				Bindings callerBindings = context.getScope(context.getScopesSize() - 2);
				try{
					context.push(callerBindings);
					result = outerAction.run(context, parameters, isSubAction);
				}finally{
					context.pop();
				}
			}else if(isJava){
				//如果动作时原生动作，即Java动作，那么通过反射机制调用 
				if(actionClass != null){
					if(method != null){
						result = method.invoke(actionClass, new Object[]{context});
					}else{
						logger.info("Java action method is null, " + getThing().getMetadata().getPath());
					}
				}
			}else{
				//如果不是Java，那么调用当前动作事物的run行为，run行为有可能是自定义的也有可能是其描述者定义的 
			    //此方法很重要，用这个方法可以实现其他语言的支持，比如Groovy、JavaScript等脚本语言 
				if(isSelfInterpretationType){
					//self类型的动作，当一个动作作为子动作时，而变量self还需要是自己，那么需要定义成此类型 
					if(attributeTemplate){
						//属性模板，动作的属性可以设置成freemarker模板，在执行时用模板生成真正的属性值 
						Thing fthing = (Thing) thing.run("processAttributeTemplate", context, (Map<String, Object>) null, isSubAction, true);
						if(fthing != null){
							fthing.run("run", context, (Map<String, Object>) null, isSubAction, true);
						}
					}else{
						result = thing.run("run", context, (Map<String, Object>) null, isSubAction, true);
					}
				}else{
					//获取事物自己的行为run，然后执行 
					Thing actionThing = thing.getActionThing("run");
					if(actionThing != null){
						Action ac = actionThing.getAction();
						result = ac.run(context, null, caller, isSubAction);
					}
				}
				//result = thing.doAction("run", context, parameters, isSubAction);
			}
			
			//返回值的变量保存
			if(saveReturn && returnVarName != null  && !"".equals(returnVarName)){
				Bindings returnVarBindings = UtilAction.getVarScope(thingEntry.getThing(), context);
				if(returnVarBindings != null){
					returnVarBindings.put(returnVarName, result);
				}
			}
			
			/*
			 * 子流程取消，2012-12-12，使用
			//执行下级流程
			bindings.put("_return", result);					
			int status = context.getStatus();
			if(results != null && results.size() != 0 && ActionContext.RETURN != status){				
				for(ActionResult scObj : results){	
					boolean execute = false;
					try{
						execute = (Boolean) Ognl.getValue(scObj.condition, context);
					}catch(Exception e){
						log.warn("", e);
					}
					if(execute){
						result =  scObj.run(context);		
						
						int sint = context.getStatus();
						if(sint == ActionContext.RETURN || sint == ActionContext.BREAK || sint == ActionContext.EXCEPTION ||
								sint == ActionContext.CONTINUE || sint == ActionContext.CANCEL){
							break;
						}
					}					
				}					
			} */				
			
			if(context.getStatus() == ActionContext.EXCEPTION && isSubAction == false){
				//动作抛除异常
				if(context.getThrowedObject() instanceof Throwable){
					throw (Throwable) context.getThrowedObject();
				}else{
					Object throwedObject = context.getThrowedObject();
					if(throwedObject != null){
						throw new ActionException(throwedObject.toString());
					}else{
						throw new ActionException("action throw null");
					}					
				}
			}
			
			//执行动作的上下文的成功方法
			String contxtMethod = "success";
			if("failure".equals(result)){
				contxtMethod = "failure";
			}
			Throwable exception = doContextMethod(allContexts, context, contxtMethod, null);
			if(exception == null){				
				return result;			
			}else{
				if(!throwException){
					return "failure";
				}else{
					if(exception instanceof RuntimeException){
						throw (RuntimeException) exception;
					}else{
						throw new ActionException("", exception);
					}
				}
			}
		}catch(Throwable e){			
			Throwable exception = doContextMethod(allContexts, context, "exception", e);
			if(exception == null){
				return "success";
			}else{
				//exception.printStackTrace();
				if(!throwException){
					log.warn("action exception is hidded : " + thing.getMetadata().getPath(), e);
					return "failure";
				}else{
					if(exception instanceof RuntimeException){
						throw (RuntimeException) exception;
					}else{
						throw new ActionException(exception);
					}
				}
			}			
		}finally{
			context.pop();
			context.popAction();
			
			if(!isSubAction){
				context.setStatus(ActionContext.RUNNING);
			}
			
			//如果是同步的，那么通知其他线程这里已经结束
			if(thisIsSynchronized){
				this.finished();
			}
		}
	}
	
	public static Throwable doContextMethod(List<ThingEntry> contexts, ActionContext actionContext, String methodName, Throwable exception){
		List<Thing> thingList = new ArrayList<Thing>();
		for(ThingEntry entry : contexts){
			thingList.add(entry.getThing());
		}
		
		return doThingContextMethod(thingList, actionContext, methodName, exception);
	}
	
	/**
	 * 执行上下文的成功或者失败的方法。
	 * 
	 * @param selfContexts
	 * @param binding
	 * @param methodName
	 * @param exception
	 * @return
	 */
	public static Throwable doThingContextMethod(List<Thing> contexts, ActionContext actionContext, String methodName, Throwable exception){
		if(contexts.size() == 0){
			return exception;
		}
		
		String tempMethodName = methodName;
		
		Bindings bindings = actionContext.peek();
		bindings.put("exception", exception);
		//按照从后往前的顺序执行
		for(int i=contexts.size() - 1; i>=0; i--){
			Thing contextObj = contexts.get(i);
			ActionContext acContext = bindings.getContexts().get(contextObj);
			if(acContext != null){
				acContext.peek().put("exception", exception);
			}
			
			if(contextObj == null || acContext == null || acContext.getScope(0).getCaller() != contextObj){
				//如果上下文对象不存在或者上下文为初始化，那么不执行此上下文的其他方法
				continue;
			}
						
			String onError = contextObj.getString("onError");
			//所有的上下文都必须执行，避免某些上下文的资源未清空
			try{
				contextObj.doAction(tempMethodName, acContext);
				
				if("exception".equals(methodName) && exception != null){
					String preventError = contextObj.getString("preventError");
					if("true".equals(preventError)){
						exception = null;						
					}
				}
			}catch(Exception e){
				log.error("执行" + contextObj.getMetadata().getPath() + "上下文方法" + methodName + "失败：", e);
				//如果上下文抛出异常，那么后面的也执行异常的方法
				if("exception".equals(onError)){
					tempMethodName = "exception";		
				}
				
				if(exception == null){
					exception = e;						
				}
			}
		}
		
		return exception;
	}		
	
	/**
	 * 初始化上下文。
	 * 
	 * @param context
	 * @param actionContext
	 */
	public static void initContext(Thing context, ActionContext actionContext){
		if(context == null || context.getBoolean("disable")){
			return;
		}
		
		Bindings bindings = actionContext.peek();
		ActionContext acContext = new ActionContext();
		acContext.peek().put("acContext", actionContext);
		acContext.getScope(0).setCaller(context, "init");
		
		//查看是否继承，如果继承那么使用上级的脚本
		Object inheritObj = null;
		boolean needInherit = context.getBoolean("inherit");
		if(needInherit){
			inheritObj = context.doAction("inherit", acContext);
		}
		
		if(inheritObj != null && inheritObj instanceof ActionContext){
			//如果继承了，那么什么事都不作
			bindings.getContexts().put(context, (ActionContext) inheritObj);
		}else{
			//使用新的上下文
			context.doAction("init", acContext);
			bindings.getContexts().put(context, acContext);
		}		
	}
	
	/**
	 * 初始化上下文。
	 * 
	 * @param thingContext 事物定义的上下文
	 * @param context 动作上下文
	 * @param datas 变量
	 */
	//private static void initThingContext(Thing thingContext, ActionContext context, Map<Object, Object> datas){
		
	//}
	
	public ClassLoader getClassLoader(){
		return classLoader;
	}	
	
	public String getCompileClassPath(){
		return getThing().getMetadata().getThingManager().getClassLoader().getCompileClassPath();
	}
	
	/**
	 * 返回动作对应的日志。
	 * 
	 * @return
	 */
	public Logger getLogger(){
		return this.logger;
	}
	
	/**
	 * 返回正确的包名，因在X-Meta包名和类名没有约束，但java的包名和类名不能是关键字，所以修改，在关键字前加t。
	 * 
	 * @param className
	 * @return
	 */
	public static String getClassName(String className){
		String[] cns = className.split("[.]");
		String cName = "";
		for(int i=0; i<cns.length; i++){
			for(int n=0; n<javaKeyWords.length; n++){
				if(cns[i].equals(javaKeyWords[n])){
					cns[i] = "t" + cns[i];
					break;
				}
			}
			
			//替换-为_
			cns[i] = cns[i].replaceAll("(-)", "_");
			//替换空格
			cns[i] = cns[i].replace(' ', '_');
			if(cName.length() == 0){
				cName = cns[i];
			}else{
				cName = cName + "." + cns[i];
			}
		}		
		
		return cName;
	}
	
	/**
	 * 设置数据。
	 * 
	 * @param key 键
	 * @param data 值
	 */
	public void setData(String key, Object data){
		userData.put(key, data);
	}
	
	/**
	 * 通过键值获取数据。
	 * 
	 * @param key 键
	 * @return 数据
	 */
	public Object getData(String key){
		return userData.get(key);
	}
	
	public Thing getThing(){
		return thingEntry.getThing();
	}
	
	public static long getClassCompileTime(String classFileName){
		File file = new File(classFileName);
		String path = file.getParentFile().getAbsolutePath();
		String className = file.getName();
		
		ClassCompileTimeFile timeFile = getClassCompileTimeFile(path);
		if(timeFile != null){
			return timeFile.getTime(className);
		}else{
			return 0;
		}
	}
	
	private static void updateClassCompileTime(String classFileName, long time){
		File file = new File(classFileName);
		String path = file.getParentFile().getAbsolutePath();
		String className = file.getName();
		
		ClassCompileTimeFile timeFile = getClassCompileTimeFile(path);
		if(timeFile != null){
			timeFile.updateTime(className, time);
		}
	}
	
	private static ClassCompileTimeFile getClassCompileTimeFile(String path){
		synchronized(classTimeFiles){
			SoftReference<ClassCompileTimeFile> fileRef = classTimeFiles.get(path);
			ClassCompileTimeFile file = null;
			if(fileRef != null){
				file = fileRef.get();
			}
			
			if(file == null){
				file = new ClassCompileTimeFile(path);
				fileRef = new SoftReference<ClassCompileTimeFile>(file);
				classTimeFiles.put(path, fileRef);
			}
			
			return file;
		}
	}
	
	/**
	 * 记录类编译时间的文件，一个目录下只有一个记录类编译时间的文件，类编辑时间的文件名是_classTime.txt。
	 * 
	 * @author zhangyuxiang
	 *
	 */
	static class ClassCompileTimeFile{
		Map<String, Long> classTimes = new HashMap<String, Long>();
		String timeFileName = null;
		
		public ClassCompileTimeFile(String path){
			File file = new File(path);
			if(!file.exists()){
				file.mkdirs();
			}
			
			File timeFile = new File(file, "_classTime.txt");
			timeFileName = timeFile.getAbsolutePath();
			
			if(timeFile.exists()){
				FileInputStream fin = null;
				try{
					fin = new FileInputStream(timeFile);
				
					BufferedReader br = new BufferedReader(new InputStreamReader(fin));
					String line = null;
					while((line = br.readLine()) != null){
						line = line.trim();
						if("".equals(line)){
							continue;
						}
						
						String[] strs = line.split("[|]");
						if(strs.length == 2){
							classTimes.put(strs[0], Long.parseLong(strs[1]));
						}
					}
				}catch(Exception e){
					log.error("init class compile time file error, " + timeFileName, e);
				}finally{
					if(fin != null){
						try {
							fin.close();
						} catch (IOException e) {
						}
					}
				}
			}
		}
		
		public long getTime(String className){
			Long time = classTimes.get(className);
			if(time != null){
				return time.longValue();
			}else{
				return 0;
			}
		}
		
		public synchronized void updateTime(String className, long time){
			classTimes.put(className, time);
			
			FileOutputStream fout = null;
			try{
				fout = new FileOutputStream(timeFileName);
				for(String key : classTimes.keySet()){
					Long ctime = classTimes.get(key);
					fout.write((key + "|" + ctime + "\n").getBytes());
				}				
			}catch(Exception e){		
				log.error("update class compile time file error, " + timeFileName, e);
			}finally{
				if(fout != null){
					try {
						fout.close();
					} catch (IOException e) {
					}
				}
			}
		}
	}
	
	class Rate{
		int minRate;
		int maxRate;
	}
	
	class ActionResult{
		String name;
		String runType;
		String condition;
		ThingEntry resultObj;		
		
		public ActionResult(Thing resultObj){
			this.name = resultObj.getMetadata().getName();
			this.runType = resultObj.getString("type");
			this.resultObj = new ThingEntry(resultObj);
			this.condition = resultObj.getString("condition");
			
			//for(DataObject child : resultObj.getAllChilds()){
			//	scripts.add(new ScriptObject(child));
			//}
		}		
		
		/**
		 * 原来执行的是动作，现在使用事物执行，原来可能为了保留调用者的self，现在self变量
		 * 被结果动作事物替换，调用者的变量引用有程序编写者自行控制。2011-11-04张玉祥
		 * 
		 * @param actionContext
		 * @return
		 * @throws Exception
		 */
		public Object run(ActionContext actionContext) throws Exception{
			Thing resultThing = resultObj.getThing();
			if(resultThing == null){
				return null;
			}
			List<Thing> rchilds = resultThing.getAllChilds();
						
			//执行
			if(runType != null && runType.startsWith(ActionContext.RUNTYPE_RANDOM)){
				Collections.shuffle(rchilds, new Random(System.currentTimeMillis()));
			}
			
			List<Rate> rates = null;
			Random random = new Random();
			int maxRate = 0;
			if(ActionContext.RUNTYPE_RANDOM_RATE.equals(runType)){
				//按概率执行时初始化概率组
				rates = new ArrayList<Rate>();
				for(Thing child : rchilds){
					int rate = 1;
					try{
						rate = child.getInt("rate");
					}catch(Exception e){						
					}
					if(rate <= 0){
						rate = 1;
					}
					Rate r = new Rate();
					r.minRate = maxRate;
					maxRate += rate;
					r.maxRate = maxRate;
					rates.add(r);
				}
			}
			int runCount = rchilds.size()> 0 ? random.nextInt(rchilds.size()) : 0;			
			int count = 0;
			Object result = null;
			while(count < rchilds.size()){
				Exception aexception = null;
				boolean successed = false;
				
				Thing data = rchilds.get(count);
				try{
					if(ActionContext.RUNTYPE_RANDOM_RATE.equals(runType)){
						int rate = random.nextInt(maxRate);
						Rate r = rates.get(count);
						if(!(r.minRate <= rate && r.maxRate > rate)){
							//命中概率的才执行
							continue;
						}
					}
					result = data.getAction().run(actionContext, null, true);					
					if(result instanceof String){
						if("success".equals(result)){
							successed = true;
						}
					}else{
						successed = true;
					}
				}catch(Exception ee){
					aexception = ee;
				}
									
				if(successed){
					if((ActionContext.RUNTYPE_SUCCESS.equals(runType) || ActionContext.RUNTYPE_RANDOM_SUCCESS.equals(runType))){
						break;
					}
				}else{
					if((ActionContext.RUNTYPE_SUCCESS.equals(runType) || ActionContext.RUNTYPE_RANDOM_SUCCESS.equals(runType))){
						if(aexception != null){
							log.error("run script method", aexception);
						}
					}else if(aexception != null){
						throw aexception;
					}
				}
				if(ActionContext.RUNTYPE_RANDOM_RATE.equals(runType)){
					break;
				}
					
				if(ActionContext.RUNTYPE_RANDOM_ONE.equals(runType)){
					break;
				}
				if(ActionContext.RUNTYPE_RANDOM_RANDOM.equals(runType) && runCount == count){
					break;
				}
				
				int sint = actionContext.getStatus();
				if(sint == ActionContext.RETURN || sint == ActionContext.BREAK ||
					sint == ActionContext.CONTINUE){
					break;
				}else if(sint == ActionContext.CANCEL){
					actionContext.setStatus(ActionContext.RUNNING);
					break;
				}
				count ++;				
			}				
			
			return result;
		}
	}
}