package jsbabel;

import java.io.BufferedReader;
import jsbabel.entities.Site;
import jsbabel.entities.Page;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpSession;
import org.hibernate.HibernateException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;

public class SiteStringExtractor {

    ArrayList<String> visitedPages = null;
    private ProcedureController controller;
    private static final String[] invalidExtensions = {".jpg", ".png", ".gif", ".tif", ".pdf", ".zip"};
    private DataHelper dh;
    private final HttpSession session;
    private boolean hasBabelScript = false;
    private List<String> skipPages = new ArrayList<String>();
    private final Site site;
    private static Pattern ScriptRegExp = Pattern.compile("_jsb\\s*\\(\\s*\"((.(?!\"\\s*\\)))*.)");
    private Element noTranslateElement = null;

    public SiteStringExtractor(Site site, ProcedureController controller, HttpSession session) {
        this.controller = controller;
        this.visitedPages = new ArrayList<String>();
        this.site = site;
        this.session = session;
        dh = new DataHelper();
        skipPages.add("mirror.jsp");
        visitedPages.add("mirror.jsp");
    }

    public void dispose() {
        if (dh != null) {
            dh.dispose();
            dh = null;
        }
    }

    public List<Page> parse(List<URL> urls, boolean recursive) throws HibernateException, ClassNotFoundException, Exception {
        dh.beginTransaction();
        List<Page> pages = new ArrayList<Page>();
        for (URL u : urls) {
            pages.addAll(parse(u, recursive));
        }
        dh.commit();
        return pages;
    }

    public List<Page> parse(URL url, boolean recursive) throws HibernateException, ClassNotFoundException, Exception {
        dh.beginTransaction();
        List<Page> pages = new ArrayList<Page>();
        parse(url, pages, recursive);
        dh.commit();
        return pages;

    }

    private void parse(final URL url, List<Page> pages, final boolean recursive) throws ClassNotFoundException, Exception {

        if (controller.isAborted()) {
            return;
        }
        String sUrl = url.toString();
        long siteId = (site == null) ? dh.getSiteId(session, sUrl) : site.getId();
        final String lowerPageUrl = Helper.getCleanPath(url).toLowerCase();
        Page p = dh.getPage(session, sUrl);
        if (p.isIgnored()) {
            return;
        }
        if (visitedPages.contains(lowerPageUrl)) {
            return;
        }
        controller.log("Processing url " + sUrl, Level.INFO);
        visitedPages.add(lowerPageUrl);

        final List<URL> linkedUrls = new ArrayList<URL>();
        final Translations translations = new Translations();
        Element node;
        try {
            node = Jsoup.connect(sUrl)
                    .userAgent(Const.UserAgent)
                    .referrer(Helper.getExecutingHost())
                    .get();
            node.traverse(new NodeVisitor() {
                public void head(Node htmlNode, int arg1) {
                    if (htmlNode instanceof Element && ((Element) htmlNode).hasClass("jsb_notranslate")) {
                        noTranslateElement = (Element) htmlNode;
                    }
                }

                public void tail(Node htmlNode, int arg1) {
                    if (noTranslateElement != null) {
                        if (htmlNode == noTranslateElement) {
                            noTranslateElement = null;
                        }
                        return;
                    }
                    if (htmlNode instanceof Element) {
                        Element currentNode = (Element) htmlNode;


                        extractAttributes(currentNode);
                        if ("script".equals(currentNode.nodeName())) {
                            try {
                                parseScript(currentNode);
                            } catch (Exception ex) {
                                controller.log(ex.getLocalizedMessage());
                            }
                        } else if (recursive && "a".equals(currentNode.nodeName())) {
                            {
                                String addr = currentNode.attr("href");
                                if (addr != null) {
                                    try {
                                        URL u = new URL(url, addr);
                                        if (u.getHost().equals(url.getHost()) && isValidAddress(u)) {
                                            linkedUrls.add(u);
                                        }

                                    } catch (MalformedURLException e) {
                                        controller.log(e.getLocalizedMessage());
                                    }

                                }
                            }
                        }
                    } else if (htmlNode instanceof TextNode) {
                        if (skipContent(htmlNode.parent())) {
                            return;
                        }
                        TextNode text = (TextNode) htmlNode;
                        addToPage(text.text());
                    }

                }

                private void addToPage(String text) {
                    if (skipPages.contains(lowerPageUrl)) {
                        return;
                    }
                    text = Helper.prepareBaseString(text);
                    if (Helper.isBaseStringToIgnore(text)) {
                        return;
                    }
                    if (!translations.contains(text)) {
                        translations.add(text, "", false, null);
                    }

                }

                private void parseScript(Element currentNode) throws MalformedURLException, IOException {
                    String src = currentNode.attr("src");
                    if (src.endsWith("/babel.js")) {
                        hasBabelScript = true;
                    }
                    String text = "";
                    if (src.length() == 0) {
                        text = currentNode.html();
                    } else {
                        URL u = new URL(url, src);
                        if (!u.getHost().equals(url.getHost())) {
                            return;
                        }
                        InputStream openStream = u.openStream();
                        BufferedReader in = new BufferedReader(new InputStreamReader(openStream));
                        StringBuilder responseData = new StringBuilder();
                        String line;
                        while ((line = in.readLine()) != null) {
                            responseData.append(line);
                        }
                        text = responseData.toString();
                        openStream.close();
                    }
                    Matcher m = ScriptRegExp.matcher(text);
                    while (m.find()) {
                        addToPage(m.group(1));
                    }
                }

                public void extractAttributes(Element currentNode) {
                    for (Attribute attr : currentNode.attributes()) {
                        if (isAttributeToTranslate(currentNode, attr)) {
                            addToPage(attr.getValue());
                        }
                    }
                }
            });
        } catch (MalformedURLException e) {
            controller.log(e.getLocalizedMessage());
        } catch (IOException e) {
            controller.log(e.getLocalizedMessage());
        } catch (Exception e) {
            controller.log(e.getLocalizedMessage());
        }
        if (hasBabelScript) {
            dh.translateBlock(p, session, sUrl, translations, true, siteId);
            if (p != null && p.size() > 0) {
                pages.add(p);
            }
        }
        for (URL u : linkedUrls) {
            if (controller.isAborted()) {
                return;
            }
            try {
                parse(u, pages, recursive);
            } catch (Exception ex) {
                controller.log(ex.getMessage(), Level.SEVERE);
            }
        }

    }

    public static boolean isAttributeToTranslate(Element currentNode, Attribute attr) {
        String name = attr.getKey();
        if ("alt".equalsIgnoreCase(name)
                || "title".equalsIgnoreCase(name)
                || "abbr".equalsIgnoreCase(name)
                || "abbr".equalsIgnoreCase(name)
                || "accesskey".equalsIgnoreCase(name)
                || "label".equalsIgnoreCase(name)
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
            if (name.isEmpty() || "generator".equalsIgnoreCase(aName) || "author".equalsIgnoreCase(aName) || "progid".equalsIgnoreCase(aName) || "date".equalsIgnoreCase(aName)) {
                return false;
            }
            return true;
        }


        if ("value".equalsIgnoreCase(name)) {
            return "button".equalsIgnoreCase(currentNode.nodeName());
        }
        return false;
    }

    private boolean isValidAddress(URL u) {
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

    private boolean skipContent(Node currentNode) {
        if (currentNode == null) {
            return true;
        }

        String tagName = currentNode.nodeName();
        if ("script".equals(tagName) || "style".equals(tagName)) {
            return true;
        }

        return false;
    }

    public void addSkipUpdate(Page p) {
        String page = p.getPage().toLowerCase();
        if (!skipPages.contains(page)) {
            skipPages.add(page);
        }
    }
}
