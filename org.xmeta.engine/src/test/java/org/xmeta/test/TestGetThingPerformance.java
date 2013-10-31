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

import org.xmeta.Thing;
import org.xmeta.World;

public class TestGetThingPerformance {
	public static void main(String args[]){
		try{
			World world = World.getInstance();
			world.init("D:\\xmeta\\xmeta1.2\\alpha\\");
			int count = 100000;
			Thing thing = world.getThing("xmeta:core:test.things.lang.actions.TestPerformance:/@actions/@java");
			long start = System.currentTimeMillis();
	        for(int i=0; i<count; i++){
	            thing = world.getThing("xmeta:core:test.things.lang.actions.TestPerformance:/@actions/@java");
	        }
	        System.out.println("get Thing time : " + (System.currentTimeMillis() - start)); 
	        
	        System.exit(0);	        
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}