package com.maxprograms.languages;

public class Script implements Comparable<Script> {

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	private String code;
	private String description;
	
	public Script(String code, String description) {
		this.code = code;
		this.description = description;
	}
	
	@Override
	public int compareTo(Script arg0) {
		return description.compareTo(arg0.getDescription());
	}

}
