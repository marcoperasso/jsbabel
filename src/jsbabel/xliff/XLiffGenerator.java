/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsbabel.xliff;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import jsbabel.Const;
import jsbabel.Helper;
import jsbabel.SiteStringExtractor;
import jsbabel.Translation;
import jsbabel.Translations;
import jsbabel.entities.StringType;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 *
 * @author perasso
 */
public class XLiffGenerator implements NodeVisitor {

    private Document document = null;
    private XLiffTable table = null;
    private Node bodyNode;
    private Translations translations;
    private static final String toolId = "JSBABEL";
    private static final String toolName = "JSBABEL";
    private static final String toolVersion = "1.0";
    private static final String toolCompany = "Marco Perasso";
    private Integer tuId = 0;

    private String getNewTranslationUnitId() {
        return (tuId++).toString();
    }

    private void processElement(XLiffElement xEl, org.jsoup.nodes.Node node, boolean parentTU) throws DOMException {
        Element outerEl, innerEl;
        switch (currentLevel.state) {
            case SIMPLETU:
            case ATTRIBUTETU:
                if (parentTU) {
                    outerEl = document.createElement(currentLevel.state == State.SIMPLETU ? "g" : "ph");
                    outerEl.setAttribute("id", getNewTranslationUnitId());
                    innerEl = outerEl;
                    outerEl.setAttribute(xEl.inline ? "ctype" : "restype", "x-html-" + node.nodeName()/* xEl.resType*/);
                } else {
                    outerEl = document.createElement("trans-unit");
                    outerEl.setAttribute("id", getNewTranslationUnitId());
                    innerEl = (Element) outerEl.appendChild(document.createElement("source"));
                    outerEl.setAttribute("restype", "x-html-" + node.nodeName()/*xEl.resType*/);
                }

                break;
            default:
                if (parentTU) {
                    if (currentLevel.TUNodes.isEmpty()) {
                        outerEl = document.createElement("x");
                        outerEl.setAttribute("id", getNewTranslationUnitId());
                        innerEl = outerEl;
                        outerEl.setAttribute("ctype", "x-html-" + node.nodeName()/*xEl.resType*/);
                    } else {
                        outerEl = document.createElement("g");
                        outerEl.setAttribute("id", getNewTranslationUnitId());
                        innerEl = outerEl;
                        outerEl.setAttribute("ctype", "x-html-" + node.nodeName()/*xEl.resType*/);
                    }
                } else {
                    outerEl = document.createElement("group");
                    innerEl = outerEl;
                    outerEl.setAttribute("restype", "x-html-" + node.nodeName()/*xEl.resType*/);
                }


        }

        for (org.jsoup.nodes.Attribute attr : node.attributes()) {
            if (SiteStringExtractor.isAttributeToTranslate((org.jsoup.nodes.Element) node, attr)) {
                String base = Helper.prepareBaseString(attr.getValue());
                if (!isBaseStringToIgnore(base)) {
                    //non sono in uno stato di translate unit:
                    //allora devo creare una TU ad hoc
                    if (currentLevel.state != State.ATTRIBUTETU && currentLevel.state != State.SIMPLETU && !parentTU) {
                        Element tu = (Element) innerEl.appendChild(document.createElement("trans-unit"));
                        tu.setAttribute("id", getNewTranslationUnitId());
                        tu.setAttribute("restype", "x-html-" + node.nodeName() + "-" + attr.getKey());
                        Element source = (Element) tu.appendChild(document.createElement("source"));
                        source.appendChild(document.createTextNode(base));

                    } else {
                        Element parentOfSub = innerEl;
                        if (!"ph".equals(parentOfSub.getNodeName())) {
                            parentOfSub = (Element) parentOfSub.appendChild(document.createElement("ph"));
                            parentOfSub.setAttribute("id", getNewTranslationUnitId());
                        }
                        Element sub = (Element) parentOfSub.appendChild(document.createElement("sub"));
                        sub.appendChild(document.createTextNode(base));
                        sub.setAttribute("ctype", "x-html-" + node.nodeName() + "-" + attr.getKey());
                    }
                    continue;
                }
            }
            outerEl.setAttribute("html:" + attr.getKey(), attr.getValue());
        }

        //ogni nodo viene creato in due versioni, perché non so se alla fine avrò una TU in cui inserirlo oppure no
        //lo so in questo momento, in cui faccio il travaso: se sono in stato di TU, devo travasare quelli per il TU, altrimenti gli altri
        List<InnerNode> list = isTUContext(currentLevel)
                ? currentLevel.TUNodes
                : currentLevel.nonTUNodes;

        InnerNode newNode = new InnerNode(innerEl, outerEl, xEl);
        for (InnerNode n : list) {
            newNode.add(n);
            //innerEl.appendChild(n.node);
        }
        //li aggiungo sempre in entrambe le liste, ad eccezione di questo punto in cui travaso i 
        (parentTU ? currentLevel.parentLevel.TUNodes : currentLevel.parentLevel.nonTUNodes)
                .add(newNode);
    }

    private boolean isTUContext(NodeLevel level) {
        if (level.state == State.ATTRIBUTETU || level.state == State.SIMPLETU) {
            return true;
        }
        return (level.parentLevel == null) ? false : isTUContext(level.parentLevel);
    }

    /**
     * @return the translations
     */
    public Translations getTranslations() {
        return translations;
    }

    /**
     * @param translations the translations to set
     */
    public void setTranslations(Translations translations) {
        this.translations = translations;
    }

    private boolean isBaseStringToIgnore(String base) {
        return (translations != null)
                ? translations.isBaseStringToIgnore(base)
                : Helper.isBaseStringToIgnore(base);
    }

    public void importStream(InputStream inputStream) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int read;
            while ((read = inputStream.read(buff)) > 0) {
                baos.write(buff, 0, read);
            }
            document = db.parse(new ByteArrayInputStream(baos.toByteArray()));
            String originalFile = document.getDocumentElement().getAttribute("original");
            String baseLanguage = document.getDocumentElement().getAttribute("source-language");
            String targetLanguage = document.getDocumentElement().getAttribute("target-language");
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            XPathExpression xPathExpression = xPath.compile("//trans-unit");
            NodeList nodes = (NodeList) xPathExpression.evaluate(document, XPathConstants.NODESET);
            //XPathExpression xPathExpressionSource = xPath.compile("source/descendant::sub");
            //XPathExpression xPathExpressionTarget = xPath.compile("target/descendant::sub");
            XPathExpression sourceExpr = xPath.compile("source");
            XPathExpression targetExpr = xPath.compile("target");
            sourceNodesLoop:
            for (int i = 0; i < nodes.getLength(); i++) {
                Element tu = (Element) nodes.item(i);

                Element targetNode = (Element) targetExpr.evaluate(tu, XPathConstants.NODESET);
                if (targetNode == null) {
                    continue;
                }
                Element sourceNode = (Element) sourceExpr.evaluate(tu, XPathConstants.NODESET);
                if (sourceNode == null) {
                    continue;
                }
                NodeList sourceChildNodes = sourceNode.getChildNodes();
                NodeList targetChildNodes = targetNode.getChildNodes();
                int targetIdx = 0;
                for (int x = 0; x < sourceChildNodes.getLength(); x++) {
                    if (targetIdx > targetChildNodes.getLength()) {
                        break sourceNodesLoop;
                    }
                    Node sourceChild = sourceChildNodes.item(x);
                    Node targetChild = targetChildNodes.item(targetIdx);
                    if (sourceChild instanceof Text && targetChild instanceof Text) {
                        translations.add(((Text) sourceChild).getNodeValue(), ((Text) targetChild).getNodeValue(), false, StringType.Text);
                        targetIdx++;
                    }


                    /*if (targetChild.getLocalName()) {
                    }*/
                }

            }
        } catch (Exception ex) {
            Helper.log(ex);
        }
    }

    class InnerNode {

        public InnerNode(Node innerNode, Node outerNode, XLiffElement xliff) {
            this.innerNode = innerNode;
            this.outerNode = outerNode;
            this.xliff = xliff;
        }
        Node innerNode;
        Node outerNode;
        XLiffElement xliff;
        List<InnerNode> childs = new ArrayList<InnerNode>();

        @Override
        public String toString() {
            try {
                return node2String(outerNode);
            } catch (Exception ex) {
                return "";
            }
        }

        String node2String(Node node) throws TransformerFactoryConfigurationError, TransformerException {
            // you may prefer to use single instances of Transformer, and
            // StringWriter rather than create each time. That would be up to your
            // judgement and whether your app is single threaded etc
            StreamResult xmlOutput = new StreamResult(new StringWriter());
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(new DOMSource(node), xmlOutput);
            return xmlOutput.getWriter().toString();
        }

        private void add(InnerNode newNode) {
            this.childs.add(newNode);
        }

        private Node createNodeTree() {
            for (InnerNode child : childs) {
                innerNode.appendChild(child.createNodeTree());
            }
            return outerNode;
        }
    }

    class NodeLevel {

        public NodeLevel(NodeLevel parentLevel) {
            this.parentLevel = parentLevel;
        }
        NodeLevel parentLevel;
        State state = State.CONVERTIBLEGROUP;
        String name;
        List<InnerNode> TUNodes = new ArrayList<InnerNode>();
        List<InnerNode> nonTUNodes = new ArrayList<InnerNode>();

        @Override
        public String toString() {
            return name;
        }
    }

    enum State {

        CONVERTIBLEGROUP, //gruppo che può diventare una TU
        UNCONVERTIBLEGROUP, //gruppo che non può più diventare una TU
        SIMPLETU, //translation unit per nodi di testo                 
        ATTRIBUTETU         //translation unit per attributi
    }
    NodeLevel currentLevel = null;

    public XLiffGenerator() {
    }

    public Document parse(File file, String charsetName, String sourceLanguage) throws IOException, ParserConfigurationException {
        org.jsoup.nodes.Document doc = Jsoup.parse(file, charsetName);
        return parse(doc, file.getPath(), sourceLanguage, null);
    }

    public Document parse(String url, String sourceLanguage, String targetLanguage) throws IOException, ParserConfigurationException {
        org.jsoup.nodes.Document doc = Jsoup.connect(url)
                .userAgent(Const.UserAgent)
                .referrer(Helper.getExecutingHost())
                .get();
        return parse(doc, url, sourceLanguage, targetLanguage);
    }

    public Document createDocument(String url, String sourceLanguage, String targetLanguage) throws ParserConfigurationException, DOMException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        final String ns = "http://www.w3.org/2000/xmlns/";
        //Using factory get an instance of document builder
        DocumentBuilder db = dbf.newDocumentBuilder();
        document = db.newDocument();
        document.setXmlStandalone(true);

        Element el = (Element) document.appendChild(document.createElement("xliff"));
        el.setAttributeNS(ns, "xmlns", "urn:oasis:names:tc:xliff:document:1.2");
        el.setAttributeNS(ns, "xmlns:html", "http://www.w3.org/1999/xhtml");
        el.setAttributeNS(ns, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        el.setAttribute("xsi:schemaLocation", "urn:oasis:names:tc:xliff:document:1.2 xliff-core-1.2-transitional.xsd");
        el.setAttribute("version", "1.2");
        Element fileEl = (Element) el.appendChild(document.createElement("file"));
        fileEl.setAttribute("original", url);
        fileEl.setAttribute("source-language", sourceLanguage);
        if (targetLanguage != null) {
            fileEl.setAttribute("target-language", targetLanguage);
        }
        fileEl.setAttribute("datatype", "html");

        Element headerEl = (Element) fileEl.appendChild(document.createElement("header"));
        Element toolEl = (Element) headerEl.appendChild(document.createElement("tool"));
        toolEl.setAttribute("tool-id", toolId);
        toolEl.setAttribute("tool-name", toolName);
        toolEl.setAttribute("tool-version", toolVersion);
        toolEl.setAttribute("tool-company", toolCompany);
        bodyNode = fileEl.appendChild(document.createElement("body"));
        return document;
    }

    public void applyTranslations() throws XPathExpressionException {
        if (translations == null) {
            return;
        }
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        XPathExpression xPathExpression = xPath.compile("//trans-unit");
        NodeList nodes = (NodeList) xPathExpression.evaluate(document, XPathConstants.NODESET);

        xPathExpression = xPath.compile("descendant::text()");
        //resetto lo stato di usato, ogni volta che uso una traduzione
        //verrà marcata come usata
        translations.resetUsedTranslations();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element tu = (Element) nodes.item(i);
            Element target = (Element) tu.appendChild(document.createElement("target"));
            NodeList elementsByTagName = tu.getElementsByTagName("source");
            NodeList childs = elementsByTagName.item(0).getChildNodes();
            for (int j = 0; j < childs.getLength(); j++) {
                target.appendChild(document.importNode(childs.item(j), true));
            }

            NodeList textNodes = (NodeList) xPathExpression.evaluate(target, XPathConstants.NODESET);
            for (int j = 0; j < textNodes.getLength(); j++) {
                Text tn = (Text) textNodes.item(j);
                String text = tn.getNodeValue();
                tn.setNodeValue(translations.translate(text, false));
            }
        }
        //ciclo su tutte le traduzioni, quelle che non sono state usate le metto in TU a parte
        for (Translation t : translations.getTranslations()) {
            if (t.isUsed() || t.getType() == StringType.Ignore) {
                continue;
            }
            Element tu = document.createElement("trans-unit");
            bodyNode.appendChild(tu);
            tu.setAttribute("id", getNewTranslationUnitId());
            Element source = (Element) tu.appendChild(document.createElement("source"));
            source.appendChild(document.createTextNode(t.getBaseString()));
            Element target = (Element) tu.appendChild(document.createElement("target"));
            target.appendChild(document.createTextNode(t.getTargetString()));

        }
    }

    private Document parse(org.jsoup.nodes.Document doc, String url, String sourceLanguage, String targetLanguage) throws IOException, ParserConfigurationException {
        if (table == null) {
            table = new XLiffTable();
        }
        createDocument(url, sourceLanguage, targetLanguage);

        doc.traverse(this);
        return document;
    }

    @Override
    public void head(org.jsoup.nodes.Node node, int i) {
        if (node instanceof org.jsoup.nodes.Document) {
            currentLevel = new NodeLevel(currentLevel);
            currentLevel.name = node.nodeName();
        } else if (node instanceof org.jsoup.nodes.Element) {
            XLiffElement xEl = table.findElement(node.nodeName());

            if (!xEl.inline) {
                if (currentLevel.state == State.SIMPLETU) {
                    //ho beccato un elemento non inline ed ero in translation unit state:
                    //devo creare una TU separata
                    Element el = (Element) document.createElement("trans-unit");
                    el.setAttribute("id", getNewTranslationUnitId());
                    //el.setAttribute(xEl.inline ? "ctype" : "restype", xEl.resType); non ho tipo, sono un nodo di testo 'sciolto'
                    Element sourceEl = (Element) el.appendChild(document.createElement("source"));
                    InnerNode innerNode = new InnerNode(sourceEl, el, table.findElement("#text"));
                    for (InnerNode n : currentLevel.TUNodes) {
                        innerNode.add(n);
                    }
                    currentLevel.TUNodes.clear();
                    currentLevel.nonTUNodes.clear();
                    currentLevel.TUNodes.add(innerNode);
                    currentLevel.nonTUNodes.add(innerNode);
                }

                //se non sono inline, lo stato non è più convertibile in TU
                currentLevel.state = State.UNCONVERTIBLEGROUP;
            }

            currentLevel = new NodeLevel(currentLevel);
            currentLevel.name = node.nodeName();

            for (org.jsoup.nodes.Attribute attr : node.attributes()) {
                if (SiteStringExtractor.isAttributeToTranslate((org.jsoup.nodes.Element) node, attr)) {
                    String base = Helper.prepareBaseString(attr.getValue());
                    if (isBaseStringToIgnore(base)) {
                        continue;
                    }

                    if (currentLevel.state != State.UNCONVERTIBLEGROUP) {
                        currentLevel.state = State.ATTRIBUTETU;
                    }
                }
            }
        } else if (node instanceof org.jsoup.nodes.TextNode) {
            TextNode text = (TextNode) node;
            String base = Helper.prepareBaseString(text.text());
            if (isBaseStringToIgnore(base)) {
                return;
            }

            //ho beccato un elemento di testo, ma sono in uno stato di gruppo non convertibile
            //quindi creo una TU separata
            if (currentLevel.state == State.UNCONVERTIBLEGROUP) {
                Element el = (Element) document.createElement("trans-unit");
                el.setAttribute("id", getNewTranslationUnitId());
                //el.setAttribute(xEl.inline ? "ctype" : "restype", xEl.resType); non ho tipo, sono un nodo di testo 'sciolto'
                Element sourceEl = (Element) el.appendChild(document.createElement("source"));
                sourceEl.appendChild(document.createTextNode(base));
                InnerNode innerNode = new InnerNode(sourceEl, el, table.findElement("#text"));
                currentLevel.TUNodes.add(innerNode);
                currentLevel.nonTUNodes.add(innerNode);
            } else {
                currentLevel.state = State.SIMPLETU;
            }

        }
    }

    @Override
    public void tail(org.jsoup.nodes.Node node, int i) {
        if (node instanceof org.jsoup.nodes.Document) {
            for (InnerNode n : currentLevel.nonTUNodes) {
                bodyNode.appendChild(n.createNodeTree());
            }
        } else if (node instanceof org.jsoup.nodes.Element) {
            XLiffElement xEl = table.findElement(node.nodeName());
            processElement(xEl, node, true);
            processElement(xEl, node, false);
            currentLevel = currentLevel.parentLevel;
        } else if (node instanceof org.jsoup.nodes.TextNode) {
            if (currentLevel.state != State.SIMPLETU) {
                return;
            }
            TextNode text = (TextNode) node;
            String base = Helper.prepareBaseString(text.text());
            Text textNode = document.createTextNode(base);
            InnerNode innerNode = new InnerNode(textNode, textNode, null);
            currentLevel.TUNodes.add(innerNode);
            currentLevel.nonTUNodes.add(innerNode);

        }

    }
}
