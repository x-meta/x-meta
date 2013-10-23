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
 * 事物管理者的事件监听者接口。 <p/>
 * 
 * 通过事物的方法getMetadata().getThingManager()可以获得触发事件的事物管理者。
 *
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public interface ThingManagerListener {
	/**
	 * 当一个事物被载入时触发此事件。
	 * 
	 * @param thing 载入的事物
	 */
	public void loaded(ThingManager thingManager, Thing thing);
	
	/**
	 * 当一个事物被保存时触发此事件。
	 * 
	 * @param thing 被保存的事物
	 */
	public void saved(ThingManager thingManager, Thing thing);
	
	/**
	 * 当一个事物被删除是触发此事件。
	 * 
	 * @param thing 被删除的事物
	 */
	public void removed(ThingManager thingManager, Thing thing);
}