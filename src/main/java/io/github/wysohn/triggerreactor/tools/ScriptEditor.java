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

	public void printScript(ScriptEditorUser conversable){
		clearScreen(conversable);
		printHeader(conversable);
		printSource(conversable);
		printFooter(conversable);
	}

	private final int separatorSize = 60;
	private void printSeparator(ScriptEditorUser conversable){
		StringBuilder builder = new StringBuilder();
		for(int i = 0;i < separatorSize; i++){
			builder.append('-');
		}
		conversable.sendMessage("&7"+builder.toString());
	}

	private void clearScreen(ScriptEditorUser conversable){
		for(int i = 0; i < 40; i++){
			conversable.sendMessage("");
		}
	}

	private void printHeader(ScriptEditorUser conversable){
		conversable.sendMessage("&dsave&8, &dexit &9" + title);
		printSeparator(conversable);
	}

	private void printSource(ScriptEditorUser conversable){
		String[] display = new String[16];

		int j = 0;
		for(int i = currentIndex; i < Math.min(lines.size(), currentIndex + 16); i++){
			display[j++] = width(String.valueOf(i+1), 3)+". "+lines.get(i) + (currentCursor == i ? "&c<<" : "");
		}

		for(String dis : display)
			conversable.sendMessage(dis == null ? "" : dis);
	}

	private void printFooter(ScriptEditorUser conversable) {
		printSeparator(conversable);
		conversable.sendMessage("&du <lines>&8, &dd <lines>&8, &dil&8, &ddl");
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

	public interface ScriptEditorUser{
	    void sendMessage(String rawMessage);
	}

	public static final String USAGE = "&dIn edit mode, you cannot receieve any message from the other users. You can type &6save &dor &6exit "
            + "&dany time to escape from the edit mode. If the code is more than one line, you can go up or down by typing &6u "
            + "&dor &6d. &dWhen you are doing so, you can provide number of lines to skip. (for example, &6d 10 &dwill move down 10 lines). "
            + "If you don't provide the number, the default value 1 will be used instead. &6il &dto insert a new line and "
            + "&6dl &dto delete current line. Look for &c<< &d sign to see where you are currently at. You can also "
            + "&6type anything and press Tab key &dto copy the code where the cursor is pointing. Because of how minecraft client is made,"
            + " you need to specify space with &6^ to insert spaces. For example, you might can add four spaces before the code like this: "
            + "&6^^^^#MESSAGE \"HI\" &dAlso, if you want to copy current line to the command prompt, &6type anything on the prompt "
            + "and press tab&d.    &aNow enter anything to continue...";
}
