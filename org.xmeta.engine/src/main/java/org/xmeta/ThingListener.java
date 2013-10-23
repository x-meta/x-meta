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
package org.xmeta;

/**
 * 事物事件的监听者接口，主要用来监听一个事物的改动事件和删除事件。<p/>
 * 
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public interface ThingListener {
	/**
	 * 事物改变后触发的事件。
	 * 
	 * @param thing 发生改变的事物
	 */
	public void changed(Thing thing);
	
	/**
	 * 事物已删除后的触发的事件，此方法是调用事物自身的remove()方法后才触发的事件。
	 * 
	 * @param thing 事物被删除后触发的事件
	 */
	public void removed(Thing thing);
}