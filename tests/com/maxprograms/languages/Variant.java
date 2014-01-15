package com.maxprograms.languages;

public class Variant implements Comparable<Variant> {

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	private String code;
	private String description;
	private String prefix;
	
	public Variant(String code, String description, String prefix) {
		this.code = code;
		this.description = description;
		this.prefix = prefix;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	
	@Override
	public int compareTo(Variant arg0) {
		return description.compareTo(arg0.getDescription());
	}

}
