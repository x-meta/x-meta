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