/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsbabel.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jsbabel.CommandData;
import jsbabel.Const;
import jsbabel.Crypter;
import jsbabel.DataHelper;
import jsbabel.Helper;
import jsbabel.Messages;
import jsbabel.entities.User;
import jsbabel.entities.ValidationKey;

/**
 *
 * @author Marco
 */
public class CommandExecutor extends HttpServlet {

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
        response.setContentType("text/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            String cmd = (String) request.getParameter("cmd");
            if ("resetpwd".equals(cmd)) {
                String email = request.getParameter("email");
                DataHelper dh = new DataHelper();
                try {
                    User user = dh.getUserByMail(email);
                    if (user == null) {
                        Helper.requireCaptcha(request.getSession());
                        throw new Exception(Messages.InvalidEMail);
                    }
                    ValidationKey key = dh.saveUserToValidate(user);
                    user.sendResetPwdMail(key, request);
                } finally {
                    dh.dispose();
                }
                CommandData.sendOK(out);
            } else if ("logoff".equals(cmd)) {
                request.getSession().setAttribute(Const.UserKey, null);
                CommandData.sendOK(out);
            } else if ("login".equals(cmd)) {
                String email = request.getParameter("email");
                DataHelper dh = new DataHelper();
                try {
                    Helper.testCaptcha(request, false);
                    User user = dh.getUserByMail(email);
                    if (user == null || !user.isActive()) {
                        Helper.requireCaptcha(request.getSession());
                        throw new Exception(Messages.InvalidEMail);
                    }
                    String pwd = request.getParameter("pwd");
                    if (Helper.isNullOrEmpty(pwd)) {
                        Helper.requireCaptcha(request.getSession());
                        throw new Exception(Messages.InvalidPassword);
                    }
                    if (!user.getPassword().equals(Crypter.decrypt(pwd, request.getSession()))) {
                        Helper.requireCaptcha(request.getSession());
                        throw new Exception(Messages.InvalidPassword);
                    }
                    request.getSession().setAttribute(Const.UserKey, user);
                    CommandData.sendOK(out);

                } finally {
                    dh.dispose();
                }
            }
            

        } catch (Exception ex) {
            Helper.log(ex);
            CommandData.sendError(ex, out);

        } finally {
            out.close();
        }
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
        return "Executes client commands";
    }// </editor-fold>
}
