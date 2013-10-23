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
 * 元事物，元事物是任意事物的结构的结构，元事物也称为元结构（在X-Meta中结构用描述代替）。<p/>
 * 
 * 元事物用XML的方法表示就是：<br/>
 * <pre>
 * &lt;thing name="thing"&gt;
 *     &lt;attribute name="name"/&gt;
 *     &lt;attribute name="extends"/&gt;
 *     &lt;thing name="attribute"&gt;
 *         &lt;attribute name="name"/&gt;
 *     &lt;/thing>
 *     &lt;thing name="thing" extends="_root"/&gt;
 * &lt;/thing&gt;
 * </pre>
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public class MetaThing extends Thing{
	public MetaThing(){
		super();
		
		init();
	}
	
	/**
	 * 实际初始化一个:
	 * <thing name="thing" descriptors="MetaThing">
	 *     <attribute name="name"/>
	 *     <attribute name="descriptors"/>
	 *     <attribute name="extends"/>
	 *     <thing name="attribute">
	 *         <attribute name="name"/>
	 *     </thing>
	 *     <thing name="thing" extends="_root"/>
	 *     <thing name="actions">
	 *         <thing name="JavaAction">
	 *             <attribute name="name"/>
	 *             <attribute name="useOuterJava"/>
	 *             <attribute name="outerClassName"/>
	 *             <attribute name="methodName"/>
	 *             <attribute name="className"/>
	 *             <attribute name="code"/>
	 *             <attribute name="isSynchronized"/>
	 *             <attribute name="useOtherAction"/>
	 *             <attribute name="otherActionPath""/>
	 *     </thing>
	 * </thing>
	 */
	private void init(){
		this.beginModify();
		
        //<thing name="thing"/>
		attributes.put(Thing.NAME, Thing.THING);
		attributes.put(Thing.DESCRIPTORS, "MetaThing");
		
		//<attribute name="name"/>
		addChild(createAttribute("name"));
		//<attribute name="descriptors"/>
		addChild(createAttribute("descriptors"));
		//<attribute name="extends"/>
		addChild(createAttribute("extends"));
		
		//<thing name="attribute">
		//    <attribute name="name"/>
		//</thing>
		Thing attribute = createThing("attribute");
		attribute.addChild(createAttribute("name"));
		addChild(attribute);
		
		//<thing name="thing" extends="_root"/>子节点
		Thing childThing = createThing("thing");
		childThing.getAttributes().put("extends", "_root");
		addChild(childThing);
		
		//<thing name="actions"> 原生动作JavaAction定义
		//    <thing name="JavaAction"/>
		//             <attribute name="name"/>
		//             <attribute name="useOuterJava"/>
		//             <attribute name="outerClassName"/>
		//             <attribute name="methodName"/>
		//             <attribute name="className"/>
		//             <attribute name="code"/>
		//             <attribute name="isSynchronized"/>
		//             <attribute name="useOtherAction"/>
		//             <attribute name="otherActionPath""/>
		//</thing>
		Thing actions = createThing("actions");
		Thing javaAction = createThing("JavaAction");
		javaAction.addChild(createAttribute("name"));
		javaAction.addChild(createAttribute("useOuterJava"));
		javaAction.addChild(createAttribute("outerClassName"));
		javaAction.addChild(createAttribute("methodName"));
		javaAction.addChild(createAttribute("className"));
		javaAction.addChild(createAttribute("code"));
		javaAction.addChild(createAttribute("isSynchronized"));
		javaAction.addChild(createAttribute("useOtherAction"));
		javaAction.addChild(createAttribute("otherActionPath"));
		actions.addChild(javaAction);
		addChild(actions);
		
		this.endModify(true);
	}
	
	public Thing createThing(String name){
		return new Thing(name, null, "MetaThing", false);
	}
	
	public Thing createAttribute(String name){
		return new Thing(name, null, "MetaThing/@attribute", false);
	}
}