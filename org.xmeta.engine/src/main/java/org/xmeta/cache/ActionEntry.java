/*
 * Copyright 2007-2008 The X-Meta.org.
 * 
 * Licensed to the X-Meta under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The X-Meta licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
