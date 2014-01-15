package com.maxprograms.languages;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class RegistryParser {

	private List<RegistryEntry> entries;
	private Hashtable<String, Language> languages;
	private Hashtable<String, Region> regions;
	private Hashtable<String, Script> scripts;
	private Hashtable<String, Variant> variants;

	private void parseRegistry(URL url) throws IOException {
		InputStream input = url.openStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		String line = ""; //$NON-NLS-1$
		entries = new ArrayList<RegistryEntry>();
		String buffer = ""; //$NON-NLS-1$
		while ((line = reader.readLine()) != null) {
			if (line.trim().equals("%%")) { //$NON-NLS-1$
				entries.add(new RegistryEntry(buffer.replaceAll("\n ", " "))); //$NON-NLS-1$ //$NON-NLS-2$
				buffer = ""; //$NON-NLS-1$
			} else {
				buffer = buffer + line + "\n"; //$NON-NLS-1$
			}
		}	
		languages = new Hashtable<String, Language>();
		regions = new Hashtable<String, Region>();
		scripts = new Hashtable<String, Script>();
		variants = new Hashtable<String, Variant>();
		Iterator<RegistryEntry> it = entries.iterator();
		while (it.hasNext()) {
			RegistryEntry entry = it.next();
			String type = entry.getType();
			if (type == null) {
				continue;
			}
			if (type.equals("language")) { //$NON-NLS-1$
				String description = entry.getDescription();
				if (description.indexOf("Private use") != -1) { //$NON-NLS-1$
					continue;
				}
				String subtag = entry.getSubtag();
				if (subtag != null) {
					if (description.indexOf("|") != -1) { //$NON-NLS-1$
						// trim and use only the first name
						description = description.substring(0,description.indexOf("|") -1); //$NON-NLS-1$
					}
					if (subtag.equals("el")) { //$NON-NLS-1$
						description = "Greek"; //$NON-NLS-1$
					}
					description = description.replaceAll("\\(.*\\)",""); //$NON-NLS-1$ //$NON-NLS-2$
					Language lang = new Language(subtag, description);
					String suppressedScript = entry.get("Suppress-Script"); //$NON-NLS-1$
					if (suppressedScript != null) {
						lang.setSuppressedScript(suppressedScript);
					}
					languages.put(subtag, lang);
				}
			}
			if (type.equals("region")) { //$NON-NLS-1$
				String description = entry.getDescription();
				if (description.indexOf("Private use") != -1) { //$NON-NLS-1$
					continue;
				}
				String subtag = entry.getSubtag();
				if (subtag != null) {
					regions.put(subtag, new Region(subtag, description));
				}
			}
			if (type.equals("script")) { //$NON-NLS-1$
				String description = entry.getDescription();
				if (description.indexOf("Private use") != -1) { //$NON-NLS-1$
					continue;
				}
				description = description.replace('(','[');
				description = description.replace(')',']');
				String subtag = entry.getSubtag();
				if (subtag != null) {
					scripts.put(subtag, new Script(subtag, description));
				}
			}
			if (type.equals("variant")) { //$NON-NLS-1$
				String description = entry.getDescription();
				if (description.indexOf("Private use") != -1) { //$NON-NLS-1$
					continue;
				}
				description = description.replace('(','[');
				description = description.replace(')',']');
				String subtag = entry.getSubtag();
				String prefix = entry.get("Prefix"); //$NON-NLS-1$
				if (subtag != null) {
					variants.put(subtag, new Variant(subtag, description, prefix));
				}
			}
		}
		
	}
	
	public String getRegistryDate() {
		Iterator<RegistryEntry> it = entries.iterator();
		while (it.hasNext()) {
			RegistryEntry entry = it.next();
			Set<String> set = entry.getTypes();
			if (set.contains("﻿File-Date")) { //$NON-NLS-1$
				return entry.get("﻿File-Date"); //$NON-NLS-1$
			}	
		}
		return null;
	}
	
	public RegistryParser(URL url) throws MalformedURLException, IOException {
		parseRegistry(url);
	}
	
	public RegistryParser()  throws MalformedURLException, IOException {
		URL url = RegistryParser.class.getResource("language-subtag-registry.txt");  //$NON-NLS-1$
		parseRegistry(url);
	}

	public String getTagDescription(String tag) {
		String[] parts = tag.split("-"); //$NON-NLS-1$
		if (parts.length == 1) {
			// language part only
			if (languages.containsKey(tag.toLowerCase())) {
				return languages.get(tag.toLowerCase()).getDescription();
			}			 
		} else if (parts.length == 2) {
			// contains either script or region
			if (!languages.containsKey(parts[0].toLowerCase())) {
				return ""; //$NON-NLS-1$
			}
			Language lang =languages.get(parts[0].toLowerCase());
			if (parts[1].length() == 2) {
				// could be a country code
				if (regions.containsKey(parts[1].toUpperCase())) {
					return lang.getDescription() + " (" + regions.get(parts[1].toUpperCase()).getDescription() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			if (parts[1].length() == 3) {
				// could be a UN region code
				if (regions.containsKey(parts[1])) {
					Region reg = regions.get(parts[1]); 
					return lang.getDescription() + " (" + reg.getDescription() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			if (parts[1].length() == 4) {
				// could have script
				String script = parts[1].substring(0,1).toUpperCase() + parts[1].substring(1).toLowerCase();
				if (script.equals(lang.getSuppresedScript())) {
					return ""; //$NON-NLS-1$
				}
				if (scripts.containsKey(script)) {
					return lang.getDescription() + " (" + scripts.get(script).getDescription() + ")";  //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			// try with a variant
			if (variants.containsKey(parts[1].toLowerCase())) {
				Variant var = variants.get(parts[1].toLowerCase());
				if (var != null && var.getPrefix().equals(parts[0].toLowerCase())) {
					// variant is valid for the language code
					return lang.getDescription() + " (" + var.getDescription() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		} else if (parts.length == 3) {
			if (!languages.containsKey(parts[0].toLowerCase())) {
				return ""; //$NON-NLS-1$
			}
			Language lang =languages.get(parts[0].toLowerCase());
			if (parts[1].length() == 4) {
				// could be script + region or variant
				String script = parts[1].substring(0,1).toUpperCase() + parts[1].substring(1).toLowerCase();
				if (script.equals(lang.getSuppresedScript())) {
					return ""; //$NON-NLS-1$
				}
				if (scripts.containsKey(script)) {
					Script scr = scripts.get(script);
					// check if next part is a region or variant
					if (regions.containsKey(parts[2].toUpperCase())) {
						// check if next part is a variant
						Region reg = regions.get(parts[2].toUpperCase());
						return lang.getDescription() + " (" + scr.getDescription() + ", " + reg.getDescription() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
					if (variants.containsKey(parts[2].toLowerCase())) {
						Variant var = variants.get(parts[2].toLowerCase());
						if (var != null && var.getPrefix().equals(parts[0].toLowerCase())) {
							// variant is valid for the language code
							return lang.getDescription() + " (" + scr.getDescription() + ", " + var.getDescription() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						}
					}
				}
			} else {
				// could be region + variant
				if (parts[1].length() == 2 || parts[1].length() == 3) {
					// could be a region code
					if (regions.containsKey(parts[1].toUpperCase())) {
						// check if next part is a variant
						Region reg = regions.get(parts[1].toUpperCase());
						if (variants.containsKey(parts[2].toLowerCase())) {
							Variant var = variants.get(parts[2].toLowerCase());
							if (var != null && var.getPrefix().equals(parts[0].toLowerCase())) {
								// variant is valid for the language code
								return lang.getDescription() + " (" + reg.getDescription() + " - " + var.getDescription() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							}
						}
					}					
				}
			}
		}
		return ""; //$NON-NLS-1$
	}
	
}


