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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xmeta.annotation.ActionAnnotationHelper;
import org.xmeta.cache.ThingEntry;
import org.xmeta.thingManagers.ClassThingManager;
import org.xmeta.util.*;

/**
 * <p>动作是由模型转化而来的，动作是可以执行的，是把模型当作程序来执行的方法。</p>
 * 
 * <p>本动作能够直接解释执行的模型是类型为JavaAction的模型，JavaAction调用Java代码。
 * 其它类型的动作，比如Groovy等脚本语言，可以用JavaAction来实现。</p>
 * 
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public class Action extends Semaphore{
	/** 日志 */
	//private static Logger log = LoggerFactory.getLogger(Action.class);	
	private static final Logger log = Logger.getLogger(Action.class.getName());
	
	private static final World world = World.getInstance();
	
	/** 记录异常的列表 */
	private static final List<ThrowableRecord> throwables = new ArrayList<>();
	/** 记录异常的数量 */
	private static int throwableRecordCount = 0;
	
	/** 记录类编译时间的文件类集合 */
	private static final Map<String, SoftReference<ClassCompileTimeFile>> classTimeFiles = new HashMap<>();
	
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
		
	public final static String str_acContext = "acContext";
	public final static String str_parentContext = "parentContext";
	public final static String str_action = "action";
	public final static String str_actionThing = "actionThing";
	/** 源码类型-类库 */
	public final static byte SOURCE_LIB = 0;
	/** 源码类型-事物管理器 */
	public final static byte SOURCE_THINGMANAGER = 1;
	/** 源码类型-模型 */
	public final static byte SOURCE_THING = 2;
	
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
	public long lastModified = 0;
	
	/** 是否使用其他事物定义的动作 */
	private boolean useOtherAction;
	
	/** 解释方式 */
	private boolean isSelfInterpretationType;
	
	/** 其他事物的路径 */
	private String otherActionPath;
	
	/** 动作事物的上下文列表 */
	private List<ThingEntry> contexts;
	
	/** Java类装载器，动态装载类 */
	private ClassLoader classLoader = null;
	
	/** 编译Java源文件时要用到的类库路径 */
	//public String classPath;
	
	/** 编译后的类名 */
	private String className;
	
	/** 编译后的类存放的目录 */
	private String classFileName;
	
	/** 类的包名 */
	private String packageName;
	
	/** 保存Java代码的文件名 */
	private String fileName;	
	
	/** 代码 */
	private String code;
	
	/** 要运行的方法名 */
	private String methodName;

	/** 是否在执行时替换属性模板 */
	private boolean attributeTemplate;	
	
	/** 编译后并且装载了的类 */
	private Class<?> actionClass = null;
	
	/** 动作的定义是否已经改变 */
	private boolean changed = false;
	
	/** 外部动作，如果有引用*/
	private Action outerAction = null;
	
	/** 如果是Java动作，则有方法 */
	private Method method = null;
	
	/** 是否根据返回值Switch子节点 */
	private boolean switchResult = false;
	
	/** 用户数据，在代码或脚本理可以设置和Action绑定的数据 */
	private final Map<String, Object> userData = new HashMap<>();
	
	/** 子动作列表 */
	private List<ActionResult> results;
	
	/** 是否保存返回值 */
	private boolean saveReturn;
	
	/** 返回值变量名 */
	private String returnVarName;
	
	/** 是否要创建本地变量范围  */
	private boolean isCreateLocalVarScope;
	
	/** 子动作的定义 */
	private Map<String, Action> actionsDefiend = null;
	
	/** Class注解，对JavaAction生效 */
	private ActionAnnotationHelper annotationHelper;
	
	/** 是否动作定义了Variables子节点，如果定义了在创建时用于生成局部变量 */
	private boolean hasVariables = false;
		
	private boolean codeInited = false;

	/** 如果存在，用于替换动作模型本身的执行 */
	private JavaAction javaAction;

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
			//log.warn("init action error, action=" + thing.getMetadata().getPath(), e);
			StringBuilder sb = new StringBuilder("init action error, action=");
			sb.append(thing.getMetadata().getPath());
			String initExceptionMessage = thing.getStringBlankAsNull("initExceptionMessage");
			if(initExceptionMessage != null){
				sb.append("\n");
				sb.append(initExceptionMessage);
			}
			throw new ActionException(sb.toString(), e);
		}
	}
		
	/**
	 * 检查动作是否已经变更。
	 */
	public void checkChanged(){
		if(lastModified != thingEntry.getThing().getMetadata().getLastModified()){
			if(lastModified != 0){
				changed = true;
			}
			
			try{
				init();
			}catch(Exception e){
				throw new ActionException("", e);
			}	
		}
	}
	
	public String getClassTargetDirectory(){		
		String fileManagerName = thingEntry.getThing().getMetadata().getThingManager().getName();
		if(fileManagerName == null){
			fileManagerName = "null";
		}else{
			fileManagerName = UtilString.trimFileName(fileManagerName);
		}
		return World.getInstance().getPath() + "/work/actionClasses/" +  fileManagerName + "/"; 
	}
	
	private void init() throws ClassNotFoundException, IOException, SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException{
		Thing thing = thingEntry.getThing();

		//params = thing.getThing("ins@0");
		
		//初始化共有的变量		
		if(thing.getAttribute("throwException") == null){
			throwException = true;
		}else{
			throwException = thing.getBoolean("throwException");
		}
		isSynchronized = thing.getBoolean("isSynchronized");
		lastModified = thing.getMetadata().getLastModified();

		contexts = new ArrayList<>();
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
		//disableGlobalContext = thing.getBoolean("disableGlobalContext");
		
		isSelfInterpretationType = "Self".equals(thing.getString("interpretationType"));
		
		//动作定义
		List<Thing> actionsThingList = thing.getChilds("ActionDefined");
		if(actionsThingList.size() > 0){
			actionsDefiend = new HashMap<String, Action>();
			for(Thing actionsThing : actionsThingList){
				for(Thing actionThing : actionsThing.getChilds()){
					actionsDefiend.put(actionThing.getMetadata().getName(), actionThing.getAction());
				}
			}
		}else{
			actionsDefiend = null;
		}
		
		//变量定义
		if(thing.getChilds("Variables").size() > 0) {
			hasVariables = true;
		}
		
		
		//返回值
		returnVarName = thing.getString("returnVarName");
		saveReturn = thing.getBoolean("saveReturn");
		if(saveReturn && (returnVarName == null || "".equals(returnVarName))){
			returnVarName = thing.getMetadata().getName();
		}
		switchResult = thing.getBoolean("switchResult");
		
		//设置代码文件名、类名和编译后的类路径等，这部分代码对大多数动作是无用的
		codeInited = false;
		
		
		//设置是否使用外部的Java，默认是使用外部java,使用上面的sourceType和javaClassName替代了
		/*
		if(thing.get("useOuterJava") == null){
			useOuterJava = true;
		}else{
			useOuterJava  = thing.getBoolean("useOuterJava");
		}
		outerClassName = thing.getString("outerClassName");
		*/
					
		//需要重新编译
		actionClass = null;
		isJava = "JavaAction".equals(thing.getThingName());
		
		results = new ArrayList<>();
		for(Thing child : thing.getAllChilds("Result")){
			ActionResult result = new ActionResult(child);		
			results.add(result);
		}
		
		isCreateLocalVarScope = thing.getBoolean("createLocalVarScope");

		//查找是否有替换本模型的
		javaAction = JavaActionFactory.getActionJavaAction(thing.getMetadata().getPath());
		if(isJava){
			String javaClassName;
			byte sourceType = SOURCE_LIB;
			if(thing.getBoolean("useOuterJava")) {
				javaClassName = thing.getString("outerClassName");
			} else if(thing.getBoolean("useInnerJava")) {
				sourceType = SOURCE_THINGMANAGER;
				javaClassName = thing.getString("outerClassName");
			}else {
				sourceType = SOURCE_THING;
				initClassAndCode();
				javaClassName = this.className;//packageName + "." + thing.getString("className");
			}

			initJava(thing,  sourceType, javaClassName);
		}else if(useOtherAction){
			outerAction = world.getAction(otherActionPath);
		}
	}
	
	private void initJava(Thing thing, byte sourceType, String javaClassName) throws ClassNotFoundException, IOException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		initClassAndCode();

		if(javaAction == null){
			javaAction = JavaActionFactory.getJavaAction(javaClassName, methodName);
		}
		if(javaAction != null){
			return;
		}

		//查看是否需要编译获得重新装载
		if(actionClass == null){
			changed = false;				
			//需要编译或重新装载
			if(sourceType == SOURCE_LIB){
				//外部类库的Java类，直接加载
				if(javaClassName == null || "".equals(javaClassName)) {
					throw new ActionException("Out class name not setted, action=" + thing.getMetadata().getPath());
				}
				ClassLoader clsLoader = thing.getMetadata().getCategory().getClassLoader();
				actionClass = clsLoader.loadClass(javaClassName); 						
			}else{			
				//内部需要自行编译通过ActionClassLoader加载的
				boolean recompile = this.isNeedRecompile();
				File classFile = new File(classFileName);
				if(!classFile.exists()){
					recompile = true;
				}
				
				ThingClassLoader pclssLoader = thing.getMetadata().getCategory().getClassLoader();

				String compleClassPath = pclssLoader.getCompileClassPath();					
				//编程模式才会重新编译
				if(recompile && world.getMode() == World.MODE_PROGRAMING){
					if(sourceType == SOURCE_THINGMANAGER){
						String javaFileName = javaClassName.replace('.', '/') + ".java";
						ThingManager thingManager = thing.getMetadata().getThingManager(); 
						URL sourceURL = thingManager.findResource(javaFileName);
						if(sourceURL == null) {
							thingManager.findResource("/" + javaFileName);
						}							
						if(sourceURL != null) {
							String sourcePath = World.getInstance().getPath() + "/work/actionSources/" + thingManager.getName() + "/"; 
							File codeFile = new File(sourcePath + javaFileName);
							if(!codeFile.exists()) {
								codeFile.getParentFile().mkdirs();
							}

							try (FileOutputStream fout = new FileOutputStream(codeFile)) {
								InputStream uin = sourceURL.openStream();
								byte[] bytes = new byte[4096];
								int length = -1;
								while ((length = uin.read(bytes)) != -1) {
									fout.write(bytes, 0, length);
								}
								uin.close();
							}
					
							boolean use16 = true;
							boolean compiled = false;
							try{							
								Class.forName("javax.tools.JavaCompiler");
							}catch(Exception e){
								use16 = false;
							}
							
							if(use16){
								compiled = JavaCompiler16.compile(compleClassPath, sourcePath, codeFile, getClassTargetDirectory());
							}							
							if(!compiled){
								JavaCompiler15.compile(compleClassPath, sourcePath, codeFile.getAbsolutePath(), getClassTargetDirectory());								
							}	
						}else{
							throw new ActionException("Code file not found, java=" + javaFileName + ", actionThing=" + thing.getMetadata().getPath());
						}
					}else{
						File codeFile = new File(fileName + ".java");
						if(!codeFile.exists()){
							codeFile.getParentFile().mkdirs();
						}

						try (FileOutputStream fout = new FileOutputStream(codeFile)) {
							//文件头增加一个事物路径的标识
							fout.write(("/*path:" + thing.getMetadata().getPath() + "*/\n").getBytes());
							fout.write(("package " + packageName + ";\n\n").getBytes());
							fout.write(code.getBytes());
						}
						
						File classDir = new File(world.getPath() + "/work/actionClasses");
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
							compiled = JavaCompiler16.compile(compleClassPath, null, codeFile, getClassTargetDirectory());
						}							
						if(!compiled){
							JavaCompiler15.compile(compleClassPath, null, fileName, getClassTargetDirectory());								
						}	
					}
					
					updateCompileTime();
				}
				
				//if(thing.getBoolean("useInnerJava")){
				actionClass = getClassLoader().loadClass(javaClassName); 	
				//}else{
				//	actionClass = getClassLoader().loadClass(className);
				//}
			}
			//java.lang.Compiler.compileClass(actionClass);
			try{	
				if(methodName != null && !"".equals(methodName)){
					method = getDeclaredMethod(actionClass, methodName);//actionClass.getDeclaredMethod(methodName, new Class[]{ActionContext.class});
					if(method == null) {
						throw new NoSuchMethodException(methodName);
					}
					annotationHelper = ActionAnnotationHelper.parse(actionClass, method);
				}
			}catch(Throwable e){		
				throw new ActionException("load method error, class=" + actionClass.getName() 
						+ ", method=" + methodName + ",action=" + thing.getMetadata().getPath(), e);
			}
		}else if(method == null){
			try{	
				if(methodName != null && !"".equals(methodName)){
					method = getDeclaredMethod(actionClass, methodName);//actionClass.getDeclaredMethod(methodName, new Class[]{ActionContext.class});
					if(method == null) {
						throw new NoSuchMethodException(methodName);
					}
					annotationHelper = ActionAnnotationHelper.parse(actionClass, method);
				}
			}catch(Exception e){		
				throw new ActionException("", e);
			}
		}		
	}
	
	public ClassLoader getClassLoader() throws MalformedURLException {
		if(this.classLoader == null || changed) {			
			Thing thing = thingEntry.getThing();
			
			//查看是否是引用其它动作的类加载器
			String actionClassLoader = thing.getStringBlankAsNull("actionClassLoader");
			if(actionClassLoader != null) {
				Action action = World.getInstance().getAction(actionClassLoader);
				if(action != null) {
					classLoader = action.getClassLoader();
					return classLoader;
				}
			}
			
			//初始化类装载器和编译时的类库路径
			ThingClassLoader pclssLoader = thing.getMetadata().getCategory().getClassLoader();
			
			//String compleClassPath = pclssLoader.getCompileClassPath();
			if(world.getMode() == World.MODE_WORKING){
				classLoader = pclssLoader;
			}else{
				String fileManagerName = thing.getMetadata().getThingManager().getName();
				if(fileManagerName == null){
					fileManagerName = "null";
				}else{
					fileManagerName = UtilString.trimFileName(fileManagerName);
				}
				
				File classDir = new File(world.getPath() + "/work/actionClasses/" + fileManagerName);
				if(!classDir.exists()){
					classDir.mkdirs();
				}
				classLoader = new ActionClassLoader(new URL[]{classDir.toURI().toURL()}, pclssLoader);
			}
		}
		
		return classLoader;
	}
	
	private Method getDeclaredMethod(Class<?> cls, String methodName) throws Exception{
		Exception exception = null;
		try {
			return cls.getDeclaredMethod(methodName, ActionContext.class);
		}catch(Exception e) {
			exception = e;
		}
				
		for(Method method : cls.getMethods()) {
			if(method.getName().equals(methodName) ) {
				return method;
			}
		}

		throw exception;
	}
	
	/**
	 * 返回动作的类，如果存在。有些动作可能会返回null。
	 * 
	 * @param actionContext 变量上下文
	 * @return 返回Action对应的类
	 */
	@SuppressWarnings({"rawtypes" })
	public Class getActionClass(ActionContext actionContext){
		Thing thing = thingEntry.getThing();
		if(lastModified != thing.getMetadata().getLastModified()){
			if(lastModified != 0){
				changed = true;
			}
			try{
				init();
			}catch(Exception e){
				throw new ActionException("", e);
			}
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
		updateClassCompileTime(classFileName, this.className, lastModified);
	}
	
	public Method getMethod(){
		return method;
	}
	
	public final <T> T  run(){
		return runArrayParams(new ActionContext(), (Object[]) null, null, false);
	}
	
	public final <T> T  run(ActionContext actionContext){
		return runMapParams(actionContext, null, null, false);
	}
	
	public final <T> T  run(ActionContext actionContext, Object... params){
		return runArrayParams(actionContext, params, null, false);
	}
	
	public final <T> T  exec(Object... params){
		return runArrayParams(null, params, null, false);
	}
	
	public final <T> T  exec(ActionContext actionContext, Object... params){
		return runArrayParams(actionContext, params, null, false);
	}
	
	public final <T> T call(ActionContext actionContext, Object ... params){
		return runArrayParams(actionContext, params, null, false);
	}
	
	public final <T> T  call(ActionContext actionContext, Map<String, Object> parameters){
		return runMapParams( actionContext, parameters, null, false);
	}
	
	public final <T> T  run(ActionContext actionContext, Map<String, Object> parameters){
		return runMapParams( actionContext, parameters, null, false);
	}
	
	public final <T> T  run(ActionContext actionContext, Map<String, Object> parameters, boolean isSubAction){
		return runMapParams(actionContext, parameters, null, isSubAction);
	}
	
	public final <T> T  run(ActionContext actionContext, Map<String, Object> parameters, Object caller, boolean isSubAction) {
		return runMapParams(actionContext, parameters, caller, isSubAction);
	}
	
	public final <T> T runArrayParams(ActionContext actionContext, Object[] params_, Object caller,  boolean isSubAction){
		if(actionContext == null){
			actionContext = new ActionContext();
		}
		
		Bindings bindings = new Bindings();
		bindings.setParameterScope(true);
		if(params_ != null){
			for(int i=0; i<params_.length - 1; i++){
				bindings.put((String) params_[i], params_[i + 1]);
				i++;
			}
		}
		/*
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
		}*/
		
		return this.dorun(actionContext, bindings, bindings, caller, isSubAction);
	}
	
	public final <T> T runMapParams(ActionContext actionContext, Map<String, Object> parameters, Object caller, boolean isSubAction) {
		if(actionContext == null){
			actionContext = new ActionContext();
		}
		
		Bindings bindings = new Bindings();
		bindings.setParameterScope(true);
		if(parameters != null){
			bindings.putAll(parameters);
		}
		
		return this.dorun(actionContext, bindings, parameters, caller, isSubAction);
	}
	
	/**
	 * 执行动作。
	 */
	@SuppressWarnings("unchecked")
	private <T> T dorun(ActionContext actionContext, Bindings bindings, Map<String, Object> parameters, Object caller, boolean isSubAction) {
		//long start = System.nanoTime();
		//log.info("dorun started");
		//是否禁止全局上下文具有继承的性质
		//if(context.peek().disableGloableContext ||  this.disableGlobalContext){
		//	bindings.disableGloableContext = this.disableGlobalContext;
		//}
		
		//动作记录
		if(!actionContext.isDisableGloableContext() && world.isHaveActionListener()){
			ActionListener listener = world.getActionListener();
			try{
				listener.actionExecuted(this, caller, actionContext, parameters, -1, true);
			}catch(Throwable t){
				log.log(Level.WARNING, "ActionRecorder error", t);
			}
		}
		
		//判断事物是否已经是否变更，如果变更了那么重新初始化
		Thing thing = thingEntry.getThing();
		if(lastModified != thing.getMetadata().getLastModified()){
			if(lastModified != 0){
				changed = true;
			}
			
			try{
				init();
			}catch(Exception e){
				throw new ActionException("", e);
			}
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
		actionContext.push(bindings);		
		//if(!isSubAction || isCreateLocalVarScope){ 2016-04-15现在方法的调用没有自动的局部变量范围了
		if(isCreateLocalVarScope){
			bindings.setVarScopeFlag();//.isVarScopeFlag = true;//可以判定是函数调用的入口，因此设置一个局部变量范围标志
		}
		actionContext.pushAction(this);
				
		//总是输入一些常量
		bindings.put("world", world);
		//if(logger != null){
		//	bindings.put("log", logger);
		//}

		//预制的动作
		if(actionsDefiend != null){
			bindings.putAll(actionsDefiend);
		}
		
		//预制的变量
		if(hasVariables) {
			for(Thing vars : thing.getChilds("Variables")){
	        	for(Thing var : vars.getChilds()){
	        		String key = var.getMetadata().getName();
	        		Object value = var.getAction().run(actionContext, null, true);
	        		bindings.put(key, value);
	        	}
	        }
		}
				
		Object result = null;
		//动作上下文事物
		List<Thing> allContexts = getContextThings(actionContext);
		/*
		List<ThingEntry> allContexts = new ArrayList<ThingEntry>();
		if(!disableGlobalContext){
			allContexts.addAll(world.globalContexts);
		}
		if(contexts.size() > 0){
			allContexts.addAll(contexts);
		}*/
		
		//初始化脚本内的上下文
		bindings.setCaller(this, null);
		bindings.world = world;	
		
		try{
			//首先是全局上下文		
			if(allContexts.size() > 0){
				for(Thing contextThing : allContexts){
					initContext(this, contextThing, actionContext);
				}
			}

			if(javaAction != null){
				result = javaAction.run(actionContext);
			}else if(useOtherAction){
				//如果一个动作是引用其他动作的，那么执行被引用的动作 
				Bindings callerBindings = actionContext.getScope(actionContext.getScopesSize() - 2);
				try{
					actionContext.push(callerBindings);
					result = outerAction.run(actionContext, parameters, isSubAction);
				}finally{
					actionContext.pop();
				}
			}else if(isJava){
				//如果动作时原生动作，即Java动作，那么通过反射机制调用 
				if(actionClass != null){
					if(method != null){
						Object classInstance = null;
						Object[] paramValues = null;
						if(annotationHelper != null) {
							if((method.getModifiers() & Modifier.STATIC) != Modifier.STATIC){
								//静态方法不要创建实例
								classInstance = annotationHelper.createObject(actionContext);
							}
							paramValues = annotationHelper.getParamValues(actionContext);
						}
						
						//如果不是静态方法，实例化一个对象
						if(classInstance == null && ((method.getModifiers() & Modifier.STATIC) != Modifier.STATIC)){							
							classInstance = actionClass.getConstructor(new Class<?>[0]).newInstance();
						}
						
						if(paramValues == null) {
							if(method.getParameterCount() > 0) {
								paramValues = new Object[]{actionContext};
							}else {
								paramValues = new Object[0];
							}
						}
						
						result = method.invoke(classInstance, paramValues);
					}else{
						throw new ActionException("Java action method not setted");
						//logger.info("Java action method is null, " + getThing().getMetadata().getPath());
					}
				}
			}else{
				//如果不是Java，那么调用当前动作事物的run行为，run行为有可能是自定义的也有可能是其描述者定义的 
			    //此方法很重要，用这个方法可以实现其他语言的支持，比如Groovy、JavaScript等脚本语言 
				if(isSelfInterpretationType){
					//self类型的动作，当一个动作作为子动作时，而变量self还需要是自己，那么需要定义成此类型 
					if(attributeTemplate){
						//属性模板，动作的属性可以设置成freemarker模板，在执行时用模板生成真正的属性值 
						Thing fthing = thing.run("processAttributeTemplate", actionContext, (Map<String, Object>) null, isSubAction, true);
						if(fthing != null){
							result = fthing.run("run", actionContext, (Map<String, Object>) null, isSubAction, true);
						}
					}else{
						result = thing.run("run", actionContext, (Map<String, Object>) null, isSubAction, true);
					}
				}else{
					//获取事物自己的行为run，然后执行 
					Thing actionThing = thing.getActionThing("run");
					if(actionThing != null){
						Action ac = null;
						try{
							ac = actionThing.getAction();
						}catch(Exception e){
							throw new ActionException("Get run action error, thing=" + thing.getMetadata().getPath(), e);
						}
						result = ac.run(actionContext, null, caller, isSubAction);
					}
				}
				//result = thing.doAction("run", context, parameters, isSubAction);
			}
			
			//返回值的变量保存
			if(saveReturn && returnVarName != null  && !"".equals(returnVarName)){
				Bindings returnVarBindings = UtilAction.getVarScope(thingEntry.getThing(), actionContext);
				if(returnVarBindings != null){
					returnVarBindings.put(returnVarName, result);
				}
			}
			
			//Switch子节点
			if(switchResult) {
				String resultStr = String.valueOf(result);
				for(Thing switchs : thing.getChilds("ResultSwitch")) {
					boolean ok = false;
					for(Thing child : switchs.getChilds()) {
						if(resultStr.equals(child.getMetadata().getName())) {
							result = child.getAction().run(actionContext, null, caller, isSubAction);
							ok = true;
							break;
						}
					}
					
					if(ok) {
						break;
					}
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
						execute = (Boolean) OgnlUtil.getValue(scObj.condition, context);
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
			
			if(actionContext.getStatus() == ActionContext.EXCEPTION && !isSubAction){
				//动作抛除异常
				if(actionContext.getThrowedObject() instanceof Throwable){
					throw (Throwable) actionContext.getThrowedObject();
				}else{
					Object throwedObject = actionContext.getThrowedObject();
					if(throwedObject != null){
						if(throwedObject instanceof Throwable){
							throw (Throwable) throwedObject;
						}else{
							throw new ActionException(throwedObject.toString());
						}
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
			Throwable exception = doContextMethod(allContexts, actionContext, contxtMethod, null, result);
			if(exception == null){				
				return (T) result;			
			}else{
				if(!throwException){
					logHideenExceptionStackTrace(exception, actionContext);
					return null;
				}else{
					throw exception;
					/*
					if(exception instanceof RuntimeException){
						throw (RuntimeException) exception;
					}else{
						throw new ActionException("", exception);
					}*/
				}
			}
		}catch(Throwable e){			
			Throwable exception = doContextMethod(allContexts, actionContext, "exception", e, null);
			
			if(throwableRecordCount > 0){
				throwables.add(new ThrowableRecord(exception, actionContext));
				while(throwables.size() > throwableRecordCount ){
					throwables.remove(0);
				}
			}
			if(exception == null){
				return null;
			}else{
				//exception.printStackTrace();
				if(!throwException){
					logHideenExceptionStackTrace(e, actionContext);
					//log.warn("action exception is hidded : " + thing.getMetadata().getPath(), e);
					return null;
				}else{
					throw wrapToActionException(exception, actionContext);
					/*
					if(exception instanceof RuntimeException){
						throw (RuntimeException) exception;
					}else{
						throw new ActionException(exception);
					}*/
				}
			}			
		}finally{
			actionContext.pop();
			actionContext.popAction();
			
			if(!isSubAction){
				actionContext.setStatus(ActionContext.RUNNING);
			}
			
			//如果是同步的，那么通知其他线程这里已经结束
			if(thisIsSynchronized){
				this.finished();
			}
			
			//log.info("dorun ended");
		}
	}
	
	private List<Thing> getContextThings(ActionContext actionContext){
		List<Thing> allContexts = new ArrayList<>();
		//是否禁止全局变量上下文
		if(!actionContext.isDisableGloableContext()){
			for(ThingEntry entry : world.globalContexts){
				addContextThing(allContexts, entry.getThing());
			}		
		}

		if(contexts.size() > 0){
			for(ThingEntry entry : contexts){
				addContextThing(allContexts, entry.getThing());
			}
		}

		//上下文中的变量上下文呢
		if(!actionContext.isDisableGloableContext()) {
			actionContext.addContextThing(allContexts);
		}
		/*
		for(Bindings bindings : actionContext.getScopes()){
			addContextThing(allContexts, bindings.getContextThing());
		}*/
		
		return allContexts;
	}
	
	private void addContextThing(List<Thing> contexts, Thing contextThing){
		if(contextThing == null){
			return;
		}
		
		contexts.add(contextThing);
	}
	private ActionException wrapToActionException(Throwable t, ActionContext actionContext){
		if(t instanceof  InvocationTargetException){
			Throwable cause = t.getCause();
			if(cause != null){
				t = cause;
			}
		}
		
		if(t instanceof ActionException){
			return (ActionException) t;
		}
		
		return new ActionException("Action exception: " + thingEntry.getPath(), t,  actionContext);
	}
	
	private void logHideenExceptionStackTrace(Throwable t, ActionContext actionContext){
		log.log(Level.WARNING, "action ActionContext stacktrace:" + thingEntry.getPath());
		log.log(Level.WARNING, actionContext.getStackTrace());
		
		if(t instanceof InvocationTargetException){
			log.log(Level.WARNING, "action hidden throwable", t.getCause());
		}else{
			log.log(Level.WARNING, "action hidden throwable", t);
		}		
	}
	
	private static Throwable doContextMethod(List<Thing> contexts, ActionContext actionContext, String methodName, Throwable exception, Object result){
		List<Thing> thingList = new ArrayList<>(contexts);
		
		return doThingContextMethod(thingList, actionContext, methodName, exception, result);
	}
	
	/**
	 * 执行上下文的成功或者失败的方法。
	 * 
	 * @param contexts 上下文列表
	 * @param actionContext 变量上下文
	 * @param methodName 方法名称
	 * @param exception 抛出的异常
	 * @param result 运行结果
	 * 
	 * @return 如果动作可以抛出异常则抛出异常
	 */
	public static Throwable doThingContextMethod(List<Thing> contexts, ActionContext actionContext, String methodName, Throwable exception, Object result){
		if(contexts.size() == 0){
			return exception;
		}
		
		String tempMethodName = methodName;
		
		Bindings bindings = actionContext.peek();
		bindings.put("action-exception", exception);
		bindings.put("action-result", result);
		bindings.put("contexts", contexts);
		//按照从后往前的顺序执行
		for(int i=contexts.size() - 1; i>=0; i--){
			Thing contextObj = contexts.get(i);
			ActionContext acContext = bindings.getContexts().get(contextObj);			
			if(acContext != null){
				acContext.peek().put("result", result);
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
				log.log(Level.WARNING, "Execute context " + contextObj.getMetadata().getPath() + " : " + methodName + " exception.", e);
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
	 * 执行动作上下文的inherit或init方法。
	 * 
	 * @param action 动作
	 * @param context 上下文事物
	 * @param actionContext 变量上下文
	 */
	public static void initContext(Action action, Thing context, ActionContext actionContext){
		if(context == null || context.getBoolean("disable")){
			return;
		}
		
		Bindings bindings = actionContext.peek();
		ActionContext acContext = new ActionContext();
		//在这里里禁止全局动作上下文，以防止递归的出现
		acContext.setDisableGloableContext(true);
		acContext.put(str_acContext, actionContext);
		acContext.put(str_parentContext, actionContext);
		acContext.put(str_action, action);
		acContext.put(str_actionThing, action.getThing());
		acContext.getScope(0).setCaller(context, "init");
		
		//查看是否继承，如果继承那么使用上级的脚本
		Object inheritObj = null;
		boolean needInherit = context.getBoolean("inherit");
		if(needInherit){
			inheritObj = context.doAction("inherit", acContext);
			
		}
		
		if(inheritObj instanceof ActionContext){
			//如果继承了，那么什么事都不作
			bindings.getContexts().put(context, (ActionContext) inheritObj);
		}else{
			//使用新的上下文
			bindings.getContexts().put(context, acContext);
			context.doAction("init", acContext);			
		}		
	}	
	
	public String getCompileClassPath(){
		return getThing().getMetadata().getCategory().getClassLoader().getCompileClassPath();
	}
	
	/**
	 * 返回动作对应的日志。日志已取消，现在总是返回null。
	 * 
	 * @return 返回动作的日志
	 */
	//public Logger getLogger(){
	//	return null;//this.logger;
	//}
	
	/**
	 * 返回正确的包名，因在X-Meta包名和类名没有约束，但java的包名和类名不能是关键字，所以修改，在关键字前加t。
	 * 
	 * @param className 类名
	 * @return 返回完整类名
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
	
	/**
	 * 返回是否需要重新编译。
	 */
	public boolean isNeedRecompile() {
		initClassAndCode();
		
		long time = getClassCompileTime(thingEntry.getThing(), classFileName);
		return time != lastModified;
	}
	
	private long getClassCompileTime(Thing thing, String classFileName){
		initClassAndCode();
		//如果是从类路径装载的事物，尽量直接读取class，因为可能是打包运行的而不是开发环境的
		if(thing.getMetadata().getThingManager() instanceof ClassThingManager){
			try{
				//不需要重新编译才直接读取，2013-03-12
				actionClass = getClassLoader().loadClass(this.className);
				if(actionClass != null){
					return lastModified;
				}
			}catch(Throwable ignored){
			}
		}
		
		File file = new File(classFileName);
		String path = file.getParentFile().getAbsolutePath();

		ClassCompileTimeFile timeFile = getClassCompileTimeFile(path);
		return timeFile.getTime(className);
	}
	
	public static void updateClassCompileTime(String classFileName, String  className, long time){
		File file = new File(classFileName);
		String path = file.getParentFile().getAbsolutePath();

		ClassCompileTimeFile timeFile = getClassCompileTimeFile(path);
		timeFile.updateTime(className, time);
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
					br.close();
				}catch(Exception e){
					log.log(Level.WARNING, "init class compile time file error, " + timeFileName, e);
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
				return time;
			}else{
				return 0;
			}
		}
		
		public synchronized void updateTime(String className, long time){
			classTimes.put(className, time);
			
			FileOutputStream fout = null;
			try{
				File file = new File(timeFileName);
				if(!file.exists()){
					file.getParentFile().mkdirs();
				}
				
				fout = new FileOutputStream(timeFileName);
				for(String key : classTimes.keySet()){
					Long ctime = classTimes.get(key);
					fout.write((key + "|" + ctime + "\n").getBytes());
				}				
			}catch(Exception e){		
				log.log(Level.WARNING, "update class compile time file error, " + timeFileName, e);
			}finally{
				if(fout != null){
					try {
						fout.close();
					} catch (IOException ignored) {
					}
				}
			}
		}
	}
	
	static class Rate{
		int minRate;
		int maxRate;
	}
	
	static class ActionResult{
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
							log.log(Level.WARNING, "run script method", aexception);
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

	public static int getThrowableRecordCount() {
		return throwableRecordCount;
	}

	public static void setThrowableRecordCount(int throwableRecordCount) {
		Action.throwableRecordCount = throwableRecordCount;
	}

	public static List<ThrowableRecord> getThrowables() {
		return throwables;
	}
	
	public Class<?> getActionClass() {
		initClassAndCode();
		
		return actionClass;
	}
		
	public void setActionClass(Class<?> actionClass) {
		this.actionClass = actionClass;
	}

	public boolean isChanged() {
		return changed;
	}
	
	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public boolean isJava() {
		return isJava;
	}

	public long getLastModified() {
		return lastModified;
	}

	public String getOtherActionPath() {
		return otherActionPath;
	}

	public String getClassName() {
		initClassAndCode();
		
		return className;
	}

	public String getClassFileName() {
		initClassAndCode();
		
		return classFileName;
	}

	public String getPackageName() {
		initClassAndCode();
		return packageName;
	}

	public String getFileName() {
		initClassAndCode();
		return fileName;
	}

	public String getCode() {
		initClassAndCode();
		return code;
	}

	public Action getOuterAction() {
		return outerAction;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	private synchronized void initClassAndCode() {
		if(codeInited) {
			return;
		}else {
			codeInited = true;
		}
		Thing thing = thingEntry.getThing();
		
		Thing parent = thing.getParent();			
		Thing rootParent = thing.getRoot();
		if(parent == null){
			parent = thing;
		}
		String fileManagerName = thing.getMetadata().getThingManager().getName();
		if(fileManagerName == null){
			fileManagerName = "null";
		}else{
			fileManagerName = UtilString.trimFileName(fileManagerName);
		}
		
		className = rootParent.getMetadata().getPath();
		if(rootParent != thing){
			className = className + ".p" + thing.getMetadata().getPath().hashCode();
			className = className.replace('-', '_');
		}
		String cName = thing.getString("className");			    
		if(cName == null || "".equals(cName)){
		    className = className + "." + thing.getMetadata().getName();
		}else{
			className = className + "." + cName;
		}
				
		className = getClassName(className);
		
		int dotIndex = className.lastIndexOf(".");
		if(dotIndex != -1){
			packageName = className.substring(0, dotIndex);
		}
		
		fileName = fileManagerName + "/" + className.replace('.', '/');
		//fileName += ".java";
		classLoader = null;
		
		fileName = World.getInstance().getPath() + "/work/actionSources/" +  fileName;
		classFileName = World.getInstance().getPath() + "/work/actionClasses/" +  fileManagerName + "/" + className.replace('.', '/') + ".class";
		
		//设置代码和方法名
		code = thing.getString("code");
		if(code == null){
			code = "";
		}
		
		methodName = thing.getString("methodName");
	}

	/**
	 * 异常记录。
	 * 
	 * @author Administrator
	 *
	 */
	public static class ThrowableRecord{
		public Throwable throwable;
		public String actionStackTrace;
		public Date date;
		public String threadName;
		
		public ThrowableRecord(Throwable throwable, ActionContext actionContext){
			this.throwable = throwable;
			this.actionStackTrace = actionContext.getStackTrace();
			this.date = new Date();
			this.threadName = Thread.currentThread().getName();
		}
	}
}