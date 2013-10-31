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
package org.xmeta.test;

import org.xmeta.Action;
import org.xmeta.ActionContext;
import org.xmeta.Thing;
import org.xmeta.World;

public class TestPerformance {
	public static void main(String args[]){
		try{
			World world = World.getInstance();
			//world.init("E:\\work\\workspace_xmeta\\org.xmeta.alpha\\data\\");
			world.init("D:\\xmeta\\xmeta1.2\\alpha\\");
			Action jaction = world.getAction("xmeta:core:test.things.lang.actions.TestPerformance:/@actions/@java");
			jaction.getMethod().invoke(jaction.actionClass, new Object[]{null});
			int count = 100000;
			long start = System.currentTimeMillis();
	        for(int i=0; i<count; i++){
	            jaction.getMethod().invoke(jaction.actionClass, new Object[]{null});
	        }
	        System.out.println("java method invoke time : " + (System.currentTimeMillis() - start));        
	        
	        start = System.currentTimeMillis();
	        for(int i=0; i<count; i++){
	        	world.getAction("xmeta:core:test.things.lang.actions.TestPerformance:/@actions/@java");
	        }
	        System.out.println("get thing time : " + (System.currentTimeMillis() - start) + ", count=" + count);   
	        
	        ActionContext context = new ActionContext();
	        start = System.currentTimeMillis();
	        for(int i=0; i<count; i++){
	            jaction.run(context, null, null, false);
	        }
	        System.out.println("action time : " + (System.currentTimeMillis() - start) + ", count=" + count);
	        
	        Thing test = world.getThing("xmeta:core:test.things.lang.actions.TestPerformance");
	        test.doAction("DoTestByJava", context);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}