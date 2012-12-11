package org.xmeta.util;

import org.xmeta.Action;
import org.xmeta.ActionContext;
import org.xmeta.Bindings;
import org.xmeta.Thing;
import org.xmeta.World;

public class UtilAction {
	/**
	 * 获取一个动作的代码文件名。
	 * 
	 * @param actionThing
	 * @param ext 代码文件的后缀
	 * @return
	 */
	public static String getActionCodeFilePath(Thing actionThing, String ext){
		String className = "";
		
		Thing parent = actionThing.getParent();			
		Thing rootParent = actionThing.getRoot();
		if(parent == null){
			parent = actionThing;
		}
		
		if(rootParent != null){
			className = className + ".p" + rootParent.getMetadata().getPath().hashCode();
		}
		
		if(parent != null && parent != rootParent){
			className = className + ".p" + parent.getMetadata().getPath().hashCode();
		}
		
		String cName = actionThing.getString("className");
		if(cName == null || "".equals(cName)){
			className = className + "." + actionThing.getMetadata().getName();
		}else{
			className = className + "." + cName;
		}
		className = Action.getClassName(className);

		String fileName = className.replace('.', '/');
		//fileName += ".java";
					
		return World.getInstance().getPath() + "/actionSources/" + fileName + "." + ext;
	}
	
	/**
	 * 把变量赋值到动作定义的变量范围中。
	 * 
	 * @param action
	 * @param varName
	 * @param var
	 * @param actionContext
	 */
	public static void putVarByActioScope(Thing action, String varName, Object var, ActionContext actionContext){
		if(varName != null && !"".equals(varName)){
            Bindings bindings = (Bindings) action.doAction("getVarScope", actionContext);
            if(bindings != null){
                bindings.put(varName, var);
            }
        }
	}
}
