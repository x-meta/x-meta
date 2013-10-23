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
package org.xmeta.ui.session;

import java.util.HashMap;
import java.util.Map;

/**
 * 会话管理者，表示的是UI交互中的会话，还需进一步设计。
 * 
 * @author zyx
 *
 */
public class SessionManager {
	static Map<String, Session> sessions = new HashMap<String, Session>();
	
	public static Session getSession(String name) {
		Session session = sessions.get(name);
		if(session == null){
			session = new Session();
			sessions.put(name, session);
		}
		
		return session;
	}
	
	public static void remove(String name){
		sessions.remove(name);
	}
}