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
package org.xmeta.ognl;

import java.util.ArrayList;
import java.util.List;

import ognl.Ognl;

import org.xmeta.ActionContext;

public class TestOgnl {
	public static void main(String args[]){
		try{
			ActionContext ac = new ActionContext();
			ac.put("a", "a");
			ac.push();
			ac.push();
			ac.push().put("b", "b");
			
			int count = 100000;
			long start = System.currentTimeMillis();
			for(int i=0; i<count; i++){
				ac.get("a");
				//ac.get("b");
			}
			System.out.println("Action time is :" + (System.currentTimeMillis() - start));
			start = System.currentTimeMillis();
			for(int i=0; i<count; i++){
				Ognl.getValue("a", ac);
				//Ognl.getValue("b", ac);
			}
			System.out.println("Ognl time is :" + (System.currentTimeMillis() - start));
			
			List<String> s = new ArrayList<String>();
			s.add("a");
			s.add("b");
						
			String[] a = s.toArray(new String[s.size()]);
			for(int i=0; i<a.length; i++){
				System.out.println(a[i]);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}