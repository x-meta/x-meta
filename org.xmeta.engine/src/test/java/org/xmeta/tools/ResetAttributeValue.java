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
package org.xmeta.tools;

import java.io.FileInputStream;

import org.xmeta.Thing;
import org.xmeta.World;

public class ResetAttributeValue {
	public static void main(String args[]){
		try{
			//World.getInstance().init("E:\\work\\xmeta_alpha\\");
			World.getInstance().init("D:\\xmeta\\xmeta1.2\\alpha\\");
			
			Thing thing = World.getInstance().getThing("xmeta.ide.worldExplorer.swt.dataExplorerParts.ThingEditor/@rightComposite/@contentComposite/@menuBarComposite/@coolBar/@descButtonCoolItem/@descButtonToolBar/@descComboItem/@control/@160/@895/@descriptSelection/@GroovyAction");
			FileInputStream fin = new FileInputStream("code.txt");
			byte[] bytes = new byte[fin.available()];
			fin.read(bytes);
			fin.close();
			thing.set("code", new String(bytes));
			thing.save();
			
			System.exit(0);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
		
}