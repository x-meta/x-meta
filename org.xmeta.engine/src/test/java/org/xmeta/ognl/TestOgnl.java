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