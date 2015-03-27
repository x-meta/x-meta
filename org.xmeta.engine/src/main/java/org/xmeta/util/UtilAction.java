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
            Bindings bindings = getVarScope(action, actionContext);
            if(bindings != null){
                bindings.put(varName, var);
            }
        }
	}
	
	/**
	 * 获取变量范围。
	 *  
	 * @param self
	 * @param actionContext
	 * @return
	 */
	public static Bindings getVarScope(Thing action, ActionContext actionContext){
		if(action == null){
			return actionContext.getScope();
		}else{		
			String varScope = action.getString("varScope");
			return getVarScope(varScope, actionContext);
		}
    }
	
	/**
	 * 通过VarScope的字符串返回相应的Scope，如Gloabl, Local等。
	 * 
	 * @param varScope
	 * @param actionContext
	 * @return
	 */
	public static Bindings getVarScope(String varScope, ActionContext actionContext){
		Bindings binding = null;
        
        if(varScope == null || "".equals(varScope)){
        	binding = actionContext.getScope();
        }else  if("Global".equals(varScope)){
            binding = actionContext.getScope(0);    
        }else if("Local".equals(varScope)){
            binding = actionContext.getScope();
        }else{    
            try{
                int scopeIndex = Integer.parseInt(varScope);
                if(scopeIndex >= 0){
                    binding = actionContext.getScope(scopeIndex);
                }else{
                	binding = actionContext.getScope(actionContext.getScopesSize() + scopeIndex);
                }
            }catch(Exception e){
                binding = actionContext.getScope(varScope);
            }
        }
        
        return binding;
	}
	
	/**
	 * 返回是否是打印调试日志。
	 * 
	 * @param actionThing
	 * @param actionContext
	 * @return
	 */
	public static boolean getDebugLog(Thing actionThing, ActionContext actionContext){
		return actionThing.getBoolean("debugLog");
	}
}