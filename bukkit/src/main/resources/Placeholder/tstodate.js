function tstodate(args){
	var ts = args[0]*1;
	var date = new Date(ts);
	if(args[1] === "year"){
	date = date.getFullYear();
	return date;
	}else{
	}
	
	if(args[1] === "month"){
	date = date.getMonth();
	return date;
	}else{
	}
	
	if(args[1] === "day"){
	date = date.getDay();
	return date;
	}else{
	}
	
	if(args[1] === "date"){
	date = date.getDate();
	return date;
	}else{
	}
	
	if(args[1] === "hour"){
	date = date.getHours();
	return date;
	}else{
	}
	
	if(args[1] === "minute"){
	date = date.getMinutes();
	return date;
	}else{
	}
	
	if(args[1] === "sec"){
	date = date.getSeconds();
	return date;
	}else{
	}
}