/*
 * Copyright (c) 2009 Maxprograms
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

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class CustomErrorHandler implements ErrorHandler {

	public void warning(SAXParseException exception) throws SAXException {
		System.err.println("[Warning] " + exception.getLineNumber() + ":" + exception.getColumnNumber() + " "  + exception.getMessage());   //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		throw exception;
	}

	public void error(SAXParseException exception) throws SAXException {
		System.err.println("[Error] " + exception.getLineNumber() + ":" + exception.getColumnNumber() + " "  + exception.getMessage());   //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		throw new SAXException("[Error] " + exception.getLineNumber() + ":" + exception.getColumnNumber() + " "  + exception.getMessage());  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	}

	public void fatalError(SAXParseException exception) throws SAXException {
		System.err.println("[Fatal Error] " + exception.getLineNumber() + ":" + exception.getColumnNumber() + " "  + exception.getMessage());   //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		throw new SAXException("[Fatal Error] " + exception.getLineNumber() + ":" + exception.getColumnNumber() + " "  + exception.getMessage());   //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	}
}
