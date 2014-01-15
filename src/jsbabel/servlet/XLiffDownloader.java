/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsbabel.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import jsbabel.DataHelper;
import jsbabel.Helper;
import jsbabel.entities.DemoTrial;
import jsbabel.xliff.XLiffGenerator;

/**
 *
 * @author perasso
 */
public class XLiffDownloader extends HttpServlet {

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
            throws ServletException {
        DataHelper dh = new DataHelper();
        try {
            final String src = request.getParameter("url");
            final String loc = request.getParameter("baseLanguage");
            final String targetLoc = request.getParameter("targetLanguage");
            response.setContentType("text/xml;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=text-segments.xlf");
            dh.addDemoTrial(src, DemoTrial.TrialType.XLIFF);
            XLiffGenerator creator = new XLiffGenerator();

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source;
            source = new DOMSource(creator.parse(src, loc, targetLoc));
            StreamResult result = new StreamResult(response.getOutputStream());
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);

        } catch (Exception ex) {
            Helper.log(ex);
        } finally {
            dh.dispose();
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
