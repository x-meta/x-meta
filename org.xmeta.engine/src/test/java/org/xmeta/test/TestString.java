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