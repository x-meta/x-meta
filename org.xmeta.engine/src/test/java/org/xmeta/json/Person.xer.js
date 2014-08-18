{
    "thing":{
    	"name": "Person",
    	"actions":{
    		"JavaAction":{
    			"name": "eat",
    			"useOuterJava": true,
    			"outerClassName": "org.xmeta.json.Person",
    			"methodName":"run"
    		}
    	},
    	"thing":{
    		"name": "Child",
    		"extends":"org.xmeta.json.Person"
    	},
    	"attribute":{
    		"name":"name"    		
    	},
    	"attribute":{
    		"name":"age"
    	}
    }
}