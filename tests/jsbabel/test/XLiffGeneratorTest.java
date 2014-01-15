/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsbabel.test;

import com.maxprograms.xliffchecker.XLIFFChecker;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import jsbabel.xliff.XLiffGenerator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author perasso
 */
public class XLiffGeneratorTest {

    public XLiffGeneratorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void validateFiles() throws TransformerConfigurationException, IOException, TransformerException, ParserConfigurationException, SAXException, Exception {
        validateXLiffFile("jsbabel/test/res/test.html");
    }

    @Test
    public void validateUrls() throws TransformerConfigurationException, IOException, TransformerException, ParserConfigurationException, SAXException, Exception {
        validateXLiffUrl("http://www.mtbscout.it");
        validateXLiffUrl("http://www.html.it");
        validateXLiffUrl("http://www.mtb-forum.it");
    }
    private void validateXLiffFile(String file) throws TransformerConfigurationException, IOException, TransformerException, ParserConfigurationException, SAXException, Exception {
        String path = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        File f = new File(new File(path), file);
        XLiffGenerator creator = new XLiffGenerator();
        Document dom = creator.parse(f, "utf-8", "en-US");
        validate(dom);

    }

    private void validateXLiffUrl(String url) throws TransformerConfigurationException, IOException, TransformerException, ParserConfigurationException, SAXException, Exception {
        XLiffGenerator creator = new XLiffGenerator();
        Document dom = creator.parse(url, "en-US", "it-IT");
        validate(dom);

    }

    private void validate(Document dom) throws Exception, TransformerConfigurationException, IllegalArgumentException, IOException, TransformerException, FileNotFoundException, TransformerFactoryConfigurationError {
        File tempFile = File.createTempFile("tmp", ".xlf");
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StreamResult result = new StreamResult(new FileOutputStream(tempFile));
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(new DOMSource(dom), result);
        XLIFFChecker checker = new XLIFFChecker();
        try {
            checker.validateFile(tempFile.getPath());
        } catch (Exception ex) {
            System.err.append(tempFile.getPath());
            System.err.append("\r\n");
            System.err.append(ex.toString());
            throw ex;
        }
    }
}
