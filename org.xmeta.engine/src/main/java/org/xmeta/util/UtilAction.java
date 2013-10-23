/*
    X-Meta Engine。
    Copyright (C) 2013  zhangyuxiang

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
    For alternative license options, contact the copyright holder.

    Emil zhangyuxiang@tom.com
 */
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
	    	Bindings binding = null;
	        String varScope = action.getString("varScope");
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
    }
}