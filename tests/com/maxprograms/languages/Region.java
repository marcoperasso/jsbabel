package com.maxprograms.languages;

public class Region implements Comparable<Region> {

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	private String code;
	private String description;
	
	public Region(String code, String description) {
		this.code = code;
		this.description = description;
	}
	
	@Override
	public int compareTo(Region arg0) {
		return description.compareTo(arg0.getDescription());
	}

}
