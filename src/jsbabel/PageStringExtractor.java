/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsbabel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.regex.Matcher;
import javax.servlet.http.HttpSession;
import static jsbabel.PageParser.isAttributeToTranslate;
import jsbabel.entities.Page;
import jsbabel.entities.Site;
import org.hibernate.HibernateException;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

/**
 *
 * @author Marco
 */
public class PageStringExtractor extends PageParser {

    private DataHelper dh;
    private final HttpSession session;
    private final Site site;
    private boolean recursive = false;
    List<URL> linkedUrls = new ArrayList<URL>();
    Translations translations = new Translations();
    Stack<URL> urls = new Stack<URL>();
    Stack<String> cleanUrls = new Stack<String>();

    public PageStringExtractor(Site site, ProcedureController controller, HttpSession session) {
        super(controller);
        this.session = session;
        this.site = site;
        dh = new DataHelper();

    }

    public void dispose() {
        if (dh != null) {
            dh.dispose();
            dh = null;
        }
    }

    public List<Page> parse(List<URL> urls, boolean recursive) throws HibernateException, ClassNotFoundException, Exception {
         this.recursive = recursive;
       dh.beginTransaction();
        List<Page> pages = new ArrayList<Page>();
        for (URL u : urls) {
            pages.addAll(parse(u, recursive));
        }
        dh.commit();
        return pages;
    }

    public List<Page> parse(URL url, boolean recursive) throws HibernateException, ClassNotFoundException, Exception {
         this.recursive = recursive;
       dh.beginTransaction();
        List<Page> pages = new ArrayList<Page>();
        parse(url, pages);
        dh.commit();
        return pages;

    }

    private void parse(final URL url, List<Page> pages) throws ClassNotFoundException, Exception {

        if (controller.isAborted()) {
            return;
        }
        try {
            String sUrl = url.toString();
            urls.push(url);
            long siteId = (site == null) ? dh.getSiteId(session, sUrl) : site.getId();
            final String lowerPageUrl = Helper.getCleanPath(url).toLowerCase();
            cleanUrls.push(lowerPageUrl);
            Page p = dh.getPage(session, sUrl);
            if (p.isIgnored()) {
                return;
            }
            if (visitedPages.contains(lowerPageUrl)) {
                return;
            }
            controller.log("Processing url " + sUrl, Level.INFO);
            visitedPages.add(lowerPageUrl);

            traversePage(sUrl);
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
                    parse(u, pages);
                } catch (Exception ex) {
                    controller.log(ex.getMessage(), Level.SEVERE);
                }
            }
        } finally {
            urls.pop();
            cleanUrls.pop();
        }
    }

    @Override
    protected void processTextNode(TextNode text) {
        addToPage(text.text());
    }

    @Override
    protected void processElement(Element currentNode) {
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
                        URL url = urls.peek();
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
    }

    private void addToPage(String text) {
        if (skipPages.contains(cleanUrls.peek())) {
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
            URL url = urls.peek();
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

}
