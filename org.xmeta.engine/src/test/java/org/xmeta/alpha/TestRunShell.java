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
package org.xmeta.alpha;

import org.xmeta.Thing;
import org.xmeta.World;

public class TestRunShell {
	public static void main(String[] args){
		try{
			//World.getInstance().init("E:\\work\\xworker\\");
			World.getInstance().init("D:\\xworker-1.3.3\\xworker\\");
			//World.getInstance().init("E:\\work\\dist\\xworker-1.3.3\\xworker\\");
			
			//使用事物
			Thing worldExplorer = World.getInstance().getThing("xworker.ide.worldExplorer.swt.SimpleExplorerRunner");			
			worldExplorer.doAction("run");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
 