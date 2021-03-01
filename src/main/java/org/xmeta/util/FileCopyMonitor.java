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

import java.io.File;

/**
 * 文件拷贝监控者。
 * 
 * @author zhangyuxiang
 *
 */
public interface FileCopyMonitor {
	/**
	 * 如果目标文件存在，返回要被覆盖的文件。
	 * 如果要覆盖目标文件，可直接返回目标文件，否则可以返回
	 * 一个可以覆盖的文件，如果取消返回null。
	 * 
	 * @param src 源
	 * @param dest 目标
	 * @return 新文件
	 */
	public File onOverWrite(File src, File dest);
	
	/**
	 * 当文件拷贝成功后触发的事件。
	 * 
	 * @param src 源文件
	 * @param dest 目标文件
	 */
	public void onCopyed(File src, File dest);
	
	/**
	 * 当不覆盖一个已存在文件后，会调用此方法决定是否
	 * 取消整个拷贝任务。
	 * 
	 * @return 是否取消
	 */
	public boolean cancel();
}