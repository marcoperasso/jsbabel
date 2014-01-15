package com.maxprograms.languages;

import java.util.Hashtable;
import java.util.Set;

public class RegistryEntry {
	
	private Hashtable<String, String> table;

	public RegistryEntry(String entry) {
		table = new Hashtable<String, String>();
		parseEntry(entry);
	}

	private void parseEntry(String entry) {
		String[] lines = entry.split("\n"); //$NON-NLS-1$
		for (int i=0 ; i<lines.length ; i++) {
			String type = lines[i].substring(0,lines[i].indexOf(":")).trim(); //$NON-NLS-1$
			String value = lines[i].substring(lines[i].indexOf(":")+1).trim(); //$NON-NLS-1$
			if (!table.containsKey(type)) {
				table.put(type, value);
			} else {
				table.put(type, table.get(type) + " | " + value); //$NON-NLS-1$
			}
		}
	}
	
	public Set<String> getTypes() {
		return table.keySet();
	}

	public String get(String string) {
		return table.get(string);
	}

	public String getType() {
		if (table.containsKey("Type")) { //$NON-NLS-1$
			return table.get("Type"); //$NON-NLS-1$
		}
		return null;
	}

	public String getDescription() {
		if (table.containsKey("Description")) { //$NON-NLS-1$
			return table.get("Description"); //$NON-NLS-1$
		}
		return null;
	}

	public String getSubtag() {
		if (table.containsKey("Subtag")) { //$NON-NLS-1$
			return table.get("Subtag"); //$NON-NLS-1$
		}
		return null;
	}
}
