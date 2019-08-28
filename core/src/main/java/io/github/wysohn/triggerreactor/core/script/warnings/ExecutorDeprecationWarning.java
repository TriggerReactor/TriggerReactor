package io.github.wysohn.triggerreactor.core.script.warnings;

public class ExecutorDeprecationWarning extends Warning {
	private int row;
	private String executorName;
	private String context;
	
	public ExecutorDeprecationWarning(int row, String executorName, String context) {
		this.row = row;
		this.executorName = executorName;
		this.context = context;
	}

	@Override
	public String[] getMessageLines() {
		return new String[] {
				"Deprecated executor found at line " + row + ": ",
				context,
				executorName + " is deprecated and may be removed in a future release"
		};
	}
}
