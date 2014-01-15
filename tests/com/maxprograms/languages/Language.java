package com.maxprograms.languages;


public class Language implements Comparable<Language> {

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	private String code;
	private String description;
	private String script;

	public Language(String code, String description) {
		this.code = code;
		this.description = description;
		script = ""; //$NON-NLS-1$
	}

	@Override
	public int compareTo(Language arg0) {
		return description.compareTo(arg0.getDescription());
	}
	
	public void setSuppressedScript(String value) {
		script = value;
	}
	
	public String getSuppresedScript() {
		return script;
	}
	
}
