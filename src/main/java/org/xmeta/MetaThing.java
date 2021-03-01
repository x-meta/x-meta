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
package org.xmeta;

/**
 * <p>元事物，元事物是任意事物的结构的结构，元事物也称为元结构（在X-Meta中结构用描述代替）。</p>
 * 
 * 元事物用XML的方法表示就是：
 * <pre>
 * &lt;thing name="thing"&gt;
 *     &lt;attribute name="name"/&gt;
 *     &lt;attribute name="extends"/&gt;
 *     &lt;thing name="attribute"&gt;
 *         &lt;attribute name="name"/&gt;
 *     &lt;/thing/&gt;
 *     &lt;thing name="thing" extends="_root"/&gt;
 * &lt;/thing&gt;
 * </pre>
 * @author <a href="mailto:zhangyuxiang@tom.com">zyx</a>
 *
 */
public class MetaThing extends Thing{
	public static MetaThing instance = new MetaThing();
	
	public MetaThing(){				
		super();
		
		//long start = System.currentTimeMillis();		
		init();
		//System.out.println("metathing init: " + (System.currentTimeMillis() - start));
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
		//long start = System.currentTimeMillis();
		this.beginModify();
		
		this.getMetadata().setPath("MetaThing");
		
        //<thing name="thing"/>
		attributes.put(Thing.NAME, Thing.THING);
		attributes.put(Thing.DESCRIPTORS, "MetaThing");
		
		//<attribute name="name"/>
		addChild(createAttribute("name"));
		//<attribute name="descriptors"/>
		addChild(createAttribute("descriptors"));
		//<attribute name="extends"/>
		addChild(createAttribute("extends"));
		//System.out.println("metathing addchild: " + (System.currentTimeMillis() - start));
		//<thing name="attribute">
		//    <attribute name="name"/>
		//</thing>
		Thing attribute = createThing("attribute");
		attribute.addChild(createAttribute("name"));
		addChild(attribute);
		
		//System.out.println("metathing addchild attribute : " + (System.currentTimeMillis() - start));
		
		//<thing name="thing" extends="_root"/>子节点
		Thing childThing = createThing("thing");
		childThing.getAttributes().put("extends", "_root");
		addChild(childThing);
		
		//System.out.println("metathing addchild thing : " + (System.currentTimeMillis() - start));
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
		actions.set("descriptors", "MetaThing/@actions");
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
		javaAction.set("descriptors", "MetaThing/@actions/@JavaAction");
		actions.addChild(javaAction);
		addChild(actions);
		//System.out.println("metathing addchild actions : " + (System.currentTimeMillis() - start));
		
		
		this.endModify(true);
	}
	
	public Thing createThing(String name){
		Thing thing = new Thing(name, null, null, false);
		thing.getAttributes().put("descriptors", "MetaThing");
		return thing;
	}
	
	public Thing createAttribute(String name){
		Thing thing =  new Thing(name, null, null, false);
		thing.getAttributes().put("descriptors", "MetaThing/@attribute");
		return thing;
	}
}