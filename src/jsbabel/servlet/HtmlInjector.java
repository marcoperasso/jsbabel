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
import jsbabel.DataHelper;
import jsbabel.Helper;
import jsbabel.entities.DemoTrial;
import jsbabel.entities.Site;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeVisitor;

public class HtmlInjector extends HttpServlet {

    private static final long serialVersionUID = 8586328962180352932L;

    /**
     *
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        DataHelper dh = new DataHelper();
        try {
            String src = req.getParameter("src");
            String baseLanguage = req.getParameter("baseLanguage");
            String targetLanguage = req.getParameter("targetLanguage");
            String html = inject(src, req, baseLanguage, targetLanguage);
            Site site = dh.getSite(req.getSession(), src);
            if (site == null) {
                site = new Site();
                site.setHost(Helper.getHost(src));
                dh.setSite(req.getSession(), site, false);
                site.setDemoMode(true);
            }
            site.setBaseLanguage(baseLanguage);
            site.setTargetLanguages(new String[]{targetLanguage});
            dh.addDemoTrial(src, DemoTrial.TrialType.TRANSLATE);
            resp.setContentType("text/html");
            resp.setCharacterEncoding("utf-8");
            byte[] buff = html.getBytes(Charset.forName("utf-8"));
            resp.setContentLength(buff.length);
            resp.getOutputStream().write(buff);
        } catch (Exception ex) {
            Helper.log(ex);
            throw new ServletException(ex);
        } finally {
            dh.dispose();
        }
    }

    public String inject(final String src, HttpServletRequest req, final String baseLanguage, final String targetLanguage) throws IOException {

        Document doc = Jsoup.connect(src)
                .userAgent(Const.UserAgent)
                .referrer(Helper.getExecutingHost())
                .get();
        doc.head().prepend(String.format("<base href=\"%s\"></base>", src));
        StringBuilder sb = new StringBuilder();
        sb.append("<script src=\"http://");
        sb.append(req.getServerName());
        sb.append(':');
        sb.append(req.getServerPort());
        sb.append("/js/babel.js\"></script>");

        doc.traverse(new NodeVisitor() {
            @Override
            public void head(Node node, int i) {
            }

            @Override
            public void tail(Node node, int i) {
                if (node.nodeName().equals("frame")) {
                    try {
                        URL u = new URL(new URL(src), node.attr("src"));
                        String url = String.format(
                                "%s/servlet/htmlInjector?baseLanguage=%s&targetLanguage=%s&src=%s",
                                Helper.getExecutingHost(),
                                baseLanguage,
                                targetLanguage,
                                u.toString());
                        node.attr("src", url);
                    } catch (MalformedURLException ex) {
                        Helper.log(ex);
                    }

                }
            }
        });
        if (doc.body() != null) {
            doc.body().append(sb.toString());
        } else {
            doc.head().append(sb.toString());
        }

        return doc.html();

    }
}
