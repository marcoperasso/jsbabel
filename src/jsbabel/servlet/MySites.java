package jsbabel.servlet;

import java.io.IOException;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jsbabel.CommandData;
import jsbabel.DataHelper;
import jsbabel.Helper;
import jsbabel.Messages;
import jsbabel.entities.Site;
import jsbabel.entities.User;

public class MySites extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = -6246794568052463062L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

        resp.setContentType("text/json;charset=UTF-8");

        String cmd = req.getParameter("cmd");
        User user = Helper.getUser(req.getSession());
        if ("checksite".equals(cmd)) {
            try {
                String sitename = req.getParameter("sitename");
                boolean shouldNotExist = "true".equals(req.getParameter("shouldnotexist"));
                String host = checkSite(user, sitename, shouldNotExist);
                if (Helper.isNullOrEmpty(host)) {
                    throw new Exception(Messages.INVALID_SITE_URL);
                }

                CommandData.sendOK(resp.getWriter());
            } catch (Exception ex) {
                CommandData.sendError(ex, resp.getWriter());
            }
            return;
        }
        if (!Helper.isUserLogged(req.getSession())) {
            Helper.redirectForLogin(req, resp);
            return;
        }


        DataHelper dh = new DataHelper();
        try {

            if ("deletesite".equals(cmd)) {

                String siteid = req.getParameter("siteid");
                for (Site s : user.getSites()) {
                    if (s.getId().toString().equals(siteid)) {
                        user.getSites().remove(s);
                        break;
                    }
                }
            } else if ("add".equals(cmd)) {

                String sitename = req.getParameter("sitename");
                String host = checkSite(user, sitename, true);

                Site s = new Site();
                s.setHost(host);
                s.setUser(user);
                user.getSites().add(s);
            } else if ("sitechange".equals(cmd)) {
                String baseLanguage = (String) req.getParameter("baseLanguage");
                String[] targetLanguages = req.getParameterValues("targetLanguage");
                String sAnchor = req.getParameter("anchor");
                String siteid = req.getParameter("siteid");
                Site site = null;
                for (Site s : user.getSites()) {
                    if (s.getId().toString().equals(siteid)) {
                        site = s;
                        break;
                    }
                }
                if (site != null) {
                    site.setBaseLanguage(baseLanguage);
                    if (sAnchor != null && sAnchor.length() == 1) {
                        char anchor = sAnchor.charAt(0);
                        if (anchor != site.getAnchor()) {
                            site.setAnchor(anchor);
                            site.setOffset(0);
                        }
                    }
                    site.setTargetLanguages(targetLanguages);
                }
            } else {
                return;
            }
            dh.beginTransaction();
            dh.saveUser(user);
            for (Site s : user.getSites()) {
                dh.setSite(req.getSession(), s, false);
            }
            dh.commit();
            CommandData.sendOK(resp.getWriter());
        } catch (Exception ex) {
            CommandData.sendError(ex, resp.getWriter());
        } finally {
            dh.dispose();
        }
    }

    private String checkSite(User user, String sitename, boolean shouldNotExist) throws ServletException {
        try {
            URL url = new URL(sitename);
            url.openConnection().connect();
        } catch (Exception ex) {
            throw new ServletException(Messages.INVALID_SITE_URL, ex);
        }
        String host = Helper.getHost(sitename);
        if (shouldNotExist) {

            if (user != null) {
                for (Site s : user.getSites()) {
                    if (s.getHost().equalsIgnoreCase(host)) {
                        throw new ServletException(Messages.SITE_ALREADY_EXISTING);
                    }
                }
            }
            DataHelper dh = new DataHelper();
            try {
                if (dh.getSiteFromDB(host) != null) {
                    throw new ServletException(Messages.SITE_ALREADY_EXISTING);
                }
            } finally {
                dh.dispose();
            }
        }
        return host;
    }
}
