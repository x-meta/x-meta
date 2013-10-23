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
package org.xmeta.thingManagers;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmeta.XMetaTimerManager;

/**
 * 移除已不用的瞬态事物。
 * 
 * @author zhangyuxiang
 *
 */
public class TransientFinalizer extends TimerTask{
	private static Logger log = LoggerFactory.getLogger(TransientFinalizer.class);	
	
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
						log.error("remove transient dead things", t);
					}
				}
			}
			
			for(WeakReference<TransientThingManager> managerR : forRemove){
				managers.remove(managerR);
			}
		}catch(Exception e){
			log.error("do transientFinalizer error", e);
		}
	}
}