package org.xmeta.util;

import org.xmeta.ActionContext;
import org.xmeta.Thing;
import org.xmeta.ThingManager;
import org.xmeta.World;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 通常用于替换动作模型中的原有执行方式。可以用于给已经存在的动作模型打补丁。
 */
public class JavaActionFactory {
    final static Map<String, JavaAction> actions = new HashMap<>();

    public static void regist(String className, String methodName, JavaAction javaAction){
        String key = className + "_" + methodName;
        actions.put(key, javaAction);
    }

    public static JavaAction getJavaAction(String className, String methodName){
        String key = className + "_" + methodName;
        return actions.get(key);
    }

    public static void registAction(String actionPath, JavaAction javaAction){
        actions.put(actionPath, javaAction);
    }

    public static JavaAction getActionJavaAction(String actionPath){
        return actions.get(actionPath);
    }

    /**
     * 遍历系统中的模型，如果模型时JavaAction，那么生成一个JavaAction的实现，并把它注册到JavaActionFactory。
     */
    public static String genereateRegistJavaCode() {
        StringBuilder sb = new StringBuilder();
        Map<String, String> context = new HashMap<>();
        for (ThingManager thingManager : World.getInstance().getThingManagers()) {
            Iterator<Thing> iter = thingManager.iterator(null, true);
            while (iter.hasNext()) {
                Thing thing = iter.next();
                if (thing != null) {
                    registJavaAction(thing, sb, context);
                }
            }
        }

        return sb.toString();
    }

    private static void registJavaAction(Thing thing, StringBuilder sb, Map<String, String> context){
        if("JavaAction".equals(thing.getThingName())){
            String outerClassName= thing.getStringBlankAsNull("outerClassName");
            String methodName = thing.getStringBlankAsNull("methodName");

            String key = outerClassName + "_" + methodName;
            if(outerClassName != null && methodName != null && JavaActionFactory.getJavaAction(outerClassName, methodName) == null && context.get(key) == null) {
                context.put(key, key);
                boolean returnVoid = false;
                try{
                    Class<?> cls = World.getInstance().getClassLoader().loadClass(outerClassName);
                    Method method = cls.getMethod(methodName, ActionContext.class);
                    returnVoid = method.getReturnType().equals(void.class);
                }catch(Throwable ignored){
                }

                if (returnVoid) {
                    sb.append("        JavaActionFactory.regist(\"").append(outerClassName).append("\", \"").append(methodName);
                    sb.append("\", actionContext -> {\n");
                    sb.append("            ").append(outerClassName).append(".").append(methodName).append("(actionContext);\n            return null;\n        });\n");
                }else{
                    sb.append("        JavaActionFactory.regist(\"").append(outerClassName).append("\", \"").append(methodName);
                    sb.append("\", ").append(outerClassName).append("::").append(methodName).append(");\n");
                }
            }
        }else{
            for(Thing child : thing.getChilds()){
                registJavaAction(child, sb, context);
            }
        }
    }

    public static void main(String[] args){
        try {
            Method method = JavaActionFactory.class.getMethod("registAction", String.class, JavaAction.class);
            System.out.println(method.getReturnType());
            System.out.println(method.getReturnType().equals(void.class));
            System.out.println(method.getReturnType() == void.class);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}