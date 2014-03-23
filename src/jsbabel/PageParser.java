package jsbabel;

import jsbabel.entities.Page;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;

public class PageParser implements NodeVisitor {

    ArrayList<String> visitedPages = null;
    protected static final String[] invalidExtensions = {".jpg", ".png", ".gif", ".tif", ".pdf", ".zip"};
    protected static Pattern ScriptRegExp = Pattern.compile("_jsb\\s*\\(\\s*\"((.(?!\"\\s*\\)))*.)");
    protected final ProcedureController controller;
    protected boolean hasBabelScript = false;
    protected List<String> skipPages = new ArrayList<String>();
    private Element noTranslateElement = null;

    public PageParser(ProcedureController controller) {
        this.controller = controller;
        this.visitedPages = new ArrayList<String>();

        skipPages.add("mirror.jsp");
        visitedPages.add("mirror.jsp");
    }

    protected org.jsoup.nodes.Document connect(String url) throws IOException {
        org.jsoup.nodes.Document doc = Jsoup.connect(url)
                .userAgent(Const.UserAgent)
                .referrer(Helper.getExecutingHost())
                .get();
        return doc;
    }

    public static boolean isAttributeToTranslate(Element currentNode, Attribute attr) {
        String name = attr.getKey();
        if ("alt".equalsIgnoreCase(name)
                || "title".equalsIgnoreCase(name)
                || "abbr".equalsIgnoreCase(name)
                || "abbr".equalsIgnoreCase(name)
                || "accesskey".equalsIgnoreCase(name)
                || "label".equalsIgnoreCase(name) 
                || "placeholder".equalsIgnoreCase(name)
                || "prompt".equalsIgnoreCase(name)
                || "standby".equalsIgnoreCase(name)
                || "summary".equalsIgnoreCase(name)) {

            return true;
        }
        if ("content".equalsIgnoreCase(name)) {
            if (!"meta".equalsIgnoreCase(currentNode.nodeName())) {
                return false;
            }

            String httpEquiv = currentNode.attr("http-equiv");
            if ("keywords".equalsIgnoreCase(httpEquiv)) {
                return true;
            }
            if (!httpEquiv.isEmpty()) {
                return false;
            }
            String aName = currentNode.attr("name");
            return !name.isEmpty() && !"generator".equalsIgnoreCase(aName) && !"author".equalsIgnoreCase(aName) && !"progid".equalsIgnoreCase(aName) && !"date".equalsIgnoreCase(aName);
        }

        if ("value".equalsIgnoreCase(name)) {
            return "button".equalsIgnoreCase(currentNode.nodeName());
        }
        return false;
    }

    protected boolean isValidAddress(URL u) {
        String path = Helper.getCleanPath(u).toLowerCase();
        if (visitedPages.contains(path)) {
            return false;
        }

        int idx = path.lastIndexOf('.');
        if (idx == -1) {
            return true;
        }
        String ext = path.substring(idx);

        for (String s : invalidExtensions) {
            if (s.equals(ext)) {
                return false;
            }
        }
        return true;
    }

    protected boolean skipContent(Node currentNode) {
        if (currentNode == null) {
            return true;
        }

        String tagName = currentNode.nodeName();

        return "script".equals(tagName) || "style".equals(tagName);
    }

    public void addSkipUpdate(Page p) {
        String page = p.getPage().toLowerCase();
        if (!skipPages.contains(page)) {
            skipPages.add(page);
        }
    }

    protected void traversePage(String sUrl) {
        Element node;
        try {
            node = connect(sUrl);
            node.traverse(this);
        } catch (MalformedURLException e) {
            controller.log(e.getLocalizedMessage());
        } catch (IOException e) {
            controller.log(e.getLocalizedMessage());
        }
    }

    @Override
    public void head(Node htmlNode, int arg1) {
        if (htmlNode instanceof Element && ((Element) htmlNode).hasClass("jsb_notranslate")) {
            noTranslateElement = (Element) htmlNode;
        }
    }

    @Override
    public void tail(Node htmlNode, int arg1) {
        if (noTranslateElement != null) {
            if (htmlNode == noTranslateElement) {
                noTranslateElement = null;
            }
            return;
        }
        if (htmlNode instanceof Element) {
            Element currentNode = (Element) htmlNode;
            processElement(currentNode);

        } else if (htmlNode instanceof TextNode) {
            if (skipContent(htmlNode.parent())) {
                return;
            }
            TextNode text = (TextNode) htmlNode;
            processTextNode(text);

        }

    }

    protected void processTextNode(TextNode text) {
    }

    protected void processElement(Element currentNode) {

    }
}
