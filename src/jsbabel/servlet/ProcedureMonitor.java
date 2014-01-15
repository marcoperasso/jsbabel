package jsbabel.servlet;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jsbabel.Const;
import jsbabel.ProcedureController;

public class ProcedureMonitor extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = -3187112227738164485L;

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String sessionId = req.getParameter(Const.SessionIdParam);

            ProcedureController c = (ProcedureController) req.getSession().getAttribute(sessionId);
            if (c != null) {
                String stop = req.getParameter("stop");
                if ("true".equals(stop)) {
                    c.setAborted();
                }
                StringBuilder sb = new StringBuilder();
                sb.append("{\"terminated\": ");
                sb.append(c.isFinished() || c.isAborted());
                sb.append(", \"message\": \"");
                sb.append(c.getLastMessage());
                sb.append("\"}");

                resp.setContentType("text/json;charset=UTF-8");
                resp.getWriter().write(sb.toString());
            }

        } catch (Exception e) {
            e.printStackTrace(resp.getWriter());
        }
    }
}
