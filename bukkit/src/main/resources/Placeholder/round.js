/* ******************************************************************************
 *     Copyright (C) 2018 wysohn (Created by black9685, professer_snape, RedLime)
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
function round(args){
    var Integer = java.lang.Integer; 
    var Double = java.lang.Double;
    var string = java.lang.String;
    if(args.length == 0) {
        throw new Error("'$round' placeholder requires at least 1 argument!"); 
    }
    else if(args.length == 1) {
        if(typeof args[0] !== "number") {
            throw new Error("'$round' placeholder should have real number value only!");
        }
        else if(args[0] instanceof Integer) {
            return args[0];
        }
        else { 
            var value = Math.round(args[0]);
            return value; // 
        }
    }
    else if(args.length == 2) {
        if(typeof args[0] !== "number" || typeof args[1] !== "number") {
            throw new Error("'$round' placeholder should have real number value only!"); 
        
        }
        else {
            if(args[0] instanceof Integer) {
                if(args[1] instanceof Double || args[1] < 0) {
                throw new Error("Cannot be rounded to the "+args[1]+" digit after the decimal point, becuase it's not exist!"); 
                }
                else {
                return args[0];
                } 
            }
            if(args[1] instanceof Double || args[1] < 0) {
                throw new Error("Cannot be rounded to the "+args[1]+" digit after the decimal point, becuase it's not exist!");
            }
            else { 
                if(args[1] == 0) {
                    var value = Math.round(args[0]); 
                    return value; 
                }
                else {
                    var uc = 1;
                    for (var i = 1; i <= args[1]; i++) {
                        uc = uc*10;
                    }
                    var value = Math.round(args[0] * uc); 
                    value = value/uc;
                    return value;
                }
            }
        }
    }
    else {
        throw new Error("There are too many arguments! '$round' placeholder only accepts up to 2 arguments.");
    } 
    
}
