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