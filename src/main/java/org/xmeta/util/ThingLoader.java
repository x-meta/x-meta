package org.xmeta.util;

import org.xmeta.ActionContext;
import org.xmeta.Thing;
import org.xmeta.annotation.ActionField;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;

public class ThingLoader {
    private static final Logger logger = Logger.getLogger(ThingLoader.class.getName());
    private static final ThreadLocal<Stack<Object>> objectLocal = new ThreadLocal<>();

    public static void push(Object object){
        Stack<Object> stack = objectLocal.get();
        if(stack == null) {
            stack = new Stack<>();
            objectLocal.set(stack);
        }
        stack.push(object);
    }

    public static void pop(){
        Stack<Object> stack = objectLocal.get();

        if(stack != null) {
            stack.pop();
        }
    }

    /**
     * 通过变量上下文对对象的有@ActionField的字段赋值
     *
     */
    @SafeVarargs
    public static <T> T load(Object object, ActionContext actionContext, Class<? extends Annotation> ... annotations){
        return load(object, (Thing) null, actionContext, annotations);
    }

    /**
     * 执行thing的create(actionContext)方法，执行后会遍历对象的注解为ActionField的字段，并从变量上下文中取值对字段赋值。
     */
    @SafeVarargs
    public static <T> T load(Object object, Thing thing, ActionContext actionContext, Class<? extends Annotation> ... annotations){
        Stack<Object> stack = objectLocal.get();
        if(stack == null) {
            stack = new Stack<>();
            objectLocal.set(stack);
        }

        stack.push(object);
        T result = null;
        try{
            if(thing != null) {
                result = thing.doAction("create", actionContext);
            }

            init(object, false, actionContext, annotations);
        }finally {
            stack.pop();
        }

        return result;
    }

    /**
     * 依次执行things的create(actionContext)方法，执行后会遍历对象的注解为ActionField的字段，并从变量上下文中取值对字段赋值。
     */
    @SafeVarargs
    public static <T> T load(Object object, List<Thing> things, ActionContext actionContext, Class<? extends Annotation> ... annotations){
        Stack<Object> stack = objectLocal.get();
        if(stack == null) {
            stack = new Stack<>();
            objectLocal.set(stack);
        }

        stack.push(object);
        T result = null;
        try{
            for(Thing thing : things) {
                result = thing.doAction("create", actionContext);
            }

            init(object, false, actionContext, annotations);
        }finally {
            stack.pop();
        }

        return result;
    }

    @SafeVarargs
    public static void init(Object object, boolean doInitMethod, ActionContext actionContext, Class<? extends Annotation> ... annotations){
        //查找字段的注解
        java.util.List<Field> allFieldList = new ArrayList<>() ;
        Class<?> tempClass = object.getClass();
        while (tempClass != null) {//当父类为null的时候说明到达了最上层的父类(Object类).
            allFieldList.addAll(Arrays.asList(tempClass .getDeclaredFields()));
            tempClass = tempClass.getSuperclass(); //得到父类,然后赋给自己
        }

        for(Field field : allFieldList) {
            ActionField actionField = field.getAnnotation(ActionField.class);

            if(actionField != null) {
                String name = field.getName();
                String vname = actionField.name();
                if("".equals(vname)) {
                    vname = name;
                }

                setFieldValue(object, field, vname, actionContext);
            } else if(annotations != null){
                for(Class<? extends  Annotation>  annotationClass : annotations) {
                    Object annotationField = field.getAnnotation(annotationClass);
                    if (annotationField != null) {
                        String name = field.getName();

                        setFieldValue(object, field, name, actionContext);
                        break;
                    }
                }
            }
        }

        if(doInitMethod){
            init(object);
        }
    }

    public static void init(Object object){
        if(object == null){
            return;
        }

        try {
            Method method = object.getClass().getMethod("init");
            method.invoke(object);
        }catch (Exception e){
            logger.warning("Execute init() mtehod  exception, " + e);
        }

    }

    /**
     * 对对象含有指定注解的字段赋值，值从变量上下文中取字段同名的值。
     */
    public static void setFieldValues(Object object, Class<? extends Annotation> annotationClass, ActionContext actionContext){
        //查找字段的注解
        java.util.List<Field> allFieldList = new ArrayList<>() ;
        Class<?> tempClass = object.getClass();
        while (tempClass != null) {//当父类为null的时候说明到达了最上层的父类(Object类).
            allFieldList.addAll(Arrays.asList(tempClass .getDeclaredFields()));
            tempClass = tempClass.getSuperclass(); //得到父类,然后赋给自己
        }

        for(Field field : allFieldList) {
            Object annotationField = field.getAnnotation(annotationClass);
            if(annotationField != null) {
                String name = field.getName();

                setFieldValue(object, field, name, actionContext);
            }
        }
    }

    private static void setFieldValue(Object object, Field field, String valueName, ActionContext actionContext){
        if(valueName == null || "".equals(valueName)){
            valueName = field.getName();
        }

        boolean accessChanged = false;
        try {
            if(!field.isAccessible()) {
                field.setAccessible(true);
                accessChanged = true;
            }
            field.set(object, actionContext.get(valueName));
        } catch (IllegalAccessException e) {
            logger.warning("Set field value exception, field=" + field.getName() + ", valueName=" + valueName
                    + ", " + e);
        } finally {
            if(accessChanged) {
                field.setAccessible(false);
            }
        }
    }

    public static Object getObject(){
        Stack<Object> stack = objectLocal.get();
        if(stack != null && stack.size() > 0){
            return stack.peek();
        }

        return null;
    }

    /**
     * 基于继承了xworker.lang.ThingLoader的模型使用，一般用于在模型里加载到对象。
     *
     * @param thing 模型
     * @param actionContext 变量上下文
     *
     * @return 要加载到的对象，如果为null表示没有
     */
    public static Object getObjectForLoad(Thing thing, ActionContext actionContext) {
        Object objectForLoad = thing.doAction("getObjectForLoad", actionContext);
        if(objectForLoad != null){
            return objectForLoad;
        }

        Class<?> objectForLoadClass = thing.doAction("getObjectForLoadClass", actionContext);
        if(objectForLoadClass != null){
            try {
                return objectForLoadClass.getConstructor(new Class<?>[]{}).newInstance();
            }catch(Exception e){
                logger.warning("NewInstance error, thing=" + thing.getMetadata().getPath() + ", " + e);
            }
        }

        return null;
    }

    /**
     * xworker.lang.ThingLoader模型的create方法实现.
     */
    public static Object create(ActionContext actionContext){
        Thing self = actionContext.getObject("self");

        Object objectForLoad = getObjectForLoad(self, actionContext);
        Object result = null;
        if(objectForLoad != null){
            result = ThingLoader.load(objectForLoad, self.getChilds(), actionContext);

            if(self.getBoolean("init")){
                init(objectForLoad);
            }
        }else{
            for(Thing child : self.getChilds()){
                result = child.doAction("create", actionContext);
            }
        }

        actionContext.g().put(self.getMetadata().getName(), objectForLoad);

        return result;
    }
}
