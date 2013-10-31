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


public class TestString {	
	public static String[] split(String str, char ch){
		char[] chs = str.toCharArray();
		String[] strs = new String[10];
		int strIndex = 0;
		int index1 = 0;
		int index2 = 0;
		while(index2 < chs.length){
			if(chs[index2] == ch){				
				strs[strIndex] = new String(chs, index1, index2 - index1);
				index1 = index2+1;
				strIndex++;
				
				if(strIndex >= strs.length){
					String[] nstrs = new String[strs.length + 10];
					System.arraycopy(strs, 0, nstrs, 0, strs.length);
					strs = nstrs;
				}
			}
			
			index2++;
		}
		if(index1 <= index2){
			strs[strIndex] = new String(chs, index1, index2 - index1);
			strIndex++;
			
			if(strIndex >= strs.length){
				String[] nstrs = new String[strs.length + 10];
				System.arraycopy(strs, 0, nstrs, 0, strs.length);
				strs = nstrs;
			}
		}
		String fstrs[] = new String[strIndex];
		System.arraycopy(strs, 0, fstrs, 0, strIndex);
		return fstrs;
	}
	
	public static void main(String args[]){
		try{
			int count = 100000;
			long start = System.currentTimeMillis();
	        for(int i=0; i<count; i++){
	            //"d:d:s:d:d:sd:sds:sdds:sdsd:sdds:sdds:sds:sds:sds:ddss:sds:sds:".split("[:]");
	        	split("d:d:s:d:d:sd:sds:sdds:sdsd:sdds:sdds:sds:sds:sds:ddss:sds:sds:", ':');
	        }
	        System.out.println("split time : " + (System.currentTimeMillis() - start));
	        
	        String[] strs = split(":abcd", ':');
	        for(String str : strs){
	        	System.out.println( "'" + str + "'");
	        }
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}