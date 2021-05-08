function LIST(args) {
	var String = Java.type('java.lang.String');
	var YamlConfiguration = Java.type('org.bukkit.configuration.file.YamlConfiguration');
	var File = Java.type('java.io.File');
	var ArrayList = Java.type('java.util.ArrayList')
	var folderpath = new File('./plugins/TriggerReactor/ListExecutor');
	var list = Java.type('java.util.List');
	if(!folderpath.exists()){
		print('creating folder LISTexecutor');
		folderpath.mkdir()
	}else{
	}
	
	if(args.length == 0){
			player.sendMessage('Invalid parameters type #LIST "help"');
	}else{
		if(typeof args[0] == 'object'){
			player.sendMessage('this type is object!!!!! where is "" ?');
			return null;
		}else{
		}
		
		if(args[0] == "help"){
			player.sendMessage('#LIST "add" "<filename>" "value to add"');
			player.sendMessage('#LIST "remove" "<filename>" <"value to remove" or line>');
			player.sendMessage('#LIST "get" "<filename>" line');
			player.sendMessage('#LIST "insert" "<filename>" "value to insert" line');
			player.sendMessage('#LIST "contains" "<filename>" "value to search"');
			player.sendMessage('#LIST "set" "<filename>" "value to set" line');
			player.sendMessage('#LIST "size" "<filename>"');
			player.sendMessage('#LIST "indexof" "<filename>" value to search');
			player.sendMessage('#LIST "lineof" "<filename>" value to search');
			player.sendMessage('#LIST "direct" "<filename>" "list.***"');
			return null;
		}else{
		}
		
		if(args[0] == "add"){
			if(args.length < 3){
				player.sendMessage('Invalid parameters #LIST "<filename>" "value to add"');
				return null;
			}else{
				var userfile = new File('./plugins/TriggerReactor/ListExecutor/'+args[1]+'.yml');
				var userYml = YamlConfiguration.loadConfiguration(userfile);
				if(userfile.exists()){
					var list = userYml.get('list');
					if(!list.contains(args[2])){
						list = list.add(args[2]);
						userYml.save(userfile);
						list = userYml.get('list');
						player.sendMessage('add success!');
						player.sendMessage(list);
					}else{
						player.sendMessage('It already exists! Nothing Change');
						player.sendMessage(list);
						return null;
					}
				}else{
					userYml.set('list', new ArrayList());
					userYml.save(userfile);
					print('created new list file at '+userfile);
					player.sendMessage("I created a new file because I couldn't find it.");
					var list = userYml.get('list');
					list = list.add(args[2]);
					userYml.save(userfile);
					list = userYml.get('list');
					player.sendMessage('add success!');
					player.sendMessage(list);
				}
			}
		}else{
		}
		
		if(args[0] == "remove"){
			if(args.length < 3){
				player.sendMessage('Invalid parameters #LIST "remove" "<filename>" <"value to remove" or line>');
			}else{
				var userfile = new File('./plugins/TriggerReactor/ListExecutor/'+args[1]+'.yml');
				var userYml = YamlConfiguration.loadConfiguration(userfile);
				if(userfile.exists()){
					if(typeof args[2] == 'number'){
						var list = userYml.get('list');
						var size = list.size();
						if(size < args[2]){
							player.sendMessage('Input value is greater than maximum! maximum is '+size);
							return null;
						}else{
							list = list.remove(args[2]-1);
							userYml.save(userfile);
							list = userYml.get('list');
							player.sendMessage('remove success!');
							player.sendMessage(list);
						}
					}else{
					}
					
					if(typeof args[2] == 'string'){
						var list = userYml.get('list');
						var size = list.size();
						if(!list.contains(args[2])){
							player.sendMessage('Input value does not exist in the list');
							return null;
						}else{
							var num = list.indexOf(args[2]);
							list = list.remove(num);
							userYml.save(userfile);
							list = userYml.get('list');
							player.sendMessage('remove success!');
							player.sendMessage(list);
						}
					}else{
					}
				}else{
					player.sendMessage('file is not exists!!');
					return null;
				}
				
			}
		}else{
		}
		
		if(args[0] == "get"){
			if(args.length < 3){
				player.sendMessage('Invalid parameters #LIST "get" "<filename>" line');
			}else{
				var userfile = new File('./plugins/TriggerReactor/ListExecutor/'+args[1]+'.yml');
				var userYml = YamlConfiguration.loadConfiguration(userfile);
				if(userfile.exists()){
					if(!typeof args[2] == 'number'){
						player.sendMessage('Lines must only be numeric');
						return null;
					}else{
						var list = userYml.get('list');
						var size = list.size();
						if(size < args[2]){
							player.sendMessage('Input value is greater than maximum! maximum is '+size);
						}else{
						var el = list.get(args[2]-1);
						player.sendMessage(el)
						return el;
						}
					}
				}else{
				}
			}
		}else{
		}
		
		if(args[0] == 'insert'){
			if(args.length < 4){
				player.sendMessage('Invalid parameters #LIST "insert" "<filename>" "value to insert" line');
			}else{
				var userfile = new File('./plugins/TriggerReactor/ListExecutor/'+args[1]+'.yml');
				var userYml = YamlConfiguration.loadConfiguration(userfile);
				if(userfile.exists()){
					if(!typeof args[3] == 'number'){
						player.sendMessage('Lines must only be numeric!!');
						return null;
					}else{
						var list = userYml.get('list');
						var size = list.size();
						if(!list.contains(args[2])){
							if(size < args[3]){
								player.sendMessage('Input value is greater than maximum! maximum is '+size);
							}else{
								list = list.add(args[3] - 1, args[2]);
								userYml.save(userfile);
								list = userYml.get('list');
								player.sendMessage('insert success!');
								player.sendMessage(list);
							}
						}else{
							player.sendMessage('The value has already been added. Nothing Change');
							player.sendMessage(list);
							return null;
						}
					}
				}else{
					player.sendMessage('file is not exists!!');
					return null;
				}
			}
		}else{
		}
		
		if(args[0] == 'contains'){
			if(args.length < 3){
				player.sendMessage('Invalid parameters #LIST "contains" "<filename>" "value to search"')
			}else{
				var userfile = new File('./plugins/TriggerReactor/ListExecutor/'+args[1]+'.yml');
				var userYml = YamlConfiguration.loadConfiguration(userfile);
				if(userfile.exists()){
					var list = userYml.get('list');
					var res = list.contains(args[2]);
					player.sendMessage(res);
					return res;
				}else{
					player.sendMessage('file is not exists!');
					return null;
				}
			}
		}else{
		}
		
		if(args[0] == 'set'){
			if(args.length < 4){
				player.sendMessage('Invalid parameters #LIST "set" "<filename>" "value to set" line');
			}else{
				if(!typeof args[3] == 'number'){
						player.sendMessage('Lines must only be numeric!!');
						return null;
				}else{
				var userfile = new File('./plugins/TriggerReactor/ListExecutor/'+args[1]+'.yml');
				var userYml = YamlConfiguration.loadConfiguration(userfile);
					if(userfile.exists()){
						var list = userYml.get('list');
						var size = list.size();
						if(!list.contains(args[2])){
							list = list.set(args[3] - 1 ,args[2]);
							userYml.save(userfile);
							list = userYml.get('list');
							player.sendMessage('set success!');
							player.sendMessage(list);
						}else{
							player.sendMessage('The value has already been added. Nothing Change');
							player.sendMessage(list);
							return null;						
						}
					}else{
						player.sendMessage('file is not exists!');
						return null;
					}
				}
			}
		}else{
		}
		
		if(args[0] == 'size'){
			if(args.length < 2){
				player.sendMessage('Invalid parameters #LIST "size" "<filename>"');
				player.sendMessage('This returns in a string format.');
			}else{
				var userfile = new File('./plugins/TriggerReactor/ListExecutor/'+args[1]+'.yml');
				var userYml = YamlConfiguration.loadConfiguration(userfile);
				if(userfile.exists()){
					var list = userYml.get('list');
					var size = list.size();
					player.sendMessage(size);
					size = size.toString();
					return size;
				}else{
					player.sendMessage('file is not exists!');
					return null;
				}
			}
		}else{
		}
		
		if(args[0] == 'indexof'){
			if(args.length < 3){
				player.sendMessage('Invalid parameters #LIST "indexof" "<filename>" value to search');
				player.sendMessage('This returns in a string format.');
			}else{
				var userfile = new File('./plugins/TriggerReactor/ListExecutor/'+args[1]+'.yml');
				var userYml = YamlConfiguration.loadConfiguration(userfile);
				if(userfile.exists()){
					var list = userYml.get('list');
					if(list.contains(args[2])){
						var index = list.indexOf(args[2]);
						player.sendMessage(index);
						index = index.toString();
						return index;
					}else{
						player.sendMessage('Input value does not exist in the list');
						return null;
					}
				}else{
					player.sendMessage('file is not exists!');
					return null;
				}
			}
		}else{
		}
		
		if(args[0] == 'lineof'){
			if(args.length < 3){
				player.sendMessage('Invalid parameters #LIST "lineof" "<filename>" value to search');
				player.sendMessage('This returns in a string format.');
			}else{
				var userfile = new File('./plugins/TriggerReactor/ListExecutor/'+args[1]+'.yml');
				var userYml = YamlConfiguration.loadConfiguration(userfile);
				if(userfile.exists()){
					var list = userYml.get('list');
					if(list.contains(args[2])){
						var index = list.indexOf(args[2]);
						index = index + 1
						player.sendMessage(index);
						index = index.toString();
						return index;
					}else{
						player.sendMessage('Input value does not exist in the list');
						return null;
					}
				}else{
					player.sendMessage('file is not exists!');
					return null;
				}
			}
		}else{
		}
		
		if(args[0] == "direct"){
			if(args.length < 2){
				player.sendMessage('Invalid parameters #LIST "direct" "<filename>" "list.***"');
			}else{
				var userfile = new File('./plugins/TriggerReactor/ListExecutor/'+args[1]+'.yml');
				var userYml = YamlConfiguration.loadConfiguration(userfile);
				if(userfile.exists()){
					var list = userYml.get('list');
					if(args[2] == "run"){
						skript = args[3].replace("name",player.getName());
						eval(skript);
						userYml.save(userfile);
						player.sendMessage('done!');
					}else{
					}
					
					if(args[2] == "return"){
						res = list.args[3];
						userYml.save(userfile);
						player.sendMessage(res);
						return res;
					}else{
					}
				}else{
					player.sendMessage('file is not exists!');
				}
			}
		}else{
		}
						
	}
	
}
