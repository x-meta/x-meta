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

import org.xmeta.World;
import org.xmeta.util.RefactorListener;
import org.xmeta.util.RefactorUtil;

public class TestRefactory {
	public static void main(String args[]){
		try{
			World.getInstance().init("D:\\xmeta\\xmeta1.2\\alpha\\");
			String sourcePath = "test:test:temp.test";
			String targetPath = "test:test:test";
			
			RefactorUtil.getInstance().refactor(sourcePath, targetPath, new RefactorListener(){
				@Override
				public void onCopy(String sourcePath, String targetPath) {
					System.out.println("Copy " + sourcePath + " to " + targetPath);
				}

				@Override
				public void onDelete(String sourcePath) {
					System.out.println("Delete " + sourcePath);
				}

				@Override
				public void notMidify(String path) {
					System.out.println("NotModify " + path);
				}

				@Override
				public void onStart(int count) {
					System.out.println("Start...");
				}

				@Override
				public void onUpdated(String path) {
					System.out.println("Update " + path);
				}

				@Override
				public void finish() {
					System.out.println("finished");
					System.exit(0);
				}
				
			});
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}