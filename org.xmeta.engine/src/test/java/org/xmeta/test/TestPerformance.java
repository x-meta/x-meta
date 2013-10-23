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