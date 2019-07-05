package rhigin.scripts.comple;

import org.mozilla.javascript.Script;

public class ScriptElement {
	private Script script;
	private String name;
	private long time;
	
	public ScriptElement(Script s, String n, long t) {
		script = s;
		name = n;
		time = t;
	}
	
	public Script getScript() {
		return script;
	}
	
	public String getName() {
		return name;
	}
	
	public long getTime() {
		return time;
	}
}
