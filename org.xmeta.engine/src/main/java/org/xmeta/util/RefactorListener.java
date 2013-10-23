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