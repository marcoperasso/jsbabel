package jsbabel.servlet;

import java.io.IOException;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jsbabel.ClientData;
import jsbabel.CommandData;
import jsbabel.DataHelper;
import jsbabel.Helper;
import jsbabel.Messages;

import jsbabel.entities.Site;

public class Data extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 8586328962180352932L;

    /**
     *
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            resp.setContentType("text/json;charset=UTF-8");
            String left = (String) req.getParameter("left");
            String top = (String) req.getParameter("top");
            String url = (String) req.getParameter("src");
            DataHelper dh = new DataHelper();
            try {
                boolean persistent = dh.persistent(req, url);
                Site site = dh.getSite(req.getSession(), url);
                if (site == null) {
                    throw new ServletException(Messages.INVALID_SITE);
                }
                site.setOffset(Integer.parseInt(left));
                site.setTop(Integer.parseInt(top));
                dh.setSite(req.getSession(), site, persistent);
                CommandData.sendOK(resp.getWriter());
            } finally {
                dh.dispose();
            }
        } catch (Exception ex) {
            Helper.log(ex);
            CommandData.sendError(ex, resp.getWriter());
        }
    }
}
