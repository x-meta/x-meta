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
package org.xmeta.util;

public class Semaphore {
	public synchronized void finished(){
        inuse = false;
        notify();
    }

    public synchronized void use() throws InterruptedException {
        if(inuse){
            try {
                wait();
            }
            catch(InterruptedException _ex){
            	throw _ex;                
            }
        }
        inuse = true;
    }

    public Semaphore(){
        inuse = false;
    }

    private boolean inuse;    
}
