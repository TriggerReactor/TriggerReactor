/*******************************************************************************
 *     Copyright (C) 2018 wysohn
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
function isnumber(args){
    if(args.length !== 1)
        throw new Error("Invalid parameter(s) found. $isnumber accepts up to one argument.");

    if(typeof args[0] !== "string"){
        if(typeof args[0] === "number")
            return true;    
        
        throw new Error("Invalid parameter type. $isnumber only accepts String value.");
    }
    
    var arg = args[0];

    return arg.matches("[-]{0,1}[0-9]+[.]{0,1}[0-9]{0,1}$");
}
