/*******************************************************************************
 *     Copyright (C) 2018 wysohn (idea provided by gerzytet, author Pro_Snape)
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
function isDouble(n){
	return Number(n) === n && n % 1 !== 0;
}
function packlist(args){
	if(args.length < 1){
		throw new Error("$packlist placeholder should have at least 1 arguments!");
	}
	var arrType = args[0];
	if(args.length == 1){
		var arr = Array();
		if(typeof arrType !== "string"){
			throw new Error("Unknown Type '"+arrType+"' ! You can use either 'String', or 'Int', or 'Double' ! [invalid type]");
		}
		if(arrType.toLowerCase() !== "string" && arrType.toLowerCase() !== "int"&& arrType.toLowerCase() !== "double") {
			throw new Error("Unknown Type '"+arrType+"' ! You can use either 'String', or 'Int', or 'Double' ! [invalid type]");
		}else {	
			if(arrType.toLowerCase() == "string") {
				return Java.to(arr,"java.lang.String[]");
			}
			if(arrType.toLowerCase() == "int") {
				return Java.to(arr,"int[]");
			}
			if(arrType.toLowerCase() == "double") {
				return Java.to(arr,"java.lang.Double[]");
			}	
		}
	}
	if(args.length == 2){
		var arr = Array()
		arr.push(args[1]);
	}
	if(args.length > 2){
		var arr = Array.apply(0, Array(args.length - 1));
		for(i=1; i <= args.length - 1; i++) {
			arr[i-1] = args[i];
		}
	}
	if(arrType.toLowerCase() !== "string" && arrType.toLowerCase() !== "int"&& arrType.toLowerCase() !== "double") {
		throw new Error("Unknown Type '"+args[0]+"' ! You can use either 'String', or 'Int', or 'Double' ! [invalid type]");
	}else {	
		if(arrType.toLowerCase() == "string") {
			var arrsType = "string";
			var arrType = "java.lang.String";
			var msgType = "String"
		}
		if(arrType.toLowerCase() == "int") {
			var arrsType = "number";
			var arrType = "java.lang.Integer";
			var checkDouble = true;
			var msgType = "Integer";
		}
		if(arrType.toLowerCase() == "double") {
			var arrsType = "number";
			var arrType = "java.lang.Double";
			var checkInt = true;
			var msgType = "Double";
		}
		for(i=0; i <= arr.length - 1; i++){
			if(typeof arr[i] !== arrsType) {
				throw new Error("Type of '"+arr[i]+"' is not "+msgType+"! Error occured argument index is "+i+".")
			}
			if(checkDouble == true) {
				if(isDouble(arr[i]) == true) {
					throw new Error("Type of '"+arr[i]+"' is not "+msgType+"! Error occured argument index is "+i+".")
				}
			}
			if(checkInt == true) {
				if(isDouble(arr[i]) !== true) {
					throw new Error("Type of '"+arr[i]+"' is not "+msgType+"! Error occured argument index is "+i+".")
				}
			}
		}
		return Java.to(arr, arrType+"[]")
	}
}
