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
package org.xmeta.cache;

import java.lang.ref.WeakReference;

import org.xmeta.Action;

public class ActionEntry {
	protected String path;
	protected WeakReference<Action> thingReference = null;
	protected long loastModified;
	
	public ActionEntry(String path, Action action){
		this.path = path;
		thingReference = new WeakReference<Action>(action);
		loastModified = action.getThing().getMetadata().getLastModified();
	}
	
	public String getPath(){
		return path;
	}
	
	public Action getAction(){
		Action action = thingReference.get();
		if(action != null){
			if(action.getThing().getMetadata().isRemoved() || loastModified != action.getThing().getMetadata().getLastModified()){
				return null;
			}else{
				return action;
			}
		}else{
			return null;
		}
	}
}