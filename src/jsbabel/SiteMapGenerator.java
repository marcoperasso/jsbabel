/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsbabel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import jsbabel.entities.Page;
import jsbabel.entities.Site;
import jsbabel.entities.TargetLocale;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Marco
 */
public class SiteMapGenerator {
 class CaseInsensitiveMap extends HashMap<String, String> {

    @Override
    public String put(String key, String value) {
       return super.put(key.toLowerCase(), value);
    }

    // not @Override because that would require the key parameter to be of type Object
    public String get(String key) {
       return super.get(key.toLowerCase());
    }
}
    private static final String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<urlset\n"
            + "    xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"\n"
            + "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
            + "    xsi:schemaLocation=\"http://www.sitemaps.org/schemas/sitemap/0.9\n"
            + "            http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd\">\n"
            + "</urlset>";

    public static synchronized SiteMapGenerator getMapGenerator() {
        if (generator == null) {
            generator = new SiteMapGenerator();
            generator.createMapInternal();
        }
        return generator;
    }
    private CaseInsensitiveMap map = new CaseInsensitiveMap();
    private HashMap<String, String> reverseMap = new HashMap<String, String>();
    private Document mapDocument;

    public SiteMapGenerator() {
     
    }

    public String decodeUrlToken(String id) {
        return reverseMap.get(id);
    }

    public String encodeUrlToken(String url) {
        String s = map.get(url);
        if (s != null) {
            return s;
        }
        return map.get(url + '/');
    }
    private static SiteMapGenerator generator = null;

    public static Document createMap() {
        return getMapGenerator().mapDocument;
    }

    static boolean owns(String requestURI) {
        return requestURI.startsWith("/m/");
    }

    static void getUrlTokens(String requestURI, StringBuilder loc, StringBuilder url) {
        int idx = requestURI.indexOf("/", 3);
        String s = requestURI.substring(3, idx);
        loc.append(s);
        s = requestURI.substring(idx + 1, requestURI.length());
        url.append(getMapGenerator().decodeUrlToken(s));
    }

    private void createMapInternal() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DataHelper dh = new DataHelper();
        try {
            //Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            mapDocument = db.parse(new ByteArrayInputStream(xml.getBytes()));
            mapDocument.setXmlStandalone(true);

            for (Site s : dh.getSites()) {
                for (TargetLocale targetLocale : s.getTargetLocales()) {
                    for (Page p : dh.getPages(s.getId())) {
                        if (p.isIgnored()) {
                            continue;
                        }
                        String url = s.getHost() + "/" + p.getPage();
                        String id = Helper.hexEncode(url);
                        
                        map.put(url, id);
                        reverseMap.put(id, url);

                        addUrl(targetLocale.getLocale(), url);
                    }
                }
            }
        } catch (Exception ex) {
            Helper.log(ex);
        } finally {
            dh.dispose();
        }
    }

    private void addUrl(String targetLocale, String url) throws DOMException, IOException {
        String targetUrl = getDynamicUrl(targetLocale, url, true);

        Element urlNode = (Element) mapDocument.getDocumentElement().appendChild(mapDocument.createElement("url"));
        Element locNode = (Element) urlNode.appendChild(mapDocument.createElement("loc"));
        locNode.appendChild(mapDocument.createTextNode(targetUrl));
        //Element freqNode = (Element) urlNode.appendChild(doc.createElement("changefreq"));
        //freqNode.appendChild(doc.createTextNode("daily"));
    }

    public static String getDynamicUrl(String targetLocale, String url, boolean absolute) {
        return String.format(
                "%s/m/%s/%s",
                absolute ? Helper.getExecutingHost() : "",
                targetLocale,
                getMapGenerator().encodeUrlToken(url));
    }
}
