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
package org.xmeta.thingManagers;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import org.xmeta.XMetaTimerManager;

/**
 * 移除已不用的瞬态事物。
 * 
 * @author zhangyuxiang
 *
 */
public class TransientFinalizer extends TimerTask{
	//private static Logger log = LoggerFactory.getLogger(TransientFinalizer.class);	
	
	List<WeakReference<TransientThingManager>> managers = new CopyOnWriteArrayList<WeakReference<TransientThingManager>>();
	
	public TransientFinalizer(){
		XMetaTimerManager.schedule(this, 4000, 2000);
	}
	
	public void addTransientManager(TransientThingManager manager){
		managers.add(new WeakReference<TransientThingManager>(manager));
	}
	
	public void run(){
		try{
			List <WeakReference<TransientThingManager>> forRemove = new CopyOnWriteArrayList<WeakReference<TransientThingManager>>();
			
			for(WeakReference<TransientThingManager> managerR : managers){
				if(managerR.get() == null){
					forRemove.add(managerR);
				}else{
					TransientThingManager manager = managerR.get();
					try{
						manager.removeDeadThings();
					}catch(Throwable t){
						//log.error("remove transient dead things", t);
					}
				}
			}
			
			for(WeakReference<TransientThingManager> managerR : forRemove){
				managers.remove(managerR);
			}
		}catch(Exception e){
			//log.error("do transientFinalizer error", e);
		}
	}
}