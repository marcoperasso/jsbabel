/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsbabel.servlet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jsbabel.Const;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;
import jsbabel.DataHelper;
import jsbabel.Helper;
import jsbabel.Messages;
import jsbabel.SiteMapGenerator;
import jsbabel.SiteStringExtractor;
import jsbabel.Translation;
import jsbabel.Translations;
import jsbabel.entities.StringType;
import org.jsoup.nodes.Attribute;

/**
 *
 * @author Perasso
 */
public class Mirror extends HttpServlet {

    private static final String[] invalidExtensions = {".jpg", ".png", ".gif", ".tif", ".pdf", ".zip"};
    String url = null;
    String targetLocale = null;
    Translations translations = null;
    boolean isBot;

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        isBot = Helper.isBot(request);
        response.setContentType("text/html;charset=UTF-8");
        DataHelper dh = new DataHelper();
        try {
            url = request.getParameter("url");
            if (Helper.isNullOrEmpty(url)) {
                if (isBot) {
                    return;
                }
                throw new ServletException(Messages.INVALID_SITE_URL);
            }
            targetLocale = request.getParameter("loc");
            if (Helper.isNullOrEmpty(targetLocale)) {
                if (isBot) {
                    return;
                }
                throw new ServletException(Messages.INVALID_SITE_URL);
            }
            translations = dh.getTranslations(request.getSession(), url, targetLocale, true, false);
            String html = translate(url, targetLocale);
            byte[] buff = html.getBytes(Charset.forName("utf-8"));
            response.setContentLength(buff.length);
            response.getOutputStream().write(buff);
        } finally {
            dh.dispose();
        }
    }

    public String translate(String pageUrl, final String targetLocale) throws MalformedURLException {
        final URL url = new URL(pageUrl);

        try {

            Document document = Jsoup.connect(url.toString())
                    .userAgent(Const.UserAgent)
                    .referrer(Helper.getExecutingHost())
                    .get();

            document.traverse(new NodeVisitor() {
                @Override
                public void head(Node htmlNode, int arg1) {
                }

                @Override
                public void tail(Node htmlNode, int arg1) {
                    try {
                        if (htmlNode instanceof Element) {
                            Element currentNode = (Element) htmlNode;

                            for (Attribute attr : currentNode.attributes()) {
                                if (SiteStringExtractor.isAttributeToTranslate(currentNode, attr)) {
                                    attr.setValue(translations.translate(attr.getValue(), true));
                                }
                            }
//                            if ("script".equals(currentNode.nodeName())) {
//                                currentNode.empty();
//                            } else 
                            if ("a".equals(currentNode.nodeName())) {
                                String addr = currentNode.attr("href");
                                if (addr != null) {
                                    try {
                                        URL u = new URL(url, addr);
                                        if (u.getHost().equals(url.getHost())) {
                                            if (isValidAddress(u)) {
                                                currentNode.attr("href", SiteMapGenerator.getDynamicUrl(targetLocale, u.toString(), true));
                                            } else {
                                                currentNode.attr("href", u.toString());
                                            }
                                        }

                                    } catch (MalformedURLException e) {
                                    }
                                }
                            }
                        } else if (htmlNode instanceof TextNode) {
                            if (skipContent(htmlNode.parent())) {
                                return;
                            }
                            TextNode text = (TextNode) htmlNode;
                            if (text.text() != null) {
                                text.text(translations.translate(text.text(), true));
                            }
                        }
                    } catch (Exception e) {
                        Helper.log(e);
                    }

                }
            });
            StringBuilder sb = new StringBuilder();
            sb.append("<base href=\"");
            sb.append(Helper.getHost(url, true));
            sb.append("\"></base>\n");
            sb.append("<link rel=\"canonical\" href=\"");
            sb.append(SiteMapGenerator.getDynamicUrl(targetLocale, pageUrl, true));
            sb.append("\"/>");
            if (!isBot) {
                sb.append("<script type=\"text/javascript\">");
                sb.append("window.location.href=\"");
                sb.append(url);
                sb.append("\"");
                sb.append("</script>\n");
            }
            document.head().prepend(sb.toString());

            return document.html();
        } catch (Exception e) {
            Helper.log(e);
        }

        return "";
    }

    private boolean isValidAddress(URL u) {
        String path = u.getPath().toLowerCase();
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

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
