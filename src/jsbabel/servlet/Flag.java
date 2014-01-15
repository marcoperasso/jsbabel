/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsbabel.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jsbabel.Helper;

/**
 *
 * @author Marco
 */
public class Flag extends HttpServlet {

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

        ServletOutputStream stream = response.getOutputStream();
        BufferedInputStream buf = null;
        FileInputStream fis = null;
        try {
            String loc = request.getParameter("loc");
            Locale l = Helper.fromLanguageCode(loc);
            String country = l.getCountry();
            String fileName = country.toLowerCase() + ".png";
            String path = request.getSession().getServletContext().getRealPath("/img/flags") + File.separator +  fileName;
            //set response headers
            response.setContentType("img/png");
            //response.addHeader("Content-Disposition", "attachment; filename=" + fileName);

            File file = new File(path);
            response.setContentLength((int) file.length());
            fis = new FileInputStream(file);
            buf = new BufferedInputStream(fis);
            int readBytes;
            //read from the file; write to the ServletOutputStream
            while ((readBytes = buf.read()) != -1) {
                stream.write(readBytes);
            }
        } catch (Exception ioe) {
            Helper.log(ioe);
            throw new ServletException(ioe.getMessage());
        } finally {
            if (stream != null) {
                stream.close();
            }
            if (fis != null) {
                fis.close();
            }
            if (buf != null) {
                buf.close();
            }
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
        return "Short description";
    }// </editor-fold>
}
