package jsbabel.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;
import jsbabel.CommandData;
import jsbabel.Const;
import jsbabel.DataHelper;
import jsbabel.HTMLGenerator;
import jsbabel.Helper;
import jsbabel.Messages;
import jsbabel.ProcedureController;
import jsbabel.PageParser;
import jsbabel.PageStringExtractor;
import jsbabel.entities.Page;
import jsbabel.entities.Site;
import jsbabel.entities.User;
import jsbabel.xliff.XLiffGenerator;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;

/**
 * Servlet implementation class SiteParserServlet
 */
public class SiteManager extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public SiteManager() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String cmd = request.getParameter("cmd");
        response.setContentType("text/json;charset=UTF-8");
        try {
            if ("deletestrings".equals(cmd)) {
                Site site = checkValidSite(request);
                List<Page> pages = (List<Page>) request.getSession().getAttribute(Helper.getPageCacheKey(site.getId()));
                if (pages == null) {
                    throw new ServletException(Messages.INVALID_STATE);
                }
                DataHelper dh = new DataHelper();

                try {
                    dh.delete(pages,
                            request.getParameterValues("pages[]"),
                            request.getParameterValues("strings[]"),
                            request.getParameterValues("longstrings[]"));
                    //annullo la cache per far sentire la nuova traduzione
                    request.getSession().setAttribute(Helper.getSiteKey(site.getHost()), site);

                } finally {
                    dh.dispose();
                }
            } else if ("ignorestrings".equals(cmd)) {
                Site site = checkValidSite(request);
                List<Page> pages = (List<Page>) request.getSession().getAttribute(Helper.getPageCacheKey(site.getId()));
                if (pages == null) {
                    throw new ServletException(Messages.INVALID_STATE);
                }
                DataHelper dh = new DataHelper();

                try {
                    dh.ignore(site,
                            Boolean.parseBoolean(request.getParameter("delete")),
                            pages,
                            request.getParameterValues("pages[]"),
                            request.getParameterValues("strings[]"),
                            request.getParameterValues("longstrings[]"));
                    //annullo la cache per far sentire la nuova traduzione
                    request.getSession().setAttribute(Helper.getSiteKey(site.getHost()), site);

                } finally {
                    dh.dispose();
                }
            } else if ("updatestring".equals(cmd)) {
                Site site = checkValidSite(request);
                List<Page> pages = (List<Page>) request.getSession().getAttribute(Helper.getPageCacheKey(site.getId()));
                if (pages == null) {
                    throw new ServletException(Messages.INVALID_STATE);
                }
                DataHelper dh = new DataHelper();
                try {
                    dh.update(site, pages,
                            Long.parseLong(request.getParameter("stringid")),
                            Boolean.parseBoolean(request.getParameter("islong")),
                            request.getParameter("text"),
                            request.getParameter("targetlocale"));
                    //annullo la cache per far sentire la nuova traduzione
                    request.getSession().setAttribute(Helper.getSiteKey(site.getHost()), site);
                } finally {
                    dh.dispose();
                }
            } else if ("importxliff".equals(cmd)) {
                XLiffGenerator gen = new XLiffGenerator();

                FileItemFactory factory = new DiskFileItemFactory();
                ServletFileUpload upload = new ServletFileUpload(factory);
                List<FileItem> items = upload.parseRequest(request); // This line is where it died.
                for (FileItem item : items) {
                    if (!item.isFormField()) {
                        String filename = item.getName();
                        if (filename.toLowerCase().endsWith(".xlf")) {
                            gen.importStream(item.getInputStream());
                        } else if (filename.toLowerCase().endsWith(".zip")) {
                            ZipInputStream zis = new ZipInputStream(item.getInputStream());
                            ZipEntry entry = null;
                            while ((entry = zis.getNextEntry()) != null) {
                                String name = entry.getName();
                                if (!name.toLowerCase().endsWith(".xlf")) {
                                    throw new ServletException(String.format(Messages.INVALID_IMPORT_FILE, name, ".xlf"));
                                }
                                gen.importStream(zis);
                            }
                        } else {
                            throw new ServletException(String.format(Messages.INVALID_IMPORT_FILE, filename, "(.xlf, .zip)"));
                        }

                    }
                }
            } else if ("export".equals(cmd)) {
                final String sessionId = request.getParameter(Const.SessionIdParam);
                ProcedureController controller = new ProcedureController();
                //controllo che l'utente sia connesso
                Site site = checkValidSite(request);
                boolean plain = "true".equals(request.getParameter("plain"));

                request.getSession().setAttribute(sessionId, controller);
                DataHelper dh = new DataHelper();

                try {
                    String[] pageIds = request.getParameterValues("pages[]");
                    if (pageIds == null) {
                        return;
                    }
                    List<Page> pages = (List<Page>) request.getSession().getAttribute(Helper.getPageCacheKey(site.getId()));
                    if (pages == null) {
                        throw new ServletException(Messages.INVALID_STATE);
                    }
                    ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());

                    final String bareHost = Helper.getBareHost(site.getHost());
                    String targetLocale = request.getParameter("targetlocale");
                    response.setContentType("application/zip");
                    response.setHeader("Content-Disposition", "attachment;filename=" + bareHost + '.' + targetLocale + ".zip");
                    final boolean exportHtml = "html".equals(request.getParameter("format"));
                    for (String ids : pageIds) {
                        Long id = Long.parseLong(ids);
                        for (Page p : pages) {
                            if (p.getId().equals(id)) {
                                URL url = new URL(site.getHost() + "/" + p.getPage());
                                final String sUrl = url.toString();
                                StringBuilder sb = new StringBuilder();
                                sb.append(bareHost);
                                String page = p.getPage().replace('/', '.');
                                if (page.length() > 0) {
                                    sb.append('.');
                                    sb.append(page);
                                }
                                sb.append(exportHtml ? ".html" : ".xlf");
                                dh.reattach(p);

                                ZipEntry ze = new ZipEntry(sb.toString());
                                try {
                                    zos.putNextEntry(ze);
                                    if (exportHtml) {
                                        generateHTML(controller, p, targetLocale, plain, sUrl, zos);
                                    } else {
                                        generateXLiff(p, site.getBaseLanguage(), targetLocale, plain, sUrl, zos);
                                    }

                                } catch (Exception ex) {
                                    controller.log(ex.getLocalizedMessage(), Level.SEVERE);
                                }
                                zos.closeEntry();
                                break;
                            }
                        }
                    }
                    zos.close();
                } catch (Exception ex) {
                    controller.log(ex.getLocalizedMessage(), Level.SEVERE);
                } finally {
                    dh.dispose();
                    controller.setFinished();
                }
            } else {
                final String sessionId = request.getParameter(Const.SessionIdParam);
                ProcedureController controller = new ProcedureController();
                //controllo che l'utente sia connesso
                Site site = checkValidSite(request);

                request.getSession().setAttribute(sessionId, controller);
                PageStringExtractor sp = new PageStringExtractor(site, controller, request.getSession());
                try {
                    if ("refresh".equals(cmd)) {
                        String[] pageIds = request.getParameterValues("pages[]");
                        if (pageIds == null) {
                            return;
                        }
                        List<Page> pages = (List<Page>) request.getSession().getAttribute(Helper.getPageCacheKey(site.getId()));
                        if (pages == null) {
                            throw new ServletException(Messages.INVALID_STATE);
                        }
                        List<URL> urls = new ArrayList<URL>(pageIds.length);
                        for (String ids : pageIds) {
                            Long id = Long.parseLong(ids);
                            for (Page p : pages) {
                                if (p.getId().equals(id)) {
                                    urls.add(new URL(site.getHost() + "/" + p.getPage()));
                                    break;
                                }
                            }
                        }
                        sp.parse(urls, false);
                    } else {
                        if ("addmissing".equals(cmd)) {
                            List<Page> pages = (List<Page>) request.getSession().getAttribute(Helper.getPageCacheKey(site.getId()));
                            if (pages == null) {
                                throw new ServletException(Messages.INVALID_STATE);
                            }

                            for (Page p : pages) {
                                sp.addSkipUpdate(p);
                            }
                        }

                        String src = request.getParameter("src");

                        URL url = new URL(src);
                        sp.parse(url, true);

                    }
                } catch (Exception ex) {
                    controller.log(ex.getLocalizedMessage(), Level.SEVERE);
                    throw ex;
                } finally {
                    controller.setFinished();
                    sp.dispose();
                }

            }

            CommandData.sendOK(response.getWriter());
        } catch (Exception ex) {
            CommandData.sendError(ex, response.getWriter());
        }
    }

    private void generateHTML(ProcedureController controller, Page p, String targetLocale, boolean plain, final String sUrl, ZipOutputStream zos) throws IOException, DOMException, TransformerException, ParserConfigurationException, XPathExpressionException, IllegalArgumentException {
        HTMLGenerator creator = new HTMLGenerator(controller);
        creator.setTranslations(p.getTranslations(targetLocale, true));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter result = new PrintWriter(baos);
        org.jsoup.nodes.Document doc = creator.parse(sUrl);
        result.write(doc.html());
        result.flush();
        zos.write(baos.toByteArray());
        result.close();
        baos.close();
    }

    private void generateXLiff(Page p, String baseLocale, String targetLocale, boolean plain, final String sUrl, ZipOutputStream zos) throws IOException, DOMException, TransformerException, ParserConfigurationException, XPathExpressionException, IllegalArgumentException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        XLiffGenerator creator = new XLiffGenerator();
        creator.setTranslations(p.getTranslations(targetLocale, true));
        Document doc = plain
                ? creator.createDocument(sUrl, baseLocale, targetLocale)
                : creator.parse(sUrl, baseLocale, targetLocale);
        creator.applyTranslations();
        DOMSource source = new DOMSource(doc);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(baos);
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(source, result);
        zos.write(baos.toByteArray());
        baos.close();
    }

    private Site checkValidSite(HttpServletRequest request) throws ServletException, NumberFormatException {
        if (!Helper.isUserLogged(request.getSession())) {
            throw new ServletException(Messages.UserNotLogged);
        }
        User user = Helper.getUser(request.getSession());
        Site site = user.getSite(Integer.parseInt(request.getParameter("siteid")));
        if (site == null) {
            throw new ServletException(Messages.INVALID_SITE);
        }
        return site;
    }

}
