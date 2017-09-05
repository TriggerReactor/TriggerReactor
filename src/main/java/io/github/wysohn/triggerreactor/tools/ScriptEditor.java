/*******************************************************************************
 *     Copyright (C) 2017 wysohn
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
package io.github.wysohn.triggerreactor.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptException;

import org.bukkit.ChatColor;
import org.bukkit.conversations.Conversable;

public class ScriptEditor{
    private static final String separater = System.lineSeparator();

    private final String title;
	private final String script;
	private final SaveHandler handler;

	private int currentIndex = 0;
	private int currentCursor = 0;
	private List<String> lines = new ArrayList<String>();

	public ScriptEditor(String title, String script, SaveHandler handler) {
	    this.title = title;
		this.script = script;
		this.handler = handler;

		load();
	}

	private void load(){
		String[] strs = script.split(separater);
		for(String str : strs){
			lines.add(str.replaceAll("\\t", "    "));
		}
	}

	public void printScript(Conversable conversable){
		clearScreen(conversable);
		printHeader(conversable);
		printSource(conversable);
		printFooter(conversable);
	}

	private final int separatorSize = 60;
	private void printSeparator(Conversable conversable){
		StringBuilder builder = new StringBuilder();
		for(int i = 0;i < separatorSize; i++){
			builder.append('-');
		}
		conversable.sendRawMessage(ChatColor.GRAY+builder.toString());
	}

	private void clearScreen(Conversable conversable){
		for(int i = 0; i < 40; i++){
			conversable.sendRawMessage("");
		}
	}

	private void printHeader(Conversable conversable){
		conversable.sendRawMessage(ChatColor.LIGHT_PURPLE + "save" + ChatColor.DARK_GRAY + ", " + ChatColor.LIGHT_PURPLE
				+ "exit " + ChatColor.BLUE + title);
		printSeparator(conversable);
	}

	private void printSource(Conversable conversable){
		String[] display = new String[16];

		int j = 0;
		for(int i = currentIndex; i < Math.min(lines.size(), currentIndex + 16); i++){
			display[j++] = width(String.valueOf(i+1), 3)+". "+lines.get(i) + (currentCursor == i ? ChatColor.RED+"<<" : "");
		}

		for(String dis : display)
			conversable.sendRawMessage(dis);
	}

	private void printFooter(Conversable conversable) {
		printSeparator(conversable);
		conversable.sendRawMessage(ChatColor.LIGHT_PURPLE + "u <lines>" + ChatColor.DARK_GRAY + ", "
				+ ChatColor.LIGHT_PURPLE + "d <lines>" + ChatColor.DARK_GRAY + ", "
		        + ChatColor.LIGHT_PURPLE + "il"+ ChatColor.DARK_GRAY + ", "
				+ ChatColor.LIGHT_PURPLE + "dl");
	}

	public void save() throws IOException, ScriptException{
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < lines.size(); i++){
			builder.append((i != 0 ? separater : "") + lines.get(i));
		}
		handler.onSave(builder.toString());
	}

	public void up(int lines){
		if(lines <= 0)
			return;

		currentCursor = Math.max(0, currentCursor - lines);

		//if not within 16 around the index
		if(!(currentIndex <= currentCursor && currentCursor < currentIndex + 16)){
			currentIndex = currentCursor;
		}
	}

	public void down(int lines){
		if(lines <= 0)
			return;

		currentCursor = Math.min(this.lines.size() - 1, currentCursor + lines);

		//if not within 16 around the index
		if(!(currentIndex <= currentCursor && currentCursor < currentIndex + 16)){
			currentIndex = currentCursor - 16 + 1;
		}
	}

	public void insertNewLine(){
		if(currentCursor + 1 > lines.size())
			lines.add("");
		else
			lines.add(currentCursor + 1, "");
	}

	public void deleteLine(){
		lines.remove(currentCursor);

		if(currentCursor >= lines.size()){
			currentCursor--;
		}
	}

	public void intput(String input){
		lines.set(currentCursor, input);
	}

	public String getLine(){
		return lines.get(currentCursor);
	}

	public interface SaveHandler{
	    void onSave(String script);
	}

	private static String width(String str, int length){
	    int d = length - str.length();
	    if(d <= 0)
	        return str;

	    StringBuilder builder = new StringBuilder();
	    for(int i = 0; i < d; i++)
	        builder.append(" ");
	    builder.append(str);

	    return builder.toString();
	}
}
