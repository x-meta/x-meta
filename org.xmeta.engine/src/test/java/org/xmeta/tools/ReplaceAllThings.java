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

import org.xmeta.World;
import org.xmeta.util.RefactorListener;
import org.xmeta.util.RefactorUtil;

public class ReplaceAllThings {
	public static void main(String args[]){
		try{
			//World.getInstance().init("E:\\work\\xworker\\");
			//World.getInstance().init("D:\\xmeta\\xmeta1.2\\alpha\\");
			try{
				World.getInstance().init("E:\\git\\xworker\\xworker\\");
			}catch(Exception e){
				e.printStackTrace();
			}
			
			RefactorUtil.replaceAll("xworker.ide.util.TextTemplate", "xworker.lang.text.TextTemplate", new RefactorListener(){

				@Override
				public void onStart(int count) {
				}

				@Override
				public void onCopy(String sourcePath, String targetPath) {
				}

				@Override
				public void onDelete(String sourcePath) {
				}

				@Override
				public void onUpdated(String path) {
					System.out.println("updated: " + path);
				}

				@Override
				public void notMidify(String path) {
					//System.out.println("not modify: " + path);
				}

				@Override
				public void finish() {
					System.exit(0);
				}
				
			});
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}