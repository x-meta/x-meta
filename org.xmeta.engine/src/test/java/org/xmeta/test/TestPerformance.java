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
