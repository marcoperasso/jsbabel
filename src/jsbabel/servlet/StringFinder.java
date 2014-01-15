package jsbabel.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Locale;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jsbabel.ClientData;
import jsbabel.Const;
import jsbabel.DataHelper;
import jsbabel.Helper;
import jsbabel.LocaleData;
import jsbabel.Translations;
import jsbabel.entities.Site;
import jsbabel.entities.TargetLocale;

public class StringFinder extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 4683853315327024463L;

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/javascript");
        PrintWriter pw = resp.getWriter();

        String url = req.getParameter("src");
        DataHelper dh = new DataHelper();
        try {
            boolean persistent = dh.persistent(req, url);

            Site site = dh.getSite(req.getSession(), url);
            if (site == null) {
                return;
            }
            boolean translateMode = Helper.isTranslateMode(site, req.getSession());
            String clientDataVer = req.getParameter("vData");
            String clientStringsVer = req.getParameter("vStrings");
            String serverVer = Integer.toString(site.getTranslationVersion() + Const.ClientCacheVersion);

            String targetLocale = req.getParameter("loc");
            String baseLocale = site.getBaseLanguage();
            if (Helper.isNullOrEmpty(baseLocale)) {
                baseLocale = Helper.getLanguageCode(req.getLocale());
            }
            String translations = "";
            String clientData = "null";
            boolean send = false;
            String sServerVer = serverVer.toString();
            if (translateMode || !sServerVer.equals(clientDataVer)) {
                ClientData data = new ClientData();
                data.bl = baseLocale;
                //la lingua visualizzata va sempre per prima nella dropdown delle bandiere
                data.ld.add(getLocaleFlagData(targetLocale));
                if (!baseLocale.equals(targetLocale)) {//lo aggiungo se non l'ho già messo in testa
                    data.ld.add(getLocaleFlagData(baseLocale));
                }
                for (TargetLocale loc : site.getTargetLocales()) {
                    if (!loc.getLocale().equals(targetLocale)) {//lo aggiungo se non l'ho già messo in testa
                        data.ld.add(getLocaleFlagData(loc.getLocale()));
                    }
                }
                data.a = site.getAnchor();
                data.x = site.getOffset();
                data.y = site.getTop();
                clientData = data.toJSON();
                send = true;
            }
            if (translateMode || !sServerVer.equals(clientStringsVer)) {
                if (!baseLocale.equals(targetLocale)) {
                    Translations t = dh.getTranslations(req.getSession(), url, targetLocale, persistent, translateMode);
                    translations = t.serialize();
                    if (!Helper.isNullOrEmpty(translations)) {
                        send = true;
                    }
                }
            }

            if (send) {
                pw.write(String.format("__babel.setTranslationData(%s, %s, '%s', %s);", clientData, serverVer, translations, serverVer));
            }


            if (translateMode) {
                pw.write(String.format("__babel.addTranslatorScripts(%s);", site.isDemoMode()));
            }

        } catch (Exception e) {
            Helper.log(e);
        } finally {
            dh.dispose();
        }
    }

    private LocaleData getLocaleFlagData(String loc) {
        LocaleData d = new LocaleData();
        Locale l = Helper.fromLanguageCode(loc);
        String country = l.getCountry();
        d.l = loc;
        d.u = String.format("/img/flags/%s.png", country.toLowerCase());
        d.t = Helper.encodeURIComponent(l.getDisplayName(l));
        return d;
    }
}
