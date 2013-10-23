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