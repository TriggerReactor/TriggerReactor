package io.github.wysohn.triggerreactor.core.script.warning;

public abstract class Warning {
	//format and return the warning message, without chat color
	public abstract String[] getMessageLines();
	
	//used for testing
	public boolean equals(Warning other) {
		String[] m1 = getMessageLines();
		String[] m2 = other.getMessageLines();
		if (m1.length != m2.length)
			return false;
		
		for (int i = 0; i < m1.length; i++) {
			if (!(m1[i].equals(m2[i])))
				return false;
		}
		return true;
	}
}