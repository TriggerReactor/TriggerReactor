function read(args){
	var File = Java.type("java.io.File");
	var rd = Java.type("java.io.FileReader");
	var stream = Java.type("java.io.FileInputStream");
	var streamrd = Java.type("java.io.InputStreamReader");
	var buf = Java.type("java.io.BufferedReader");
	var ArrayList = Java.type("java.util.ArrayList");
	var List = Java.type("java.util.List");
	
	list = new ArrayList();
	file = new File(args[0]);
	if(!file.exists()){
		player.sendMessage("file is not exists!");
	}else{
		fr = new rd(file);
		dr = new buf(fr);
		line = dr.lines().count();
		file = new File(args[0]);
		fr = new stream(file);
		isr = new streamrd(fr, "UTF-8");
		dr = new buf(isr);
		for(i = 0; i<line; i++){		
			msg = dr.readLine();
			list.add(msg);
		}
		num = args[1] - 1
		ms = list.get(num)
		return ms;
	}
}