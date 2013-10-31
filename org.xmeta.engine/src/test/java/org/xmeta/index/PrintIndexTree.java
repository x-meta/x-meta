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
package org.xmeta.index;

import org.xmeta.Index;
import org.xmeta.World;

public class PrintIndexTree {
	public static void printIndex(Index index, String parent){
		String myPath = null;
		if(parent != null){
			myPath = parent + "->" + index.getLabel();
		}else{
			myPath = index.getName();
		}
		System.out.println(myPath);
		
		for(Index child : index.getChilds()){
			printIndex(child, myPath);
		}
	}
	
	public static void main(String args[]){
		try{
			World world = World.getInstance();			
			world.init("E:\\git\\xworker\\xworker\\");
			
			WorldIndex index = new WorldIndex();
			printIndex(index, null);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}