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
package org.xmeta.util;

/**
 * 重构监听器。<p/>
 * 
 * 重构的流程是：<br>
 *     1. 计算要移动和更新的事物总数，事物总数也是操作数量的总数。<br/>
 *     2. 进行拷贝操作，把原事物拷贝到目标事物。<br/>
 *     3. 更新所有的事物对原目标的引用到目标事物，操作可能是更新或未更新。<br/>
 *     4. 删除原事物。<br/>
 * 
 * @author zhangyuxiang
 *
 */
public interface RefactorListener {
	/**
	 * 开始重构。
	 * 
	 * @param count 可能需要改动的事物总数，包括重构的可以引用路径可能需要变动的。
	 */
	public void onStart(int count);
	
	/**
	 * 拷贝原事物到目标事物。
	 * 
	 * @param sourcePath
	 * @param targetPath
	 */
	public void onCopy(String sourcePath, String targetPath);
	
	/**
	 * 删除原事物。
	 * 
	 * @param sourcePaht
	 */
	public void onDelete(String sourcePath);
	
	/**
	 * 事物更新了，引用变更后事物更新。
	 * 		
	 * @param path
	 */
	public void onUpdated(String path);
	
	/**
	 * 路径对应的事物没有更改。
	 * 
	 * @param path
	 */
	public void notMidify(String path);	
	
	/**
	 * 重构结束了。
	 * 
	 */
	public void finish();
}