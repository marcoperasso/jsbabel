package jsbabel.servlet;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jsbabel.DataHelper;
import jsbabel.Helper;
import jsbabel.Translations;
import jsbabel.entities.Site;
import jsbabel.entities.StringType;

public class Translator extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 5297215901741438814L;

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        DataHelper dh = new DataHelper();
        try {
            
            String url = req.getParameter("src");
            String targetLanguage = req.getParameter("targetLocale");
            Site site = dh.getSite(req.getSession(), url);
            if (site == null) {
                return;
            }
            if (!dh.persistent(req, url)) {
                return;
            }
            
            boolean append = "true".equals(req.getParameter("appendToExisting"));
            Translations tt = new Translations();
            int i = 0;
            String base;
            while ((base = req.getParameter("b" + i)) != null) {
                tt.add(base, 
                        req.getParameter("t" + i), 
                        "true".equals(req.getParameter("p" + i)), 
                        StringType.Text);
                i++;
            }
            i = 0;
            while ((base = req.getParameter("bm" + i)) != null) {
                tt.add(base, 
                        req.getParameter("tm" + i), 
                        "true".equals(req.getParameter("pm" + i)), 
                        StringType.Move);
                i++;
            }
            
             while ((base = req.getParameter("bi" + i)) != null) {
                tt.add(base, 
                        req.getParameter("ti" + i), 
                        "true".equals(req.getParameter("pi" + i)), 
                        StringType.Ignore);
                i++;
            }
            dh.setTranslations(req.getSession(), tt, url, targetLanguage, append);

        } catch (Exception e) {
            Helper.log(e);
        } finally {
            dh.dispose();
        }
    }
}
