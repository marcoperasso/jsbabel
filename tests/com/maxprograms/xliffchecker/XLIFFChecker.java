package com.maxprograms.xliffchecker;

/*
 * Copyright (c) 2009-2011 Maxprograms
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Rodolfo M. Raya - initial API and implementation
 */
import com.maxprograms.languages.RegistryParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

class Styled {

    StringBuilder sb = new StringBuilder();

    void append(String format) {
        sb.append(format);
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}

class Display {

    void update() {
    }

    void readAndDispatch() {
    }
}

public class XLIFFChecker {

    private Document document;
    private Hashtable<String, String> ids;
    private Hashtable<String, String> groupIds;
    private Hashtable<String, String> exTable;
    private Hashtable<String, String> bxTable;
    private Hashtable<String, String> bptTable;
    private Hashtable<String, String> eptTable;
    private Hashtable<String, String> inlineIds;
    private Hashtable<String, String> srcItTable;
    private Hashtable<String, String> tgtItTable;
    private Hashtable<String, String> altSrcItTable;
    private Hashtable<String, String> altTgtItTable;
    private Hashtable<String, String> midTable;
    private Hashtable<String, String> phasesTable;
    private Hashtable<String, String> toolsTable;
    private String xliffNamespace;
    private String sourceLanguage;
    private String targetLanguage;
    private boolean hasExtensions;
    private boolean inAltTrans;
    private boolean inSegSource;
    private Hashtable<String, Set<String>> attributesTable;
    private String version;
    private boolean inSource;
    private Hashtable<String, String> xids;
    private RegistryParser langParser;
    private Styled styled = new Styled();
    private Display display = new Display();
    private boolean strict = false;

    public XLIFFChecker() {
        Locale.setDefault(new Locale(getLanguage()));

        //Display.setAppName(Messages.getString("XLIFFChecker.0")); //$NON-NLS-1$
        //display = new Display();

        //shell = new Shell(display, SWT.SHELL_TRIM);
        //shell.setText(Messages.getString("XLIFFChecker.1")); //$NON-NLS-1$
        //shell.setImage(new Image(display,"images/Icons/Bronze_squares.png")); //$NON-NLS-1$
        //shell.setLayout(new GridLayout());
        //shell.setSize(420,450);

        //Menu mainMenu = new Menu(shell, SWT.BAR);
        //shell.setMenuBar(mainMenu);

        //Menu fileMenu = new Menu(mainMenu);
        //MenuItem file = new MenuItem(mainMenu, SWT.CASCADE);
        //file.setText(Messages.getString("XLIFFChecker.3"));  //$NON-NLS-1$
        //file.setMenu(fileMenu);

        //MenuItem validate = new MenuItem(fileMenu,SWT.PUSH);
        //validate.setText(Messages.getString("XLIFFChecker.4")); //$NON-NLS-1$
        //validate.setImage(new Image(display,"images/Small/open.png")); //$NON-NLS-1$
        //validate.addSelectionListener(new SelectionAdapter(){

        //	public void widgetSelected(SelectionEvent arg0) {
        //		validateFile();
        //	}
        //});

        //MenuItem cleanChars = new MenuItem(fileMenu,SWT.PUSH);
        //cleanChars.setText(Messages.getString("XLIFFChecker.12")); //$NON-NLS-1$
        //cleanChars.setImage(new Image(display,"images/Small/chars.png")); //$NON-NLS-1$
        //cleanChars.addSelectionListener(new SelectionListener(){

        //	public void widgetSelected(SelectionEvent arg0) {
        //		cleanCharacters();
        //	}

        //	public void widgetDefaultSelected(SelectionEvent arg0) {
        // do nothing				
        //	}
        // });
        //new MenuItem(fileMenu,SWT.SEPARATOR);

        //MenuItem exit = new MenuItem(fileMenu, SWT.PUSH);
        //if ( System.getProperty("file.separator").equals("\\")) { //$NON-NLS-1$ //$NON-NLS-2$
        //	exit.setText(Messages.getString("XLIFFChecker.6"));  //$NON-NLS-1$
        //	exit.setAccelerator(SWT.ALT | SWT.F4);
        //} else {
        //	if (!System.getProperty("user.home").startsWith("/User")) { //$NON-NLS-1$ //$NON-NLS-2$
        //		exit.setText(Messages.getString("XLIFFChecker.7")); //$NON-NLS-1$
        //		exit.setAccelerator(SWT.CTRL | 'Q');
        //	} else {
        //		exit.setText(Messages.getString("XLIFFChecker.8"));  //$NON-NLS-1$
        //		exit.setAccelerator(SWT.COMMAND | 'Q');
        //	}
        //}
        //exit.addSelectionListener(new SelectionAdapter() {
        //	public void widgetSelected(SelectionEvent e) {
        //		shell.dispose();
        //	}
        //});

        //Menu optionsMenu = new Menu(mainMenu);
        //MenuItem options = new MenuItem(mainMenu,SWT.CASCADE);
        //options.setText(Messages.getString("XLIFFChecker.9")); //$NON-NLS-1$
        //options.setMenu(optionsMenu);

        ///MenuItem languagesItem = new MenuItem(optionsMenu,SWT.CASCADE);
        //languagesItem.setText(Messages.getString("XLIFFChecker.10")); //$NON-NLS-1$
        //Menu langsMenu = new Menu(languagesItem);
        //languagesItem.setMenu(langsMenu);

        //MenuItem english = new MenuItem(langsMenu,SWT.PUSH);
        //english.setText(Messages.getString("XLIFFChecker.11")); //$NON-NLS-1$
        //english.addSelectionListener(new SelectionAdapter() {
        //	public void widgetSelected(SelectionEvent e) {
        //		saveLanguage("en"); //$NON-NLS-1$
        //	}
        ///});

        //MenuItem polish = new MenuItem(langsMenu,SWT.PUSH);
        //polish.setText(Messages.getString("XLIFFChecker.13")); //$NON-NLS-1$
        //polish.addSelectionListener(new SelectionAdapter() {
        //	public void widgetSelected(SelectionEvent e) {
        //		saveLanguage("pl"); //$NON-NLS-1$
        //	}
        //});

        //MenuItem spanish = new MenuItem(langsMenu,SWT.PUSH);
        //spanish.setText(Messages.getString("XLIFFChecker.15")); //$NON-NLS-1$
        //spanish.addSelectionListener(new SelectionAdapter() {
        //	public void widgetSelected(SelectionEvent e) {
        //		saveLanguage("es"); //$NON-NLS-1$
        //	}
        //});

        //Menu helpMenu = new Menu(mainMenu);
        //MenuItem help = new MenuItem(mainMenu, SWT.CASCADE);
        //help.setText(Messages.getString("XLIFFChecker.17"));  //$NON-NLS-1$
        //help.setMenu(helpMenu);

        //MenuItem about = new MenuItem(helpMenu,SWT.PUSH);
        //about.setText(Messages.getString("XLIFFChecker.18")); //$NON-NLS-1$
        //about.addSelectionListener(new SelectionAdapter() {
        //	public void widgetSelected(SelectionEvent e) {
        //		About aboutBox = new About(shell);
        //		aboutBox.show();
        //	}
        //});

        //ToolBar toolBar = new ToolBar(shell, SWT.FLAT);

        //ToolItem openItem = new ToolItem(toolBar, SWT.PUSH);
        //openItem.setToolTipText(Messages.getString("XLIFFChecker.19")); //$NON-NLS-1$
        //openItem.setImage(new Image(display, "images/Normal/open.png")); //$NON-NLS-1$
        //openItem.addSelectionListener(new SelectionAdapter() {
        //	public void widgetSelected(SelectionEvent arg0) {
        //		validateFile();
        //	}
        //});

        //ToolItem cleanCharsItem = new ToolItem(toolBar, SWT.PUSH);
        //cleanCharsItem.setToolTipText(Messages.getString("XLIFFChecker.14")); //$NON-NLS-1$
        //cleanCharsItem.setImage(new Image(display, "images/Normal/chars.png")); //$NON-NLS-1$
        //cleanCharsItem.addSelectionListener(new SelectionListener() {

        //	public void widgetSelected(SelectionEvent arg0) {
        //		cleanCharacters();
        //	}

        //	public void widgetDefaultSelected(SelectionEvent arg0) {
        // do nothing
        //	}
        //});
        //styled = new StyledText(shell,SWT.BORDER|SWT.V_SCROLL|SWT.H_SCROLL|SWT.READ_ONLY);
        //styled.setLayoutData(new GridData(GridData.FILL_BOTH));

    }

    /*private void cleanCharacters() {
     FileDialog fd = new FileDialog(shell, SWT.OPEN);
     String[] extensions = { "*.xlf", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
     fd.setFilterExtensions(extensions);
     String[] names = { Messages.getString("XLIFFChecker.16"), Messages.getString("XLIFFChecker.20") };  //$NON-NLS-1$ //$NON-NLS-2$
     fd.setFilterNames(names);
     String name = fd.open();
     if (name != null) {
     MessageFormat mf = new MessageFormat(Messages.getString("XLIFFChecker.21"));  //$NON-NLS-1$
     Object[] args = { name };
     styled.setText(mf.format(args));
     shell.setCursor(new Cursor(display, SWT.CURSOR_WAIT));
     display.update();
     display.readAndDispatch();
     try {
     clean(name);
     } catch (Exception e) {
     if (e.getMessage() != null) {
     styled.setText(e.getMessage());
     } else {
     MessageFormat mf1 = new MessageFormat(Messages.getString("XLIFFChecker.24"));  //$NON-NLS-1$
     styled.setText(mf1.format(args));
     }
     }
     styled.setText(Messages.getString("XLIFFChecker.27"));  //$NON-NLS-1$
     shell.setCursor(new Cursor(display, SWT.CURSOR_ARROW));
     }
     }*/

    /*private static void clean(String name) throws IOException {
     FileInputStream stream = new FileInputStream(name);
     String encoding = getXMLEncoding(name);
     InputStreamReader input = new InputStreamReader(stream, encoding);
     BufferedReader buffer = new BufferedReader(input);
     FileOutputStream output = new FileOutputStream(name + ".tmp"); //$NON-NLS-1$
     String line = buffer.readLine();
     while (line != null) {
     line = validChars(line) + "\n"; //$NON-NLS-1$
     output.write(line.getBytes(encoding));
     line = buffer.readLine();
     }
     output.close();
     input.close();
     String backup = name + ".bak"; //$NON-NLS-1$
     if (name.indexOf(".") != -1 && name.lastIndexOf(".") < name.length()) { //$NON-NLS-1$ //$NON-NLS-2$
     backup = name.substring(0, name.lastIndexOf(".")) + ".~" + name.substring(name.lastIndexOf(".") + 1); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
     }
     File f = new File(backup);
     if (f.exists()) {
     f.delete();
     }
     File original = new File(name);
     original.renameTo(f);
     File ok = new File(name + ".tmp"); //$NON-NLS-1$
     original = null;
     original = new File(name);
     ok.renameTo(original);
     }
     */
    private static String getXMLEncoding(String fileName) {
        // return UTF-8 as default
        String result = "UTF-8"; //$NON-NLS-1$
        try {
            // check if there is a BOM (byte order mark)
            // at the start of the document
            FileInputStream inputStream = new FileInputStream(fileName);
            byte[] array = new byte[2];
            inputStream.read(array);
            inputStream.close();
            byte[] lt = "<".getBytes(); //$NON-NLS-1$
            byte[] feff = {-1, -2};
            byte[] fffe = {-2, -1};
            if (array[0] != lt[0]) {
                // there is a BOM, now check the order
                if (array[0] == fffe[0] && array[1] == fffe[1]) {
                    return "UTF-16BE"; //$NON-NLS-1$
                }
                if (array[0] == feff[0] && array[1] == feff[1]) {
                    return "UTF-16LE"; //$NON-NLS-1$
                }
            }
            // check declared encoding
            FileReader input = new FileReader(fileName);
            BufferedReader buffer = new BufferedReader(input);
            String line = buffer.readLine();
            input.close();
            if (line.startsWith("<?")) { //$NON-NLS-1$
                line = line.substring(2, line.indexOf("?>")); //$NON-NLS-1$
                line = line.replaceAll("\'", "\""); //$NON-NLS-1$ //$NON-NLS-2$
                StringTokenizer tokenizer = new StringTokenizer(line);
                while (tokenizer.hasMoreTokens()) {
                    String token = tokenizer.nextToken();
                    if (token.startsWith("encoding")) { //$NON-NLS-1$
                        result = token.substring(token.indexOf("\"") + 1, token.lastIndexOf("\"")); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result.equals("utf-8")) { //$NON-NLS-1$
            result = "UTF-8"; //$NON-NLS-1$
        }
        return result;
    }

    private static String validChars(String input) {
        // Valid: #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] |
        // [#x10000-#x10FFFF]
        // Discouraged: [#x7F-#x84], [#x86-#x9F], [#xFDD0-#xFDDF]
        //
        StringBuffer buffer = new StringBuffer();
        char c;
        int length = input.length();
        for (int i = 0; i < length; i++) {
            c = input.charAt(i);
            if ((c == '\t' || c == '\n' || c == '\r' || (c >= '\u0020' && c <= '\uD7DF') || (c >= '\uE000' && c <= '\uFFFD'))) {
                // normal character
                buffer.append(c);
            } else if ((c >= '\u007F' && c <= '\u0084') || (c >= '\u0086' && c <= '\u009F')
                    || (c >= '\uFDD0' && c <= '\uFDDF')) {
                // Control character
                buffer.append("&#x" + Integer.toHexString(c) + ";"); //$NON-NLS-1$ //$NON-NLS-2$
            } else if ((c >= '\uDC00' && c <= '\uDFFF') || (c >= '\uD800' && c <= '\uDBFF')) {
                // Multiplane character
                buffer.append(input.substring(i, i + 1));
            }
        }
        return buffer.toString();
    }

    private String getLanguage() {
        try {
            File preferences = new File(getPreferencesDir(), "xliffchecker"); //$NON-NLS-1$
            if (!preferences.exists()) {
                return "en"; //$NON-NLS-1$
            }
            BufferedReader reader = new BufferedReader(new FileReader(preferences));
            String lang = reader.readLine();
            reader.close();
            return lang;
        } catch (Exception e) {
            return "en"; //$NON-NLS-1$
        }
    }

    /*protected void saveLanguage(String string) {
     try {
     File preferences = new File(getPreferencesDir(),"xliffchecker"); //$NON-NLS-1$
     FileOutputStream out = new FileOutputStream(preferences);
     out.write(string.getBytes("UTF-8")); //$NON-NLS-1$
     out.close();
     MessageBox box = new MessageBox(shell,SWT.ICON_INFORMATION|SWT.OK);
     box.setMessage(Messages.getString("XLIFFChecker.25")); //$NON-NLS-1$
     box.open();
     } catch (Exception e) {
     MessageBox box = new MessageBox(shell,SWT.ICON_ERROR|SWT.OK);
     box.setMessage(e.getMessage());
     box.open();
     }
     }*/
    public File getPreferencesDir() throws IOException {
        String directory;
        if (System.getProperty("file.separator").equals("\\")) { //$NON-NLS-1$ //$NON-NLS-2$
            // Windows
            directory = System.getenv("AppData") + "\\Maxprograms\\"; //$NON-NLS-1$ //$NON-NLS-2$
        } else if (System.getProperty("user.home").startsWith("/Users")) { //$NON-NLS-1$ //$NON-NLS-2$
            // Mac
            directory = System.getProperty("user.home") + "/Library/Preferences/Maxprograms/"; //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            // Linux
            directory = System.getProperty("user.home") + "/.maxprograms/"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        File dir = new File(directory);
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                throw new IOException(Messages.getString("XLIFFChecker.26")); //$NON-NLS-1$
            }
        }
        return dir;
    }

    public void validateFile(String file) throws Exception {
        //FileDialog fd = new FileDialog(shell,SWT.OPEN);
        //String[] extensions = {"*.xlf", "*.*"}; //$NON-NLS-1$ //$NON-NLS-2$
        //String[] names = {Messages.getString("XLIFFChecker.29"), Messages.getString("XLIFFChecker.30")}; //$NON-NLS-1$ //$NON-NLS-2$
        //fd.setFilterExtensions(extensions);
        //fd.setFilterNames(names);
        //String file = fd.open();
        if (file != null) {
            try {
                if (langParser == null) {
                    langParser = new RegistryParser();
                }
                loadFile(file);
            } catch (Exception e) {
                MessageFormat mfe = new MessageFormat(Messages.getString("XLIFFChecker.31")); //$NON-NLS-1$
                styled.append(mfe.format(new Object[]{e.getMessage()}));
                showResult(Messages.getString("XLIFFChecker.32"), true); //$NON-NLS-1$
            }
        }
    }

    private void showResult(String string, boolean error) throws Exception {
        styled.append(string.toString());
        if (error) {
            throw new Exception(styled.toString());
        }
    }

    private void loadFile(String fileName) throws MalformedURLException, IOException, SAXException, ParserConfigurationException, Exception {

        MessageFormat mf = new MessageFormat(Messages.getString("XLIFFChecker.33")); //$NON-NLS-1$
        styled.append(mf.format(new Object[]{fileName}));

        DocumentBuilderFactory parserFactory = DocumentBuilderFactory.newInstance();
        parserFactory.setNamespaceAware(true);
        String id = "http://apache.org/xml/properties/dom/document-class-name";  //$NON-NLS-1$
        Object value = "org.apache.xerces.dom.DocumentImpl";  //$NON-NLS-1$
        try {
            parserFactory.setAttribute(id, value);
        } catch (IllegalArgumentException e) {
            System.err.println("Could not set parser property.");  //$NON-NLS-1$
        }
        DocumentBuilder parser = parserFactory.newDocumentBuilder();

        CustomErrorHandler errorHandler = new CustomErrorHandler();
        parser.setErrorHandler(errorHandler);
        CustomResolver resolver = new CustomResolver("catalog/catalog.xml");  //$NON-NLS-1$
        parser.setEntityResolver(resolver);

        // parse without validating first
        document = parser.parse(new File(fileName));
        Element root = document.getDocumentElement();

        // check root node name
        if (!root.getLocalName().equals("xliff")) { //$NON-NLS-1$
            showResult(Messages.getString("XLIFFChecker.36"), true); //$NON-NLS-1$
            return;
        }

        styled.append(Messages.getString("XLIFFChecker.37")); //$NON-NLS-1$
        display.update();
        display.readAndDispatch();

        version = root.getAttribute("version"); //$NON-NLS-1$

        MessageFormat mfv = new MessageFormat(Messages.getString("XLIFFChecker.39")); //$NON-NLS-1$
        styled.append(mfv.format(new Object[]{version}));
        display.update();
        display.readAndDispatch();

        if (version.equals("1.0")) { //$NON-NLS-1$
            validateWithDTD(root, fileName);
            return;
        }
        if (version.equals("1.2")) { //$NON-NLS-1$
            xliffNamespace = "urn:oasis:names:tc:xliff:document:1.2"; //$NON-NLS-1$
        } else if (version.equals("1.1")) { //$NON-NLS-1$
            xliffNamespace = "urn:oasis:names:tc:xliff:document:1.1"; //$NON-NLS-1$
        } else {
            styled.append(Messages.getString("XLIFFChecker.45")); //$NON-NLS-1$
            showResult(Messages.getString("XLIFFChecker.46"), false); //$NON-NLS-1$
            return;
        }

        String location = root.getAttribute("xsi:schemaLocation"); //$NON-NLS-1$
        String checkedLocation = checkSchemaLocations(location, resolver);
        if (!location.equals(checkedLocation)) {
            root.setAttribute("xsi:schemaLocation", checkedLocation); //$NON-NLS-1$
            Writer writer = new Writer();
            File temp = File.createTempFile("temp", ".xlf"); //$NON-NLS-1$ //$NON-NLS-2$
            temp.deleteOnExit();
            FileOutputStream output = new FileOutputStream(temp);
            writer.setOutput(output);
            writer.write(document);
            output.close();
            styled.append(Messages.getString("XLIFFChecker.51"));	 //$NON-NLS-1$
            display.update();
            display.readAndDispatch();

            document = null;
            root = null;
            document = parser.parse(temp);
            root = document.getDocumentElement();
            styled.append(Messages.getString("XLIFFChecker.52"));	 //$NON-NLS-1$
            display.update();
            display.readAndDispatch();

            fileName = temp.getAbsolutePath();
            location = root.getAttribute("xsi:schemaLocation"); //$NON-NLS-1$
        }

        String namespace = root.getNamespaceURI();
        if (namespace == null) {
            namespace = ""; //$NON-NLS-1$
        }
        if (!namespace.equals("")) { //$NON-NLS-1$
            // check if the declared namespace matches the version of XLIFF
            if ((namespace.equals("urn:oasis:names:tc:xliff:document:1.2") && !version.equals("1.2")) || //$NON-NLS-1$ //$NON-NLS-2$
                    (namespace.equals("urn:oasis:names:tc:xliff:document:1.1") && !version.equals("1.1"))) { //$NON-NLS-1$ //$NON-NLS-2$
                styled.append(Messages.getString("XLIFFChecker.22")); //$NON-NLS-1$
                showResult(Messages.getString("XLIFFChecker.23"), false); //$NON-NLS-1$
                return;
            }
        }

        if (location.equals("") || namespace.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
            // namespace not declared
            styled.append(Messages.getString("XLIFFChecker.55")); //$NON-NLS-1$
            display.update();
            display.readAndDispatch();

            if (version.equals("1.2")) { //$NON-NLS-1$
                root.setAttribute("xmlns", xliffNamespace); //$NON-NLS-1$
                root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); //$NON-NLS-1$ //$NON-NLS-2$
                root.setAttribute("xsi:schemaLocation", "urn:oasis:names:tc:xliff:document:1.2 xliff-core-1.2-transitional.xsd"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (version.equals("1.1")) { //$NON-NLS-1$
                root.setAttribute("xmlns", "urn:oasis:names:tc:xliff:document:1.1"); //$NON-NLS-1$ //$NON-NLS-2$
                root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"); //$NON-NLS-1$ //$NON-NLS-2$
                root.setAttribute("xsi:schemaLocation", "urn:oasis:names:tc:xliff:document:1.1 xliff-core-1.1.xsd"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            styled.append(Messages.getString("XLIFFChecker.69")); //$NON-NLS-1$
            display.update();
            display.readAndDispatch();
            Writer writer = new Writer();
            File temp = File.createTempFile("temp", ".xlf"); //$NON-NLS-1$ //$NON-NLS-2$
            temp.deleteOnExit();
            FileOutputStream output = new FileOutputStream(temp);
            writer.setOutput(output);
            writer.write(document);
            output.close();
            document = null;
            root = null;
            document = parser.parse(temp);
            root = document.getDocumentElement();
            styled.append(Messages.getString("XLIFFChecker.72"));	 //$NON-NLS-1$
            display.update();
            display.readAndDispatch();
            fileName = temp.getAbsolutePath();
        }

        // reload validating
        if (version.equals("1.1")) { //$NON-NLS-1$
            styled.append(Messages.getString("XLIFFChecker.74")); //$NON-NLS-1$
        } else {
            styled.append(Messages.getString("XLIFFChecker.75")); //$NON-NLS-1$
        }
        display.update();
        display.readAndDispatch();

        // get a validating parser
        parserFactory.setFeature("http://xml.org/sax/features/validation", true); //$NON-NLS-1$
        parserFactory.setFeature("http://apache.org/xml/features/validation/schema", true); //$NON-NLS-1$
        parserFactory.setFeature("http://apache.org/xml/features/validation/dynamic", true); //$NON-NLS-1$
        parser = parserFactory.newDocumentBuilder();
        parser.setErrorHandler(errorHandler);
        parser.setEntityResolver(resolver);
        document = parser.parse(new File(fileName));
        root = document.getDocumentElement();

        // There is a bug in XLIFF 1.1 schema, duplicated in XLIFF 1.2 Transitional schema,
        // they allow any attribute in XLIFF elements.
        // Load a table of attributes for validating details that the parser ignored.
        createAttributesTable();

        if (!validateDetails(root)) {
            showResult(Messages.getString("XLIFFChecker.76"), true); //$NON-NLS-1$
            return;
        }

        if (strict && version.equals("1.2")) { //$NON-NLS-1$
            styled.append(Messages.getString("XLIFFChecker.78")); //$NON-NLS-1$
            display.update();
            display.readAndDispatch();
            try {
                // parse again using Strict schema by default
                resolver.setStrict(true);
                document = parser.parse(new File(fileName));
                showResult(Messages.getString("XLIFFChecker.79"), false); //$NON-NLS-1$
            } catch (Exception ex) {
                showResult(Messages.getString("XLIFFChecker.80"), true); //$NON-NLS-1$
            }
        }
        if (version.equals("1.1")) { //$NON-NLS-1$
            showResult(Messages.getString("XLIFFChecker.82"), false); //$NON-NLS-1$
        }
    }

    private void createAttributesTable() {
        if (attributesTable != null) {
            attributesTable = null;
        }
        attributesTable = new Hashtable<String, Set<String>>();

        Set<String> xliffSet = new HashSet<String>();
        xliffSet.add("version"); //$NON-NLS-1$
        attributesTable.put("xliff", xliffSet); //$NON-NLS-1$

        Set<String> fileSet = new HashSet<String>();
        fileSet.add("original"); //$NON-NLS-1$
        fileSet.add("source-language"); //$NON-NLS-1$
        fileSet.add("datatype"); //$NON-NLS-1$
        fileSet.add("tool"); //$NON-NLS-1$
        fileSet.add("tool-id"); //$NON-NLS-1$
        fileSet.add("date"); //$NON-NLS-1$
        fileSet.add("ts"); //$NON-NLS-1$
        fileSet.add("category"); //$NON-NLS-1$
        fileSet.add("target-language"); //$NON-NLS-1$
        fileSet.add("product-name"); //$NON-NLS-1$
        fileSet.add("product-version"); //$NON-NLS-1$
        fileSet.add("build-num"); //$NON-NLS-1$
        attributesTable.put("file", fileSet); //$NON-NLS-1$

        attributesTable.put("header", new HashSet<String>()); // this element doesn't have attributes //$NON-NLS-1$
        attributesTable.put("skl", new HashSet<String>()); // this element doesn't have attributes //$NON-NLS-1$

        Set<String> internal_fileSet = new HashSet<String>();
        internal_fileSet.add("form"); //$NON-NLS-1$
        internal_fileSet.add("crc"); //$NON-NLS-1$
        attributesTable.put("internal-file", internal_fileSet); //$NON-NLS-1$

        Set<String> external_fileSet = new HashSet<String>();
        external_fileSet.add("href"); //$NON-NLS-1$
        external_fileSet.add("uid"); //$NON-NLS-1$
        external_fileSet.add("crc"); //$NON-NLS-1$
        attributesTable.put("external-file", external_fileSet); //$NON-NLS-1$

        attributesTable.put("glossary", new HashSet<String>()); // this element doesn't have attributes //$NON-NLS-1$
        attributesTable.put("reference", new HashSet<String>()); // this element doesn't have attributes //$NON-NLS-1$

        Set<String> noteSet = new HashSet<String>();
        noteSet.add("from"); //$NON-NLS-1$
        noteSet.add("priority"); //$NON-NLS-1$
        noteSet.add("annotates"); //$NON-NLS-1$
        attributesTable.put("note", noteSet); //$NON-NLS-1$

        attributesTable.put("phase-group", new HashSet<String>()); // this element doesn't have attributes //$NON-NLS-1$

        Set<String> phaseSet = new HashSet<String>();
        phaseSet.add("phase-name"); //$NON-NLS-1$
        phaseSet.add("process-name"); //$NON-NLS-1$
        phaseSet.add("company-name"); //$NON-NLS-1$
        phaseSet.add("tool"); //$NON-NLS-1$
        phaseSet.add("tool-id"); //$NON-NLS-1$
        phaseSet.add("date"); //$NON-NLS-1$
        phaseSet.add("job-id"); //$NON-NLS-1$
        phaseSet.add("contact-name"); //$NON-NLS-1$
        phaseSet.add("contact-email"); //$NON-NLS-1$
        phaseSet.add("contact-phone"); //$NON-NLS-1$
        attributesTable.put("phase", phaseSet); //$NON-NLS-1$

        Set<String> toolSet = new HashSet<String>();
        toolSet.add("tool-id"); //$NON-NLS-1$
        toolSet.add("tool-name"); //$NON-NLS-1$
        toolSet.add("tool-version"); //$NON-NLS-1$
        toolSet.add("tool-company"); //$NON-NLS-1$
        attributesTable.put("tool", toolSet); //$NON-NLS-1$

        Set<String> count_groupSet = new HashSet<String>();
        count_groupSet.add("name"); //$NON-NLS-1$
        attributesTable.put("count-group", count_groupSet); //$NON-NLS-1$

        Set<String> countSet = new HashSet<String>();
        countSet.add("count-type"); //$NON-NLS-1$
        countSet.add("phase-name"); //$NON-NLS-1$
        countSet.add("unit"); //$NON-NLS-1$
        attributesTable.put("count", countSet); //$NON-NLS-1$

        Set<String> context_groupSet = new HashSet<String>();
        context_groupSet.add("crc"); //$NON-NLS-1$
        context_groupSet.add("name"); //$NON-NLS-1$
        context_groupSet.add("purpose"); //$NON-NLS-1$
        attributesTable.put("context-group", context_groupSet); //$NON-NLS-1$

        Set<String> contextSet = new HashSet<String>();
        contextSet.add("context-type"); //$NON-NLS-1$
        contextSet.add("match-mandatory"); //$NON-NLS-1$
        contextSet.add("crc"); //$NON-NLS-1$
        attributesTable.put("context", contextSet); //$NON-NLS-1$

        Set<String> prop_groupSet = new HashSet<String>();
        prop_groupSet.add("name"); //$NON-NLS-1$
        attributesTable.put("prop-group", prop_groupSet); //$NON-NLS-1$

        Set<String> propSet = new HashSet<String>();
        propSet.add("prop-type"); //$NON-NLS-1$
        attributesTable.put("prop", propSet); //$NON-NLS-1$

        attributesTable.put("body", new HashSet<String>()); // this element doesn't have attributes //$NON-NLS-1$

        Set<String> groupSet = new HashSet<String>();
        groupSet.add("id"); //$NON-NLS-1$
        groupSet.add("datatype"); //$NON-NLS-1$
        groupSet.add("ts"); //$NON-NLS-1$
        groupSet.add("restype"); //$NON-NLS-1$
        groupSet.add("resname"); //$NON-NLS-1$
        groupSet.add("extradata"); //$NON-NLS-1$
        groupSet.add("help-id"); //$NON-NLS-1$
        groupSet.add("menu"); //$NON-NLS-1$
        groupSet.add("menu-option"); //$NON-NLS-1$
        groupSet.add("menu-name"); //$NON-NLS-1$
        groupSet.add("coord"); //$NON-NLS-1$
        groupSet.add("font"); //$NON-NLS-1$
        groupSet.add("css-style"); //$NON-NLS-1$
        groupSet.add("style"); //$NON-NLS-1$
        groupSet.add("exstyle"); //$NON-NLS-1$
        groupSet.add("extype"); //$NON-NLS-1$
        groupSet.add("translate"); //$NON-NLS-1$
        groupSet.add("reformat"); //$NON-NLS-1$
        groupSet.add("maxbytes"); //$NON-NLS-1$
        groupSet.add("minbytes"); //$NON-NLS-1$
        groupSet.add("size-unit"); //$NON-NLS-1$
        groupSet.add("maxheight"); //$NON-NLS-1$
        groupSet.add("minheight"); //$NON-NLS-1$
        groupSet.add("maxwidth"); //$NON-NLS-1$
        groupSet.add("minwidth"); //$NON-NLS-1$
        groupSet.add("charclass"); //$NON-NLS-1$
        if (!version.equals("1.1")) { //$NON-NLS-1$
            groupSet.add("merged-trans"); //$NON-NLS-1$
        }
        attributesTable.put("group", groupSet); //$NON-NLS-1$

        Set<String> trans_unitSet = new HashSet<String>();
        trans_unitSet.add("id"); //$NON-NLS-1$
        trans_unitSet.add("approved"); //$NON-NLS-1$
        trans_unitSet.add("translate"); //$NON-NLS-1$
        trans_unitSet.add("reformat"); //$NON-NLS-1$
        trans_unitSet.add("datatype"); //$NON-NLS-1$
        trans_unitSet.add("ts"); //$NON-NLS-1$
        trans_unitSet.add("phase-name"); //$NON-NLS-1$
        trans_unitSet.add("restype"); //$NON-NLS-1$
        trans_unitSet.add("resname"); //$NON-NLS-1$
        trans_unitSet.add("extradata"); //$NON-NLS-1$
        trans_unitSet.add("help-id"); //$NON-NLS-1$
        trans_unitSet.add("menu"); //$NON-NLS-1$
        trans_unitSet.add("menu-option"); //$NON-NLS-1$
        trans_unitSet.add("menu-name"); //$NON-NLS-1$
        trans_unitSet.add("coord"); //$NON-NLS-1$
        trans_unitSet.add("font"); //$NON-NLS-1$
        trans_unitSet.add("css-style"); //$NON-NLS-1$
        trans_unitSet.add("style"); //$NON-NLS-1$
        trans_unitSet.add("exstyle"); //$NON-NLS-1$
        trans_unitSet.add("extype"); //$NON-NLS-1$
        trans_unitSet.add("maxbytes"); //$NON-NLS-1$
        trans_unitSet.add("minbytes"); //$NON-NLS-1$
        trans_unitSet.add("size-unit"); //$NON-NLS-1$
        trans_unitSet.add("maxheight"); //$NON-NLS-1$
        trans_unitSet.add("minheight"); //$NON-NLS-1$
        trans_unitSet.add("maxwidth"); //$NON-NLS-1$
        trans_unitSet.add("minwidth"); //$NON-NLS-1$
        trans_unitSet.add("charclass"); //$NON-NLS-1$
        attributesTable.put("trans-unit", trans_unitSet); //$NON-NLS-1$

        Set<String> sourceSet = new HashSet<String>();
        sourceSet.add("ts"); //$NON-NLS-1$
        attributesTable.put("source", sourceSet); //$NON-NLS-1$

        Set<String> targetSet = new HashSet<String>();
        targetSet.add("state"); //$NON-NLS-1$
        targetSet.add("state-qualifier"); //$NON-NLS-1$
        targetSet.add("phase-name"); //$NON-NLS-1$
        targetSet.add("ts"); //$NON-NLS-1$
        targetSet.add("restype"); //$NON-NLS-1$
        targetSet.add("resname"); //$NON-NLS-1$
        targetSet.add("coord"); //$NON-NLS-1$
        targetSet.add("font"); //$NON-NLS-1$
        targetSet.add("css-style"); //$NON-NLS-1$
        targetSet.add("style"); //$NON-NLS-1$
        targetSet.add("exstyle"); //$NON-NLS-1$
        if (!version.equals("1.1")) { //$NON-NLS-1$
            targetSet.add("equiv-trans"); //$NON-NLS-1$
        }
        attributesTable.put("target", targetSet); //$NON-NLS-1$

        Set<String> alt_transSet = new HashSet<String>();
        if (!version.equals("1.1")) { //$NON-NLS-1$
            alt_transSet.add("mid"); //$NON-NLS-1$
        }
        alt_transSet.add("match-quality"); //$NON-NLS-1$
        alt_transSet.add("tool"); //$NON-NLS-1$
        alt_transSet.add("tool-id"); //$NON-NLS-1$
        alt_transSet.add("crc"); //$NON-NLS-1$
        alt_transSet.add("datatype"); //$NON-NLS-1$
        alt_transSet.add("ts"); //$NON-NLS-1$
        alt_transSet.add("restype"); //$NON-NLS-1$
        alt_transSet.add("resname"); //$NON-NLS-1$
        alt_transSet.add("extradata"); //$NON-NLS-1$
        alt_transSet.add("help-id"); //$NON-NLS-1$
        alt_transSet.add("menu"); //$NON-NLS-1$
        alt_transSet.add("menu-option"); //$NON-NLS-1$
        alt_transSet.add("menu-name"); //$NON-NLS-1$
        alt_transSet.add("coord"); //$NON-NLS-1$
        alt_transSet.add("font"); //$NON-NLS-1$
        alt_transSet.add("css-style"); //$NON-NLS-1$
        alt_transSet.add("style"); //$NON-NLS-1$
        alt_transSet.add("exstyle"); //$NON-NLS-1$
        alt_transSet.add("extype"); //$NON-NLS-1$
        alt_transSet.add("origin"); //$NON-NLS-1$
        if (!version.equals("1.1")) { //$NON-NLS-1$
            alt_transSet.add("phase-name"); //$NON-NLS-1$
            alt_transSet.add("alttranstype"); //$NON-NLS-1$
        }
        attributesTable.put("alt-trans", alt_transSet); //$NON-NLS-1$

        Set<String> bin_unitSet = new HashSet<String>();
        bin_unitSet.add("id"); //$NON-NLS-1$
        bin_unitSet.add("mime-type"); //$NON-NLS-1$
        bin_unitSet.add("approved"); //$NON-NLS-1$
        bin_unitSet.add("translate"); //$NON-NLS-1$
        bin_unitSet.add("reformat"); //$NON-NLS-1$
        bin_unitSet.add("ts"); //$NON-NLS-1$
        bin_unitSet.add("phase-name"); //$NON-NLS-1$
        bin_unitSet.add("restype"); //$NON-NLS-1$
        bin_unitSet.add("resname"); //$NON-NLS-1$
        attributesTable.put("bin-unit", bin_unitSet); //$NON-NLS-1$

        Set<String> bin_sourceSet = new HashSet<String>();
        bin_sourceSet.add("ts"); //$NON-NLS-1$
        attributesTable.put("bin-source", bin_sourceSet); //$NON-NLS-1$

        Set<String> bin_targetSet = new HashSet<String>();
        bin_targetSet.add("mime-type"); //$NON-NLS-1$
        bin_targetSet.add("ts"); //$NON-NLS-1$
        bin_targetSet.add("state"); //$NON-NLS-1$
        bin_targetSet.add("phase-name"); //$NON-NLS-1$
        bin_targetSet.add("restype"); //$NON-NLS-1$
        bin_targetSet.add("resname"); //$NON-NLS-1$
        if (!version.equals("1.1")) { //$NON-NLS-1$
            bin_targetSet.add("state-qualifier"); //$NON-NLS-1$
        }
        attributesTable.put("bin-target", bin_targetSet); //$NON-NLS-1$

        if (!version.equals("1.1")) { //$NON-NLS-1$
            Set<String> seg_sourceSet = new HashSet<String>();
            seg_sourceSet.add("ts"); //$NON-NLS-1$
            attributesTable.put("seg-source", seg_sourceSet); //$NON-NLS-1$
        }

        Set<String> gSet = new HashSet<String>();
        gSet.add("id"); //$NON-NLS-1$
        gSet.add("ctype"); //$NON-NLS-1$
        gSet.add("ts"); //$NON-NLS-1$
        gSet.add("clone"); //$NON-NLS-1$
        gSet.add("xid"); //$NON-NLS-1$
        if (!version.equals("1.1")) { //$NON-NLS-1$
            gSet.add("equiv-text"); //$NON-NLS-1$
        }
        attributesTable.put("g", gSet); //$NON-NLS-1$

        Set<String> xSet = new HashSet<String>();
        xSet.add("id"); //$NON-NLS-1$
        xSet.add("ctype"); //$NON-NLS-1$
        xSet.add("ts"); //$NON-NLS-1$
        xSet.add("clone"); //$NON-NLS-1$
        xSet.add("xid"); //$NON-NLS-1$
        if (!version.equals("1.1")) { //$NON-NLS-1$
            xSet.add("equiv-text"); //$NON-NLS-1$
        }
        attributesTable.put("x", xSet); //$NON-NLS-1$

        Set<String> bxSet = new HashSet<String>();
        bxSet.add("id"); //$NON-NLS-1$
        bxSet.add("rid"); //$NON-NLS-1$
        bxSet.add("ctype"); //$NON-NLS-1$
        bxSet.add("ts"); //$NON-NLS-1$
        bxSet.add("clone"); //$NON-NLS-1$
        bxSet.add("xid"); //$NON-NLS-1$
        if (!version.equals("1.1")) { //$NON-NLS-1$
            bxSet.add("equiv-text"); //$NON-NLS-1$
        }
        attributesTable.put("bx", bxSet); //$NON-NLS-1$

        Set<String> exSet = new HashSet<String>();
        exSet.add("id"); //$NON-NLS-1$
        exSet.add("rid"); //$NON-NLS-1$
        exSet.add("ts"); //$NON-NLS-1$
        exSet.add("xid"); //$NON-NLS-1$
        if (!version.equals("1.1")) { //$NON-NLS-1$
            exSet.add("equiv-text"); //$NON-NLS-1$
        }
        attributesTable.put("ex", exSet); //$NON-NLS-1$

        Set<String> phSet = new HashSet<String>();
        phSet.add("id"); //$NON-NLS-1$
        phSet.add("ctype"); //$NON-NLS-1$
        phSet.add("ts"); //$NON-NLS-1$
        phSet.add("crc"); //$NON-NLS-1$
        phSet.add("assoc"); //$NON-NLS-1$
        phSet.add("xid"); //$NON-NLS-1$
        if (!version.equals("1.1")) { //$NON-NLS-1$
            phSet.add("equiv-text"); //$NON-NLS-1$
        }
        attributesTable.put("ph", phSet); //$NON-NLS-1$

        Set<String> bptSet = new HashSet<String>();
        bptSet.add("id"); //$NON-NLS-1$
        bptSet.add("rid"); //$NON-NLS-1$
        bptSet.add("ctype"); //$NON-NLS-1$
        bptSet.add("ts"); //$NON-NLS-1$
        bptSet.add("crc"); //$NON-NLS-1$
        bptSet.add("xid"); //$NON-NLS-1$
        if (!version.equals("1.1")) { //$NON-NLS-1$
            bptSet.add("equiv-text"); //$NON-NLS-1$
        }
        attributesTable.put("bpt", bptSet); //$NON-NLS-1$

        Set<String> eptSet = new HashSet<String>();
        eptSet.add("id"); //$NON-NLS-1$
        eptSet.add("rid"); //$NON-NLS-1$
        eptSet.add("ts"); //$NON-NLS-1$
        eptSet.add("crc"); //$NON-NLS-1$
        eptSet.add("xid"); //$NON-NLS-1$
        if (!version.equals("1.1")) { //$NON-NLS-1$
            eptSet.add("equiv-text"); //$NON-NLS-1$
        }
        attributesTable.put("ept", eptSet); //$NON-NLS-1$

        Set<String> itSet = new HashSet<String>();
        itSet.add("id"); //$NON-NLS-1$
        itSet.add("pos"); //$NON-NLS-1$
        itSet.add("rid"); //$NON-NLS-1$
        itSet.add("ctype"); //$NON-NLS-1$
        itSet.add("ts"); //$NON-NLS-1$
        itSet.add("crc"); //$NON-NLS-1$
        itSet.add("xid"); //$NON-NLS-1$
        if (!version.equals("1.1")) { //$NON-NLS-1$
            itSet.add("equiv-text"); //$NON-NLS-1$
        }
        attributesTable.put("it", itSet); //$NON-NLS-1$

        Set<String> subSet = new HashSet<String>();
        subSet.add("datatype"); //$NON-NLS-1$
        subSet.add("ctype"); //$NON-NLS-1$
        subSet.add("xid"); //$NON-NLS-1$
        attributesTable.put("sub", subSet); //$NON-NLS-1$

        Set<String> mrkSet = new HashSet<String>();
        mrkSet.add("mtype"); //$NON-NLS-1$
        mrkSet.add("mid"); //$NON-NLS-1$
        mrkSet.add("ts"); //$NON-NLS-1$
        mrkSet.add("comment"); //$NON-NLS-1$
        attributesTable.put("mrk", mrkSet); //$NON-NLS-1$
    }

    private String checkSchemaLocations(String location, CustomResolver resolver) {
        if (location.equals("")) { //$NON-NLS-1$
            return ""; //$NON-NLS-1$
        }
        // normalize spaces
        String normalized = location.replaceAll("(\\s)+", " "); //$NON-NLS-1$ //$NON-NLS-2$
        String[] parts = normalized.trim().split("\\s"); //$NON-NLS-1$
        if (parts.length != 0 && parts.length % 2 == 0) {
            // we have pairs, as expected
            String newLocations = ""; //$NON-NLS-1$
            boolean notFound = false;
            for (int i = 0; i < parts.length / 2; i++) {
                String namespace = parts[i * 2];
                String url = parts[i * 2 + 1];
                try {
                    if (resolver.resolveEntity("", url) != null) { //$NON-NLS-1$
                        newLocations = namespace + " " + url; //$NON-NLS-1$
                    } else {
                        MessageFormat mf = new MessageFormat(Messages.getString("XLIFFChecker.91")); //$NON-NLS-1$
                        styled.append(mf.format(new Object[]{namespace, url}));
                        display.update();
                        display.readAndDispatch();
                        notFound = true;
                        // try to fix known locations
                        if (namespace.equals("urn:oasis:names:tc:xliff:document:1.1")) { //$NON-NLS-1$
                            url = "xliff-core-1.1.xsd"; //$NON-NLS-1$
                            newLocations = namespace + " " + url; //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        if (namespace.equals("urn:oasis:names:tc:xliff:document:1.2")) { //$NON-NLS-1$
                            url = "xliff-core-1.2-transitional.xsd"; //$NON-NLS-1$
                            newLocations = namespace + " " + url; //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    }
                } catch (Exception e) {
                    MessageFormat mf = new MessageFormat(Messages.getString("XLIFFChecker.100")); //$NON-NLS-1$
                    styled.append(mf.format(new Object[]{namespace, url}));
                    display.update();
                    display.readAndDispatch();
                    notFound = true;
                    // try to fix known locations
                    if (namespace.equals("urn:oasis:names:tc:xliff:document:1.1")) { //$NON-NLS-1$
                        url = "xliff-core-1.1.xsd"; //$NON-NLS-1$
                        newLocations = newLocations + " " + namespace + " " + url; //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    if (namespace.equals("urn:oasis:names:tc:xliff:document:1.2")) { //$NON-NLS-1$
                        url = "xliff-core-1.2-transitional.xsd"; //$NON-NLS-1$
                        newLocations = newLocations + " " + namespace + " " + url; //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            }
            if (notFound) {
                return newLocations.trim();
            }
        }
        // no changes or not properly paired
        return location;
    }

    private boolean validateDetails(Element root) {
        styled.append(Messages.getString("XLIFFChecker.109")); //$NON-NLS-1$
        display.update();
        display.readAndDispatch();
        return recurse(root);
    }

    private boolean recurse(Element e) {

        // ignore non-XLIFF elements
        if (e.getNamespaceURI() != null) {
            if (!e.getNamespaceURI().equals(xliffNamespace)) {
                if (!hasExtensions) {
                    styled.append(Messages.getString("XLIFFChecker.119")); //$NON-NLS-1$
                    display.update();
                    display.readAndDispatch();
                    hasExtensions = true;
                }
                return true;
            }
        }

        if (!version.equals("1.0")) { //$NON-NLS-1$
            // validate the attributes (the parser can't do it due to bugs in the schemas
            NamedNodeMap atts = e.getAttributes();
            for (int i = 0; i < atts.getLength(); i++) {
                Node att = atts.item(i);
                String name = att.getLocalName();
                if (name.equals("xmlns")) { //$NON-NLS-1$
                    // attribute from XML standard
                    continue;
                }
                if (att.getNamespaceURI() != null) {
                    // declares a namespace
                    if (!att.getNamespaceURI().equals(xliffNamespace)) {
                        // attribute from another namespace
                        continue;
                    }
                }
                if (!attributesTable.get(e.getLocalName()).contains(att.getLocalName())) {
                    MessageFormat mf = new MessageFormat(Messages.getString("XLIFFChecker.350")); //$NON-NLS-1$
                    styled.append(mf.format(new Object[]{e.getNodeName(), att.getNodeName()}));
                    return false;
                }
            }
        }
        // "date" attributes must be in ISO 8601 format
        if (e.getLocalName().equals("file") || e.getLocalName().equals("phase")) { //$NON-NLS-1$ //$NON-NLS-2$
            String date = e.getAttribute("date"); //$NON-NLS-1$
            if (!date.equals("")) { //$NON-NLS-1$
                if (!checkDate(date)) {
                    MessageFormat mf = new MessageFormat(Messages.getString("XLIFFChecker.124")); //$NON-NLS-1$
                    styled.append(mf.format(new Object[]{date}));
                    return false;
                }
            }
        }

        // external files should be resolvable resources
        if (e.getLocalName().equals("external-file")) { //$NON-NLS-1$
            if (!checkURL(e.getAttribute("href"))) { //$NON-NLS-1$
                MessageFormat mf = new MessageFormat(Messages.getString("XLIFFChecker.127")); //$NON-NLS-1$
                styled.append(mf.format(new Object[]{e.getAttribute("href")})); //$NON-NLS-1$
                return false;
            }
        }

        // source file should be resolvable resources
        if (e.getLocalName().equals("file")) { //$NON-NLS-1$
            if (!checkURL(e.getAttribute("original"))) { //$NON-NLS-1$
                MessageFormat mf = new MessageFormat(Messages.getString("XLIFFChecker.131")); //$NON-NLS-1$
                styled.append(mf.format(new Object[]{e.getAttribute("original")})); //$NON-NLS-1$
                return false;
            }
        }

        // language codes should be valid ISO codes
        if (e.getLocalName().equals("xliff") || e.getLocalName().equals("note") || //$NON-NLS-1$ //$NON-NLS-2$
                e.getLocalName().equals("prop") || e.getLocalName().equals("source") || //$NON-NLS-1$ //$NON-NLS-2$
                e.getLocalName().equals("target") || e.getLocalName().equals("alt-trans") || //$NON-NLS-1$ //$NON-NLS-2$
                e.getLocalName().equals("seg-source")) { //$NON-NLS-1$
            String lang = e.getAttribute("xml:lang");  //$NON-NLS-1$
            if (!lang.equals("")) { //$NON-NLS-1$
                if (!checkLanguage(lang)) {
                    MessageFormat mf = new MessageFormat(Messages.getString("XLIFFChecker.142")); //$NON-NLS-1$
                    styled.append(mf.format(new Object[]{lang}));
                    return false;
                }
            }
        }

        if (e.getLocalName().equals("file")) { //$NON-NLS-1$
            // create tables to make sure that "id" attributes are unique within the <file> 
            ids = null;
            ids = new Hashtable<String, String>();
            groupIds = null;
            groupIds = new Hashtable<String, String>();

            // create tables to check that <it> tags have both start and end positions 
            srcItTable = null;
            srcItTable = new Hashtable<String, String>();
            tgtItTable = null;
            tgtItTable = new Hashtable<String, String>();

            // create table to check if "xid" points to valid <trans-unit>
            xids = null;
            xids = new Hashtable<String, String>();

            // check <phase> and <tool> elements
            phasesTable = null;
            phasesTable = new Hashtable<String, String>();

            toolsTable = null;
            toolsTable = new Hashtable<String, String>();

            // check language codes used 
            sourceLanguage = e.getAttribute("source-language");  //$NON-NLS-1$
            if (!checkLanguage(sourceLanguage)) {
                MessageFormat mf = new MessageFormat(Messages.getString("XLIFFChecker.145")); //$NON-NLS-1$
                styled.append(mf.format(new Object[]{sourceLanguage}));
                return false;
            }
            targetLanguage = e.getAttribute("target-language");  //$NON-NLS-1$
            if (!targetLanguage.equals("")) { //$NON-NLS-1$
                if (!checkLanguage(targetLanguage)) {
                    MessageFormat mf = new MessageFormat(Messages.getString("XLIFFChecker.148")); //$NON-NLS-1$
                    styled.append(mf.format(new Object[]{targetLanguage}));
                    return false;
                }
            }
        }

        if (e.getLocalName().equals("source")) { //$NON-NLS-1$
            inSource = true;
        }

        // store phase name
        if (e.getLocalName().equals("phase")) { //$NON-NLS-1$
            String name = e.getAttribute("phase-name"); //$NON-NLS-1$
            if (!phasesTable.containsKey(name)) {
                phasesTable.put(name, ""); //$NON-NLS-1$
            } else {
                MessageFormat mf = new MessageFormat(Messages.getString("XLIFFChecker.152")); //$NON-NLS-1$
                styled.append(mf.format(new Object[]{name}));
                return false;
            }
        }

        // store tool-id
        if (e.getLocalName().equals("tool")) { //$NON-NLS-1$
            String id = e.getAttribute("tool-id"); //$NON-NLS-1$
            if (!toolsTable.containsKey(id)) {
                toolsTable.put(id, ""); //$NON-NLS-1$
            } else {
                MessageFormat mf = new MessageFormat(Messages.getString("XLIFFChecker.156")); //$NON-NLS-1$
                styled.append(mf.format(new Object[]{id}));
                return false;
            }
        }

        if (e.getLocalName().equals("alt-trans")) { //$NON-NLS-1$
            // language codes in <alt-trans> are independent from those declared in <file>
            inAltTrans = true;

            // check for valid segment reference
            String mid = e.getAttribute("mid"); //$NON-NLS-1$
            if (!mid.equals("")) { //$NON-NLS-1$
                if (!midTable.containsKey(mid)) {
                    styled.append(Messages.getString("XLIFFChecker.160")); //$NON-NLS-1$
                    return false;
                }
            }

            // check for declared <tool>
            String tool = e.getAttribute("tool-id"); //$NON-NLS-1$
            if (!tool.equals("")) { //$NON-NLS-1$
                if (!toolsTable.containsKey(tool)) {
                    styled.append(Messages.getString("XLIFFChecker.163")); //$NON-NLS-1$
                    return false;
                }
            }
            // create tables to check if <it> tags are duplicated 
            altSrcItTable = null;
            altSrcItTable = new Hashtable<String, String>();
            altTgtItTable = null;
            altTgtItTable = new Hashtable<String, String>();
        }

        // validate "phase-name" attribute
        if (e.getLocalName().equals("count") || //$NON-NLS-1$
                e.getLocalName().equals("trans-unit") || //$NON-NLS-1$
                e.getLocalName().equals("bin-unit") || //$NON-NLS-1$
                e.getLocalName().equals("target") || //$NON-NLS-1$
                e.getLocalName().equals("bin-target") || //$NON-NLS-1$
                e.getLocalName().equals("alt-trans")) //$NON-NLS-1$
        {
            String phase = e.getAttribute("phase-name"); //$NON-NLS-1$
            if (!phase.equals("")) { //$NON-NLS-1$
                if (!phasesTable.containsKey(phase)) {
                    styled.append(Messages.getString("XLIFFChecker.172")); //$NON-NLS-1$
                    return false;
                }
            }
        }

        // check language code in <source> and <target>
        if (e.getLocalName().equals("source") && !inAltTrans) { //$NON-NLS-1$
            String lang = e.getAttribute("xml:lang"); //$NON-NLS-1$
            if (!lang.equals("") && !lang.equalsIgnoreCase(sourceLanguage)) { //$NON-NLS-1$
                MessageFormat mf = new MessageFormat(Messages.getString("XLIFFChecker.176")); //$NON-NLS-1$
                styled.append(mf.format(new Object[]{lang}));
                return false;
            }
        }
        if (e.getLocalName().equals("target") && !inAltTrans) { //$NON-NLS-1$
            String lang = e.getAttribute("xml:lang"); //$NON-NLS-1$
            if (targetLanguage.equals("") && lang.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
                // Missing target language code
                // bad practice, but legal
                // Check with XLIFF TC what to do in the future
            }
            if (!targetLanguage.equals("")) { //$NON-NLS-1$
                if (!lang.equals("") && !lang.equalsIgnoreCase(targetLanguage)) { //$NON-NLS-1$
                    MessageFormat mf = new MessageFormat(Messages.getString("XLIFFChecker.183")); //$NON-NLS-1$
                    styled.append(mf.format(new Object[]{lang}));
                    return false;
                }
            }
        }

        // check for unique "id" in <trans-unit> and <bin-unit>
        if (version.equals("1.2") && (e.getLocalName().equals("trans-unit") || e.getLocalName().equals("bin-unit"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            String id = e.getAttribute("id"); //$NON-NLS-1$
            if (!ids.containsKey(id)) {
                ids.put(id, ""); //$NON-NLS-1$
            } else {
                MessageFormat mf = new MessageFormat(Messages.getString("XLIFFChecker.188")); //$NON-NLS-1$
                styled.append(mf.format(new Object[]{id}));
                return false;
            }
        }

        // initialize table for checking "mid" attribute in <alt-trans>
        if (e.getLocalName().equals("trans-unit")) { //$NON-NLS-1$
            midTable = null;
            midTable = new Hashtable<String, String>();
        }

        if (e.getLocalName().equals("seg-source")) { //$NON-NLS-1$
            // entering <seg-source>
            inSegSource = true;
        }

        // check segment ids
        if (e.getLocalName().equals("mrk") && e.getAttribute("mtype").equals("seg")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            String mid = e.getAttribute("mid"); //$NON-NLS-1$
            if (inSegSource) {
                if (!midTable.containsKey(mid)) {
                    midTable.put(mid, ""); //$NON-NLS-1$
                } else {
                    MessageFormat mf = new MessageFormat(Messages.getString("XLIFFChecker.196")); //$NON-NLS-1$
                    styled.append(mf.format(new Object[]{mid}));
                    return false;
                }
            } else {
                // in <target>
                if (!midTable.containsKey(mid)) {
                    styled.append(Messages.getString("XLIFFChecker.197")); //$NON-NLS-1$
                    return false;
                }
            }
        }

        // check for unique "id" in <group>
        if (e.getLocalName().equals("group") && version.equals("1.2")) { //$NON-NLS-1$ //$NON-NLS-2$
            String id = e.getAttribute("id"); //$NON-NLS-1$
            if (!id.equals("")) { //$NON-NLS-1$
                if (!groupIds.containsKey(id)) {
                    groupIds.put(id, ""); //$NON-NLS-1$
                } else {
                    MessageFormat mf = new MessageFormat(Messages.getString("XLIFFChecker.202")); //$NON-NLS-1$
                    styled.append(mf.format(new Object[]{id}));
                    return false;
                }
            }
        }

        // initialize tables for checking matched pairs of inline elements
        if (e.getLocalName().equals("source") || //$NON-NLS-1$
                e.getLocalName().equals("seg-source") || //$NON-NLS-1$
                e.getLocalName().equals("target")) //$NON-NLS-1$
        {
            bxTable = null;
            bxTable = new Hashtable<String, String>();
            exTable = null;
            exTable = new Hashtable<String, String>();
            bptTable = null;
            bptTable = new Hashtable<String, String>();
            eptTable = null;
            eptTable = new Hashtable<String, String>();
            inlineIds = null;
            inlineIds = new Hashtable<String, String>();
        }

        // check for unique id at <source>, <seg-source> and <target> level
        if (e.getLocalName().equals("bx") || //$NON-NLS-1$
                e.getLocalName().equals("ex") || //$NON-NLS-1$
                e.getLocalName().equals("bpt") || //$NON-NLS-1$
                e.getLocalName().equals("ept") || //$NON-NLS-1$
                e.getLocalName().equals("ph")) //$NON-NLS-1$
        {
            String id = e.getAttribute("id"); //$NON-NLS-1$
            if (!inlineIds.contains(e.getLocalName() + id)) {
                inlineIds.put(e.getLocalName() + id, ""); //$NON-NLS-1$
            } else {
                MessageFormat mf = new MessageFormat(Messages.getString("XLIFFChecker.213")); //$NON-NLS-1$
                styled.append(mf.format(new Object[]{e.getLocalName()}));
                return false;
            }
        }

        // check for paired <it> tags in <file>
        if (e.getLocalName().equals("it") && !inAltTrans) { //$NON-NLS-1$
            String id = e.getAttribute("id"); //$NON-NLS-1$
            String pos = e.getAttribute("pos"); //$NON-NLS-1$
            if (inSource) {
                if (!srcItTable.containsKey(id)) {
                    srcItTable.put(id, pos);
                } else {
                    if (!srcItTable.get(id).equals(pos)) {
                        // matched
                        srcItTable.remove(id);
                    } else {
                        // duplicated
                        styled.append(Messages.getString("XLIFFChecker.217")); //$NON-NLS-1$
                        return false;
                    }
                }
            } else {
                if (!tgtItTable.containsKey(id)) {
                    tgtItTable.put(id, pos);
                } else {
                    if (!tgtItTable.get(id).equals(pos)) {
                        // matched
                        tgtItTable.remove(id);
                    } else {
                        // duplicated
                        styled.append(Messages.getString("XLIFFChecker.217")); //$NON-NLS-1$
                        return false;
                    }
                }
            }
        }
        // check for duplicated <it> tags in <alt-trans>
        if (e.getLocalName().equals("it") && inAltTrans) { //$NON-NLS-1$
            String id = e.getAttribute("id"); //$NON-NLS-1$
            String pos = e.getAttribute("pos"); //$NON-NLS-1$
            if (inSource) {
                if (!altSrcItTable.containsKey(id)) {
                    altSrcItTable.put(id, pos);
                } else {
                    if (altSrcItTable.get(id).equals(pos)) {
                        // duplicated
                        styled.append(Messages.getString("XLIFFChecker.217")); //$NON-NLS-1$
                        return false;
                    }
                }
            } else {
                if (!altTgtItTable.containsKey(id)) {
                    altTgtItTable.put(id, pos);
                } else {
                    if (altTgtItTable.get(id).equals(pos)) {
                        // duplicated
                        styled.append(Messages.getString("XLIFFChecker.217")); //$NON-NLS-1$
                        return false;
                    }
                }
            }
        }
        // check if "xid" attribute points to a valid <trans-unit> or <bin-unit>
        if (e.getLocalName().equals("ph") //$NON-NLS-1$
                || e.getLocalName().equals("it") //$NON-NLS-1$
                || e.getLocalName().equals("sub") //$NON-NLS-1$
                || e.getLocalName().equals("bx") //$NON-NLS-1$
                || e.getLocalName().equals("ex") //$NON-NLS-1$
                || e.getLocalName().equals("bpt") //$NON-NLS-1$
                || e.getLocalName().equals("ept") //$NON-NLS-1$
                || e.getLocalName().equals("g") //$NON-NLS-1$
                || e.getLocalName().equals("x")) //$NON-NLS-1$
        {
            String xid = e.getAttribute("xid"); //$NON-NLS-1$
            if (!xid.equals("")) { //$NON-NLS-1$
                xids.put(xid, ""); //$NON-NLS-1$
            }
        }

        if (e.getLocalName().equals("bx")) { //$NON-NLS-1$
            String id = e.getAttribute("rid"); //$NON-NLS-1$
            if (id.equals("")) { //$NON-NLS-1$
                id = e.getAttribute("id"); //$NON-NLS-1$
            }
            if (id.equals("")) { //$NON-NLS-1$
                styled.append(Messages.getString("XLIFFChecker.223")); //$NON-NLS-1$
                return false;
            }
            bxTable.put(id, ""); //$NON-NLS-1$
        }
        if (e.getLocalName().equals("ex")) { //$NON-NLS-1$
            String id = e.getAttribute("rid"); //$NON-NLS-1$
            if (id.equals("")) { //$NON-NLS-1$
                id = e.getAttribute("id"); //$NON-NLS-1$
            }
            if (id.equals("")) { //$NON-NLS-1$
                styled.append(Messages.getString("XLIFFChecker.230")); //$NON-NLS-1$
                return false;
            }
            exTable.put(id, ""); //$NON-NLS-1$
        }
        if (e.getLocalName().equals("bpt")) { //$NON-NLS-1$
            String id = e.getAttribute("rid"); //$NON-NLS-1$
            if (id.equals("")) { //$NON-NLS-1$
                id = e.getAttribute("id"); //$NON-NLS-1$
            }
            if (id.equals("")) { //$NON-NLS-1$
                styled.append(Messages.getString("XLIFFChecker.237")); //$NON-NLS-1$
                return false;
            }
            bptTable.put(id, ""); //$NON-NLS-1$
        }
        if (e.getLocalName().equals("ept")) { //$NON-NLS-1$
            String id = e.getAttribute("rid"); //$NON-NLS-1$
            if (id.equals("")) { //$NON-NLS-1$
                id = e.getAttribute("id"); //$NON-NLS-1$
            }
            if (id.equals("")) { //$NON-NLS-1$
                styled.append(Messages.getString("XLIFFChecker.244")); //$NON-NLS-1$
                return false;
            }
            eptTable.put(id, ""); //$NON-NLS-1$
        }

        // Recurse all children
        NodeList nodes = e.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                boolean result = recurse((Element) n);
                if (result == false) {
                    return false;
                }
            }
        }

        // check if inline tags are paired at  <source>, <seg-source> and <target>
        if (e.getLocalName().equals("source") || //$NON-NLS-1$
                e.getLocalName().equals("seg-source") || //$NON-NLS-1$
                e.getLocalName().equals("target")) //$NON-NLS-1$
        {
            Enumeration<String> keys = bxTable.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                if (exTable.containsKey(key)) {
                    exTable.remove(key);
                    bxTable.remove(key);
                }
            }

            if (exTable.size() > 0 || bxTable.size() > 0) {
                MessageFormat mf = new MessageFormat(Messages.getString("XLIFFChecker.2")); //$NON-NLS-1$
                styled.append(mf.format(new Object[]{e.getLocalName()}));
                return false;
            }
            keys = bptTable.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                if (eptTable.containsKey(key)) {
                    eptTable.remove(key);
                    bptTable.remove(key);
                }
            }
            if (eptTable.size() > 0 || bptTable.size() > 0) {
                MessageFormat mf = new MessageFormat(Messages.getString("XLIFFChecker.5")); //$NON-NLS-1$
                styled.append(mf.format(new Object[]{e.getLocalName()}));
                return false;
            }

        }

        // check for not paired <it> tags in <file>
        if (e.getLocalName().equals("file")) { //$NON-NLS-1$
            if (srcItTable.size() + tgtItTable.size() > 0) {
                styled.append(Messages.getString("XLIFFChecker.252")); //$NON-NLS-1$
                return false;
            }
        }

        // check for missing <trans-unite> referenced in <sub>
        if (e.getLocalName().equals("file")) { //$NON-NLS-1$
            Enumeration<String> keys = xids.keys();
            while (keys.hasMoreElements()) {
                if (!ids.containsKey(keys.nextElement())) {
                    styled.append(Messages.getString("XLIFFChecker.41"));  //$NON-NLS-1$
                    return false;
                }
            }
        }

        if (e.getLocalName().equals("alt-trans")) { //$NON-NLS-1$
            // leaving an <alt-trans> element
            inAltTrans = false;
        }

        if (e.getLocalName().equals("seg-source")) { //$NON-NLS-1$
            // leaving <seg-source>
            inSegSource = false;
        }

        if (e.getLocalName().equals("source")) { //$NON-NLS-1$
            // leaving <source>
            inSource = false;
        }

        // all seems fine so far
        return true;
    }

    private boolean checkLanguage(String lang) {

        if (lang.startsWith("x-") || lang.startsWith("X-")) { //$NON-NLS-1$ //$NON-NLS-2$
            // custom language code
            return true;
        }

        return !langParser.getTagDescription(lang).equals("");  //$NON-NLS-1$ 
    }

    private static boolean checkURL(String string) {
        try {
            new URL(string);
            return true;
        } catch (Exception e1) {
            try {
                new File(string);
                return true;
            } catch (Exception e2) {
                return false;
            }
        }
    }

    private static boolean checkDate(String date) {

        // YYYY-MM-DDThh:mm:ssZ
        if (date.length() != 20) {
            return false;
        }
        if (date.charAt(4) != '-') {
            return false;
        }
        if (date.charAt(7) != '-') {
            return false;
        }
        if (date.charAt(10) != 'T') {
            return false;
        }
        if (date.charAt(13) != ':') {
            return false;
        }
        if (date.charAt(16) != ':') {
            return false;
        }
        if (date.charAt(19) != 'Z') {
            return false;
        }
        try {
            int year = Integer.parseInt("" + date.charAt(0) + date.charAt(1) + date.charAt(2) + date.charAt(3)); //$NON-NLS-1$
            if (year < 0) {
                return false;
            }
            int month = Integer.parseInt("" + date.charAt(5) + date.charAt(6)); //$NON-NLS-1$
            if (month < 1 || month > 12) {
                return false;
            }
            int day = Integer.parseInt("" + date.charAt(8) + date.charAt(9)); //$NON-NLS-1$
            switch (month) {
                case 1:
                case 3:
                case 5:
                case 7:
                case 8:
                case 10:
                case 12:
                    if (day < 1 || day > 31) {
                        return false;
                    }
                    break;
                case 4:
                case 6:
                case 9:
                case 11:
                    if (day < 1 || day > 30) {
                        return false;
                    }
                    break;
                case 2:
                    // check for leap years
                    if (year % 4 == 0) {
                        if (year % 100 == 0) {
                            // not all centuries are leap years
                            if (year % 400 == 0) {
                                if (day < 1 || day > 29) {
                                    return false;
                                }
                            } else {
                                // not leap year
                                if (day < 1 || day > 28) {
                                    return false;
                                }
                            }
                        }
                        if (day < 1 || day > 29) {
                            return false;
                        }
                    } else if (day < 1 || day > 28) {
                        return false;
                    }
            }
            int hour = Integer.parseInt("" + date.charAt(11) + date.charAt(12)); //$NON-NLS-1$
            if (hour < 0 || hour > 23) {
                return false;
            }
            int min = Integer.parseInt("" + date.charAt(14) + date.charAt(15)); //$NON-NLS-1$
            if (min < 0 || min > 59) {
                return false;
            }
            int sec = Integer.parseInt("" + date.charAt(17) + date.charAt(18)); //$NON-NLS-1$
            if (sec < 0 || sec > 59) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void validateWithDTD(Element root, String fileName) throws Exception {
        DocumentType doctype = document.getDoctype();
        if (doctype == null || doctype.getSystemId() == null) {
            styled.append(Messages.getString("XLIFFChecker.255")); //$NON-NLS-1$
            display.update();
            display.readAndDispatch();
            File temp = null;
            try {
                temp = File.createTempFile("temp", ".xlf"); //$NON-NLS-1$ //$NON-NLS-2$
                temp.deleteOnExit();
                fileName = temp.getAbsolutePath();
                DOMImplementation imp = document.getImplementation();
                doctype = imp.createDocumentType("xliff", "-//XLIFF//DTD XLIFF//EN", new File("catalog/xliff/xliff.dtd").getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                document.insertBefore(doctype, root);
                Writer writer = new Writer();
                FileOutputStream output = new FileOutputStream(temp);
                writer.setOutput(output);
                writer.write(document);
                output.close();
                styled.append(Messages.getString("XLIFFChecker.261")); //$NON-NLS-1$
                display.update();
                display.readAndDispatch();
            } catch (Exception ex) {
//                MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
                //               box.setMessage(Messages.getString("XLIFFChecker.262")); //$NON-NLS-1$
//                box.open();
                return;
            }
        }
        try {
            // reload validating
            document = null;
            root = null;
            DocumentBuilderFactory parserFactory = DocumentBuilderFactory.newInstance();
            parserFactory.setNamespaceAware(true);
            parserFactory.setValidating(true);
            String id = "http://apache.org/xml/properties/dom/document-class-name";  //$NON-NLS-1$
            Object value = "org.apache.xerces.dom.DocumentImpl";  //$NON-NLS-1$
            try {
                parserFactory.setAttribute(id, value);
            } catch (IllegalArgumentException e) {
                System.err.println("Could not set parser property."); //$NON-NLS-1$
            }
            DocumentBuilder parser = parserFactory.newDocumentBuilder();
            if (!parser.isValidating()) {
                System.err.println("Received a non-validating parser"); //$NON-NLS-1$
            }
            parser.setErrorHandler(new CustomErrorHandler());
            parser.setEntityResolver(new CustomResolver("catalog/catalog.xml"));  //$NON-NLS-1$
            document = parser.parse(fileName);
            root = document.getDocumentElement();
            styled.append(Messages.getString("XLIFFChecker.263"));	 //$NON-NLS-1$
            display.update();
            display.readAndDispatch();
        } catch (Exception ex) {
            MessageFormat mfe = new MessageFormat(Messages.getString("XLIFFChecker.264")); //$NON-NLS-1$
            styled.append(mfe.format(new Object[]{ex.getMessage()}));
            showResult(Messages.getString("XLIFFChecker.265"), true); //$NON-NLS-1$
            return;
        }
        if (!validateDetails(root)) {
            showResult(Messages.getString("XLIFFChecker.266"), true); //$NON-NLS-1$
            return;
        }
        showResult(Messages.getString("XLIFFChecker.267"), false); //$NON-NLS-1$

    }
    /*private void show() {
     shell.open();
     while (!shell.isDisposed()) {
     if (!display.readAndDispatch()) {
     display.sleep();
     }
     }
     }*/

    /**
     * @return the strict
     */
    public boolean isStrict() {
        return strict;
    }

    /**
     * @param strict the strict to set
     */
    public void setStrict(boolean strict) {
        this.strict = strict;
    }
}
