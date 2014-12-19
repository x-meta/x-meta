package org.xmeta.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xmeta.Thing;
import org.xmeta.World;

public class XMetaDemo {
	static List<Thing> thingList = new ArrayList<Thing>();
	
	public static void echo(String message){
		System.out.println(message);
	}
	
	public static void createThing(String name){
		
		World world = World.getInstance();
		if(world.getThing(name) != null){
			System.out.println("创建新事物失败，事物" + name + "已经存在。");
			return;
		}
		Thing thing = new Thing(name, name);
		thing.getMetadata().setPath(name);
		World.getInstance().getTransientThingManager().save(thing);
		
		thingList.add(thing);
	}
	
	public static void setAttribute(String thingName, String attributeName, String value){
		Thing thing = World.getInstance().getThing(thingName);
		if(thing == null){
			System.out.println("事物不存在, name=" + thingName);
		}else{
			thing.set(attributeName, value);
		}
	}
	
	public static void list(){
		for(Thing thing : thingList){
			System.out.println(thing.getMetadata().getName());
		}
		
		if(thingList.size() == 0){
			System.out.println("当前还没有事物，可以通过create命令创建事物");
		}
	}
	
	public static void show(String thingName){
		Thing thing = World.getInstance().getThing(thingName);
		if(thing == null){
			System.out.println("事物不存在, name=" + thingName);
		}else{
			String descStr = null;
			for(Thing desc : thing.getAllDescriptors()){
				if(descStr == null){
					descStr = desc.getMetadata().getName();
				}else{
					descStr = descStr + "," + desc.getMetadata().getName();
				}
			}
			System.out.println("事物的名字是" + thing.getMetadata().getName() + "，它是一个" + descStr);
			System.out.println("它的属性有：");
			Map<String, Object> attributes = thing.getAttributes();
			for(String key : attributes.keySet()){
				System.out.println("    " + key + "=" + attributes.get(key));
			}
			
			List<Thing> actions = thing.getActionsThings();
			System.out.println("它的行为有：");
			for(Thing ac : actions){
				System.out.println("    " + ac.getMetadata().getName());
			}
			if(actions.size() == 0){
				System.out.println("    还没有行为");
			}
		}
	}
	
	public static void setAction(String thingName, String actionName, String code){
		Thing thing = World.getInstance().getThing(thingName);
		if(thing == null){
			System.out.println("事物不存在, name=" + thingName);
		}else{
			Thing actions = thing.getThing("actions@0");
			if(actions == null){
				actions = new Thing("actions");
				thing.addChild(actions);
			}
			
			code = "import org.xmeta.Thing;\n" +
					"import org.xmeta.ActionContext;\n" +
					"\n" +
					"public class " + actionName + "{\n" +
							"    public static void run(ActionContext actionContext){\n" + code + "\n" +
							"    }\n" +
							"}";
			boolean have = false;
			for(Thing ac : actions.getChilds()){
				if(ac.getMetadata().getName().equals(actionName)){
					ac.put("code", code);
					have = true;
					break;
				}
			}
			
			if(!have){
				Thing ac = new Thing("JavaAction");
				ac.put("name", actionName);
				ac.put("className", actionName);
				ac.put("methodName", "run");
				ac.put("useOuterJava", "false");
				ac.put("code", code);
				actions.addChild(ac);
			}
		}
	}
	
	public static void inputActionCode(String thingName, String actionName, BufferedReader br) throws IOException{
		System.out.println("请输入动作的Java代码，如果一行是字符串'end'表示结束：");
		System.out.print(">>>");
		String line = null;
		String code = null;
		while((line = br.readLine()) != null){
			line = line.trim();
			if("end".equals(line)){
				break;
			}
			
			if(code == null){
				code = line;
			}else{
				code = code + "\n" + line; 
			}
			System.out.print(">>>");
		}
		
		setAction(thingName, actionName, code);
	}
	
	public static void doAction(String thingName, String actionName){
		Thing thing = World.getInstance().getThing(thingName);
		if(thing == null){
			System.out.println("事物不存在, name=" + thingName);
		}else{
			try{
				thing.doAction(actionName);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public static void printInput(){
		System.out.print("[xmeta]# ");
	}
	
	public static void showHelp(){
		System.out.println("X-Meta引擎演示命令列表：");
		System.out.println("create <thingName>                                  创建一个事物");
		System.out.println("set <thingName> <attributeName> <attributeValue>    设置一个事物的属性");
		System.out.println("setac <thingName> <actionName>                      设置事物的一个行为");
		System.out.println("setdesc <thingName> <descriptorName>                设置一个事物的描述者事物");
		System.out.println("do <thingName> <actionName>                         执行一个事物的行为");
		System.out.println("show <thingName>                                    打印一个事物的信息");
		System.out.println("list                                                列出所有的事物");
		//System.out.println("echo <message>                                      屏幕上一个信息");
		System.out.println("help                                                显示本帮助信息");		
		System.out.println("exit                                                退出");		
	}
	
	public static void mainLoop() throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String line = null;
		showHelp();
		printInput();
		
		while((line = br.readLine()) != null){
			line = line.trim();
			if(!"".equals(line)){
				String[] ins = line.split("[ ]");
				if(line.startsWith("create")){
					if(ins.length < 2 || "".equals(ins[1].trim())){
						System.out.println("请输入事物名");
					}else{
						createThing(ins[1].trim());
					}
				}else if(line.startsWith("setac")){
					if(ins.length < 3){
						System.out.println("设置事物行为的格式是：setac <thingName> <actionName>");
					}else{
						inputActionCode(ins[1].trim(), ins[2].trim(), br);
					}
				}else if(line.startsWith("setdesc")){
					if(ins.length < 3){
						System.out.println("设置事物的描述者的格式是：setdesc <thingName> <descriptorName>");
					}else{
						setAttribute(ins[1].trim(), "descriptors", ins[2].trim());
					}
				}else if(line.startsWith("set")){
					if(ins.length < 4){
						System.out.println("设置事物属性的格式是：set <thingName> <attributeName> <attributeValue>");
					}else{
						setAttribute(ins[1].trim(), ins[2].trim(), ins[3].trim());
					}
				}else if(line.startsWith("do")){
					if(ins.length < 3){
						System.out.println("执行事物行为的格式是：do <thingName> <actionName>");
					}else{
						doAction(ins[1].trim(), ins[2].trim());
					}
				}else if(line.startsWith("show")){
					if(ins.length < 2){
						System.out.println("打印事物的格式是：show <thingName> ");
					}else{
						show(ins[1].trim());
					}
				}else if(line.startsWith("list")){
					list();
				}else if(line.startsWith("echo")){
					if(ins.length < 2){
					}else{
						echo(ins[1].trim());
					}
				}else if(line.startsWith("help")){
					showHelp();
				}else if(line.equals("exit")){
					System.exit(0);
				}
			}
			//System.out.println("------------------------------------------------------");
			printInput();
		}
	}
	
	public static void main(String args[]){
		//初始化World，World是获取事物的容器
		World world = World.getInstance();
		world.init(".");
		
		try{
			mainLoop();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
