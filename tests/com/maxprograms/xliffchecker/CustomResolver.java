/*
 * Copyright (c) 2009 - 2012 Maxprograms
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rodolfo M. Raya - initial API and implementation
 */
package com.maxprograms.xliffchecker;

import com.sun.org.apache.xerces.internal.util.XMLCatalogResolver;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class CustomResolver implements EntityResolver {

	private XMLCatalogResolver resolver;
	private Hashtable<String, Object> catalog;
	private String workDir;
	private boolean strict;

	public CustomResolver(String fileName) throws ParserConfigurationException, SAXException, IOException {
		File file = new File(fileName);
		if (!file.isAbsolute()) {
			fileName = getAbsolutePath(System.getProperty("user.dir"), fileName); //$NON-NLS-1$
			file = new File(fileName);
		}
		workDir = file.getParent();
		if ( !workDir.endsWith("\\") && !workDir.endsWith("/")) { //$NON-NLS-1$ //$NON-NLS-2$
			workDir = workDir + System.getProperty("file.separator"); //$NON-NLS-1$
		}
		resolver = new XMLCatalogResolver();
		resolver.setPreferPublic(true);
		String[] catalogs = new String[1];
		catalogs[0] = file.toURI().toURL().toString();
		resolver.setCatalogList(catalogs);
		catalog = new Hashtable<String, Object>();

		DocumentBuilderFactory parserFactory = DocumentBuilderFactory.newInstance();
		parserFactory.setNamespaceAware(true);
		DocumentBuilder parser = parserFactory.newDocumentBuilder();
		parser.setErrorHandler(new CustomErrorHandler());
		Document document = parser.parse(new File(fileName));
		recurse(document.getDocumentElement());
	}

	public void setStrict(boolean value) {
		strict = value;
	}

 	public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {

		if (strict) {
			if (systemId.endsWith("xliff-core-1.2-strict.xsd") || systemId.endsWith("xliff-core-1.2-transitional.xsd")) { //$NON-NLS-1$ //$NON-NLS-2$
				return new InputSource(new FileInputStream("catalog/xliff/xliff-core-1.2-strict.xsd")); //$NON-NLS-1$
			}
		} else {
			if (systemId.endsWith("xliff-core-1.2-strict.xsd") || systemId.endsWith("xliff-core-1.2-transitional.xsd")) { //$NON-NLS-1$ //$NON-NLS-2$
				return new InputSource(new FileInputStream("catalog/xliff/xliff-core-1.2-transitional.xsd")); //$NON-NLS-1$
			}
		}

		InputSource source = resolver.resolveEntity(publicId, new File(systemId).getName());
		if (source != null) {
			return source;
		}

		if (publicId == null){
			return null;			
		}

		if (publicId.startsWith("urn:publicid:")) { //$NON-NLS-1$
			publicId = unwrapUrn(publicId);
		}
		if (catalog.containsKey(publicId)) {
			String location = (String) catalog.get(publicId);
			InputStream input = new FileInputStream(location);
			return new InputSource(input);
		}

		if (catalog.containsKey(systemId)) {
			String location = (String) catalog.get(systemId);
			InputStream input = new FileInputStream(location);
			return new InputSource(input);
		}
		String fileName = new File(systemId).getName();
		if (catalog.containsKey(fileName)) {
			String location = (String) catalog.get(fileName);
			InputStream input = new FileInputStream(location);
			return new InputSource(input);
		}

		Enumeration<String> keys = catalog.keys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			File f = new File((String) catalog.get(key));
			String location = f.getName();
			if (systemId.endsWith(location)) {
				InputStream input = new FileInputStream((String) catalog.get(key));
				return new InputSource(input);
			}
		}

		// This DTD is not in the catalogue, 
		// try to find it in the URL reported 
		// by the document
		try {
			URL url = new URL(systemId);
			InputStream input = url.openStream();
			return new InputSource(input);
		} catch (Exception e) {
			MessageFormat mf = new MessageFormat(Messages.getString("CustomResolver.4"));  //$NON-NLS-1$
			Object[] args = {systemId};
			throw new IOException(mf.format(args) ); 
		}
	}

	private void recurse(Element root) throws IOException, ParserConfigurationException, SAXException {
		NodeList children = root.getChildNodes();
		for (int i=0 ; i<children.getLength() ; i++) {
			Node n = children.item(i);
			if (n.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			Element child = (Element) n;
			if (child.getNodeName().equals("system")) { //$NON-NLS-1$
				if (!catalog.containsKey(child.getAttribute("systemId"))) { //$NON-NLS-1$
					catalog.put(child.getAttribute("systemId"), makeAbsolute(child.getAttribute("uri"))); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			if (child.getNodeName().equals("public")) { //$NON-NLS-1$
				String publicId1 = child.getAttribute("publicId"); //$NON-NLS-1$
				if (publicId1.startsWith("urn:publicid:")) { //$NON-NLS-1$
					publicId1 = unwrapUrn(publicId1);
				}
				if (!catalog.containsKey(publicId1)) { 
					catalog.put(publicId1, makeAbsolute(child.getAttribute("uri"))); //$NON-NLS-1$ 
				}
			}
			if ( child.getNodeName().equals("uri")) { //$NON-NLS-1$
				if (!catalog.containsKey(child.getAttribute("name"))) { //$NON-NLS-1$
					catalog.put(child.getAttribute("name"), makeAbsolute(child.getAttribute("uri"))); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			if (child.getNodeName().equals("nextCatalog")) { //$NON-NLS-1$
				String nextCatalog = child.getAttribute("catalog"); //$NON-NLS-1$
				File f = new File(nextCatalog);
				if (!f.isAbsolute()) {
					nextCatalog = getAbsolutePath(workDir, nextCatalog);
				}
				CustomResolver cat = new CustomResolver(nextCatalog); 
				Hashtable<String,Object> table = cat.getCatalogue();
				Enumeration<String> keys = table.keys();
				while (keys.hasMoreElements()) {
					String key = keys.nextElement();
					if (!catalog.containsKey(key)) {
						catalog.put(key, table.get(key));
					}
				}
			}
			recurse(child);
		}

	}

	private Hashtable<String, Object> getCatalogue() {
		return catalog;
	}

	private Object makeAbsolute(String file) throws IOException {
		File f = new File(file);
		if ( !f.isAbsolute()) {
			file = getAbsolutePath(workDir, file);
		}
		return file;
	}

	private static String getAbsolutePath(String homeFile, String relative) throws IOException{
		File home = new File(homeFile);
		// If home is a file, get the parent
		File result;
		if (home.isDirectory()){
			result = new File(home.getAbsolutePath(), relative);	   		
		} else {
			result = new File(home.getParent(), relative);
		}
		return result.getCanonicalPath();
	}

	private static String unwrapUrn(String urn) {
		if (!urn.startsWith("urn:publicid:")) { //$NON-NLS-1$
			return urn;
		}
		String publicId = urn.trim().substring("urn:publicid:".length()); //$NON-NLS-1$
		publicId = publicId.replaceAll("\\+", " "); //$NON-NLS-1$ //$NON-NLS-2$
		publicId = publicId.replaceAll("\\:", "//"); //$NON-NLS-1$ //$NON-NLS-2$
		publicId = publicId.replaceAll(";", "::"); //$NON-NLS-1$ //$NON-NLS-2$
		publicId = publicId.replaceAll("%2B", "+"); //$NON-NLS-1$ //$NON-NLS-2$
		publicId = publicId.replaceAll("%3A", ":"); //$NON-NLS-1$ //$NON-NLS-2$
		publicId = publicId.replaceAll("%2F", "/"); //$NON-NLS-1$ //$NON-NLS-2$
		publicId = publicId.replaceAll("%3B", ";"); //$NON-NLS-1$ //$NON-NLS-2$
		publicId = publicId.replaceAll("%27", "'"); //$NON-NLS-1$ //$NON-NLS-2$
		publicId = publicId.replaceAll("%3F", "?"); //$NON-NLS-1$ //$NON-NLS-2$
		publicId = publicId.replaceAll("%23", "#"); //$NON-NLS-1$ //$NON-NLS-2$
		publicId = publicId.replaceAll("%25", "%"); //$NON-NLS-1$ //$NON-NLS-2$
		return publicId;
	}

}
