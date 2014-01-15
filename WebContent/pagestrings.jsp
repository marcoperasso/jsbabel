<%-- 
    Document   : pagestrings
    Created on : 17-feb-2013, 9.49.00
    Author     : Marco
--%>
<%@page import="java.util.*"%> 
<%@page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@page import="jsbabel.*"%>
<%@page import="jsbabel.entities.*"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%if (!Helper.isUserLogged(session)) {
        Helper.redirectForLogin(request, response);
        return;
    }
    
    User user = Helper.getUser(session);
    Site site = user.getSite(Integer.parseInt(request.getParameter("siteid")));
    if (site == null) {
        throw new ServletException(Messages.INVALID_SITE);
    }
    int index = Integer.parseInt(request.getParameter("pageindex"));
    Locale l = Helper.fromLanguageCode(site.getBaseLanguage());
    String baseLocaleDesc = l.getDisplayName(l);
    String targetLocale = request.getParameter("targetlocale");
    l = Helper.fromLanguageCode(targetLocale);
    String targetLocaleDesc = l.getDisplayName(l);
    
    List<Page> pages = (List<Page>) session.getAttribute(Helper.getPageCacheKey(site.getId()));
    if (pages == null) {
        throw new ServletException(Messages.INVALID_STATE);
    }
    DataHelper dh = new DataHelper();
    try {
        ITargetString ts;
        Page pg = pages.get(index);
        dh.reattach(pg);
        pg.sortStrings();
%>
<table class="strings">
    <thead>
    <th class="checkboxes"><input type="checkbox" class="multistringselector"/></th>
    <th><%=baseLocaleDesc%></th>
    <th><%=targetLocaleDesc%></th>
</thead>
<tbody>
    <%    for (IBaseString bs : pg) {%>
    <tr class="stringrow ignorable <%=bs.getType() == StringType.Ignore ? "ignore" : "notignore"%>">
        <td class="checkboxes">
            <a href="javascript:void()" class="includestring whenignore" title="Include in translation"><img class="rowcommand clickable" src="img/notranslate.png"></a>
            <a href="javascript:void()" class="ignorestring whennotignore" title="Exclude from translation"><img class="rowcommand  clickable" src="img/translate.png"></a>
            <input type="checkbox" class="stringselector <%=bs instanceof BaseLongString ? "baselongstring" : "basestring"%>" stringid="<%=bs.getId()%>"
        </td>
        <td class="basestring"><%=StringEscapeUtils.escapeHtml(bs.getText())%></td>
        <td class="targetstring"><%=StringEscapeUtils.escapeHtml((ts = bs.getTargetString(targetLocale)) == null ? "" : ts.getText())%></td>
    </tr>
    <%                }%>

</tbody></table>
<%} catch (Exception ex) {
        Helper.log(ex);
    } finally {
        dh.dispose();
    }%>