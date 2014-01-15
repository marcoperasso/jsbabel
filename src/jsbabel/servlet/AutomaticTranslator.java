package jsbabel.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jsbabel.DataHelper;
import jsbabel.Helper;
import jsbabel.Translations;
import jsbabel.entities.Page;
import jsbabel.entities.Site;

public class AutomaticTranslator extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 2655313802385814107L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/javascript");
        try {
            PrintWriter pw = resp.getWriter();

            String url = req.getParameter("src");
            DataHelper dh = new DataHelper();
            Site site = dh.getSite(req.getSession(), url);
            if (site == null) {
                return;
            }
            String targetLocale = req.getParameter("loc");
            Translations t = (Translations) req.getSession().getAttribute(Helper.getAutomaticTranslationKey(url, targetLocale));
            if (t != null) {
                pw.write(String.format("__babel.addAutomaticTranslations('%s');", t.serialize()));
            }
            else
            {
                 pw.write("__babel.addAutomaticTranslations('');");
            }
            req.getSession().setAttribute(Helper.getAutomaticTranslationKey(url, targetLocale), null);
        } catch (Exception e) {
            Helper.log(e);
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        DataHelper dh = new DataHelper();
        try {
            String url = req.getParameter("src");
            if (!dh.persistent(req, url)) {
                return;
            }
            String targetLanguage = req.getParameter("targetLocale");
            Translations tt = new Translations();
            String data = req.getParameter("strings");
            if (data != null) {
                tt.parse(data);
                Page p = dh.translateBlock(req.getSession(), url, tt, true);
                req.getSession().setAttribute(Helper.getAutomaticTranslationKey(url, targetLanguage), p.getTranslations(targetLanguage, false));
            }
            resp.getWriter().print("OK");
        } catch (Exception e) {
            Helper.log(e);
        } finally {
            dh.dispose();
        }
    }
}
