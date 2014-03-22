/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsbabel.servlet;

import java.io.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import jsbabel.DataHelper;
import jsbabel.Helper;
import jsbabel.ProcedureController;
import jsbabel.PageParser;
import jsbabel.PageStringExtractor;
import jsbabel.entities.BaseString;
import jsbabel.entities.Page;
import jsbabel.entities.Parsedsite;
import jsbabel.entities.Stringstatistics;
import jsbabel.xliff.XLiffGenerator;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.hibernate.jdbc.Work;

/**
 *
 * @author perasso
 */
public class Test extends HttpServlet {

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
            throws ServletException, TransformerConfigurationException, TransformerException {
        try {
            /*response.getWriter().append("getProtocol: ");
            response.getWriter().append(request.getProtocol());
            response.getWriter().append("\ngetScheme: ");
            response.getWriter().append(request.getScheme());
            response.getWriter().append("\ngetServerName: ");
            response.getWriter().append(request.getServerName());
            response.getWriter().append("\ngetPathInfo: ");
            response.getWriter().append(request.getPathInfo());
            response.getWriter().append("\ngetPathTranslated: ");
            response.getWriter().append(request.getPathTranslated());
            
            
            response.getWriter().append("\ngetLocalAddr: ");
            response.getWriter().append(request.getLocalAddr());
            
            response.getWriter().append("\ngetLocalName: ");
            response.getWriter().append(request.getLocalName());
            
            
            response.getWriter().append("\ngetRequestURL: ");
            response.getWriter().append(request.getRequestURL());
            
            
            response.getWriter().append("\ngetRequestURI: ");
            response.getWriter().append(request.getRequestURI());*/
             XLiffGenerator creator = new XLiffGenerator();

             TransformerFactory transformerFactory = TransformerFactory.newInstance();
             Transformer transformer = transformerFactory.newTransformer();
             DOMSource source;
             try {
             source = new DOMSource(creator.parse("http://localhost:8080/test.html", "en-US", "it-IT"));
             StreamResult result = new StreamResult(response.getOutputStream());
             transformer.setOutputProperty(OutputKeys.INDENT, "yes");
             transformer.transform(source, result);
             } catch (ParserConfigurationException ex) {
             Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
             }

            /*HtmlImporter imp = new HtmlImporter();
             ByteArrayOutputStream bFormatOut = new ByteArrayOutputStream();
             ByteArrayOutputStream bXliffOut = new ByteArrayOutputStream();
             ByteArrayOutputStream bSkeletonOut = new ByteArrayOutputStream();
             try {
             final File file = new File(request.getSession().getServletContext().getRealPath("test1.html"));

             FileInputStream fisIn = new FileInputStream(file);

             byte[] bIn = new byte[(int) file.length()];
             fisIn.read(bIn);

             fisIn.close();
             try {
             imp.convert(
             Locale.ENGLISH,
             Charset.forName("UTF-8"),
             "TODOPERASSO",
             bIn,
             bFormatOut,
             bSkeletonOut,
             bXliffOut);
             } catch (ConversionException ex) {
             Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
             }
                
             FileOutputStream fos = new FileOutputStream(new File(request.getSession().getServletContext().getRealPath("test.html.xliff")));
             fos.write(bXliffOut.toByteArray());
             fos.close();
             fos = new FileOutputStream(new File(request.getSession().getServletContext().getRealPath("test.html.format")));
             fos.write(bFormatOut.toByteArray());
             fos.close();
             fos = new FileOutputStream(new File(request.getSession().getServletContext().getRealPath("test.html.skeleton")));
             fos.write(bSkeletonOut.toByteArray());
             fos.close();
             } catch (IOException ex) {
             Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
             } */
        } catch (IOException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private Long getId(final boolean sleep) throws ClassNotFoundException, HibernateException, InterruptedException {
        final DataHelper dh = new DataHelper();
        Transaction t = dh.getHSession().beginTransaction();
        //Parsedsite ps = new Parsedsite();
        // ps.setHost("marco");
        //dh.getHSession().saveOrUpdate(ps);
        final List<Long> l = new ArrayList<Long>();
        dh.getHSession().doWork(new Work() {
            @Override
            public void execute(Connection connection) throws SQLException {

                PreparedStatement selectStmt = null;
                ResultSet rs = null;
                selectStmt = connection.prepareStatement("select next_id(?,?);");
                selectStmt.setLong(1, 0);
                int ownerId = 17;
                selectStmt.setInt(2, ownerId);
                rs = selectStmt.executeQuery();
                rs.next();
                l.add(rs.getLong(1));
                rs.close();
                selectStmt.close();
                if (sleep) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }
        });
        t.commit();
        dh.dispose();

        return l.get(0);

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
        try {
            processRequest(request, response);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        try {
            processRequest(request, response);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
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

    private boolean parseSite(HttpServletResponse response, HttpServletRequest request) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        DataHelper dh = new DataHelper();
        try {
            String url = request.getParameter("url");
            if (Helper.isNullOrEmpty(url)) {
                return true;
            }
            ProcedureController controller = new ProcedureController();
            PageStringExtractor ex = new PageStringExtractor(null, controller, request.getSession());

            URL u = new URL(url);
            Criteria c = dh.getHSession().createCriteria(Parsedsite.class).add(Restrictions.eq("host", u.getHost()));
            if (c.uniqueResult() != null) {
                out.write("Already processed!");
                return true;
            }
            Transaction beginTransaction = dh.getHSession().beginTransaction();
            Parsedsite ps = new Parsedsite();
            ps.setHost(u.getHost());
            dh.getHSession().saveOrUpdate(ps);
            List<Page> pages = ex.parse(u, true);
            Hashtable<Integer, Integer> table = new Hashtable<Integer, Integer>();
            for (Page p : pages) {
                for (BaseString s : p.getBaseStrings()) {
                    Integer i = table.get(s.getText().length());
                    if (i == null) {
                        i = 1;
                    } else {
                        i++;
                    }
                    table.put(s.getText().length(), i);
                }
            }
            int len = 0;
            for (Integer key : table.keySet()) {
                Integer i = table.get(key);
                c = dh.getHSession().createCriteria(Stringstatistics.class).add(Restrictions.eq("stringlen", key));
                Stringstatistics ss = (Stringstatistics) c.uniqueResult();
                if (ss == null) {
                    ss = new Stringstatistics();
                    ss.setStringlen(key);
                    ss.setStringcount(0);
                }
                ss.setStringcount(ss.getStringcount() + i);
                len += i;
                dh.getHSession().saveOrUpdate(ss);
            }
            beginTransaction.commit();
            out.write(String.format("Pages: %s; strings: %d", pages.size(), len));
            out.write(controller.toString().replace("\n", "<br>"));
        } catch (Exception ex1) {
            out.write(ex1.toString());
        } finally {

            dh.dispose();
            out.close();
        }
        return false;
    }
}
