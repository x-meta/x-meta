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
	 * @param src
	 * @param dest
	 * @return
	 */
	public File onOverWrite(File src, File dest);
	
	/**
	 * 当文件拷贝成功后触发的事件。
	 * 
	 * @param src
	 * @param dest
	 */
	public void onCopyed(File src, File dest);
	
	/**
	 * 当不覆盖一个已存在文件后，会调用此方法决定是否
	 * 取消整个拷贝任务。
	 * 
	 * @return
	 */
	public boolean cancel();
}