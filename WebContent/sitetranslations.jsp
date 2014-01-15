<%-- 
    Document   : sitemap
    Created on : Dec 10, 2012, 4:08:36 PM
    Author     : perasso
--%>
<%@page import="java.util.*"%> 
<%@page import="jsbabel.*"%>
<%@page import="jsbabel.entities.*"%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <%
        DataHelper dh = new DataHelper();
        try {
            if (!Helper.isUserLogged(session)) {
                Helper.redirectForLogin(request, response);
                return;
            }

            User user = Helper.getUser(session);
            Site site = user.getSite(Integer.parseInt(request.getParameter("siteid")));
            if (site == null) {
                throw new ServletException(Messages.INVALID_SITE);
            }
            Locale l = Helper.fromLanguageCode(site.getBaseLanguage());
            String targetLocale = request.getParameter("targetlocale");
            l = Helper.fromLanguageCode(targetLocale);

            List<Page> pages = null;//(List<Page>) session.getAttribute(Helper.getPageCacheKey(site.getId()));
            pages = dh.getPages(site.getId());
            session.setAttribute(Helper.getPageCacheKey(site.getId()), pages);

    %>
    <head>
        <jsp:include page="WEB-INF/jspf/commonhead.jspf"></jsp:include>
        <title>Translations managment for <%=site.getHost()%></title>
        <style type="text/css">

            .strings, .strings th, .strings td
            {
                border:1px solid orange;
            }
            .strings th{
                color:orange;
            }
            .strings{ margin-left: 10px;  width: 100%}
            .stringscontainer{display: none;}
            .stringhandle{
                cursor: pointer;
                background-position: center;
                background-repeat: no-repeat;
                background-image: url("/img/plus.png");
                background-color: transparent;
                display: inline-block;
                height: 20px;
                width: 20px;
            }
            .pagetitle{
                display: inline-block;
            }
            .minus{
                background-image: url("/img/minus.png");
            }
            .checkboxes
            {
                text-align: right;
                width:65px;
            }
            .toolbar
            {
                padding: 20px;
            } 
            .toolbar img
            {
                height: 48px;
                width: 48px;
            }
            td.stringcount, td.wordcount, td.translatedwordcount, td.progress
            {
                text-align: right;
                padding:    0px 0px 0px 5px;
            }
            td.incomplete
            {
                color:red;
            }
            td.complete
            {
                color:green;
            }
            tr.rowheader
            {
                text-align:center;
            }
            td.basestring
            {
                width: auto;
                min-width: 400px;
            }
            tr.notignore td.targetstring
            {
                width: auto;
                min-width: 400px;
                cursor: pointer;
            }
            textarea.targettext
            {
                width: 100%;
                height: 100%;
            }
            img.rowcommand{
                width:16px;
                height:16px;
            }
            .toolbar fieldset
            {
                display: inline-block;
                font-size: 12px;

            }
            .toolbar a
            {

            }
        </style>

        <script type="text/javascript">
            function getHost() {
                return "<%=site.getHost()%>";
            }
            function getTargetLocale() {
                return "<%=targetLocale%>";
            }
            function getSiteId() {
                return <%=site.getId()%>;
            }
        </script>
        <script type="text/javascript" src="/js/procedurecontroller.js"></script>
        <script type="text/javascript" src="/js/sitetranslations.js"></script>

    </head>
    <body>
        <jsp:include page="WEB-INF/jsp/header.jsp" /> 
        <div class="content">
            <h1>Translations managment for <%=site.getHost()%></h1>
            <div class="toolbar">
                <a href="javascript:void()" class="generatemissingbutton clickable" title="Extract missing pages from site"><img src="img/gear.png"></a>
                <a href="javascript:void()" class="refreshbutton clickable" title="Update selected pages"><img src="img/gearrefresh.png"></a>
                <a href="javascript:void()" class="includebutton clickable" title="Include selected pages or text blocks in translation"><img src="img/translate.png"></a>
                <a href="javascript:void()" class="ignorebutton clickable" title="Exclude selected pages or text blocks from translation"><img src="img/notranslate.png"></a>
                <a href="javascript:void()" class="exportxliffbutton clickable" title="Export selected pages in XLIFF format"><img src="img/exportxliff.png"></a>
                <a href="javascript:void()" class="importxliffbutton clickable" title="Import XLIFF files"><img src="img/importxliff.png"></a>
                <a href="javascript:void()" class="deletebutton clickable" title="Delete selected pages or text blocks (only if they are not translated)"><img src="img/deletebig.png"></a>
                <fieldset>
                    <legend>I want to hide:</legend>
                    <input type="checkbox" class="filter" id="ignore"><span>Text excluded from translation</span>
                    <input type="checkbox" class="filter" id="notignore"><span>Text to translate</span>
                    <input type="checkbox" class="filter" id="translated"><span>Translated text</span>
                    <input type="checkbox" class="filter" id="nottranslated"><span>Non yet translated text</span>
                </fieldset>
                <div class="generationoutput"></div>
            </div>
            <%if (pages.size() == 0) {%>
            <p>No text blocks available. You have to generate them launching the <a href="javascript:void()" class="generatemissingbutton" title="Extract text from site">parsing procedure</a>.</p> 
            <%} else {%>
            <table class="containertable"style="width:100%"  cellpadding="0" cellspacing="0" border="0">
                <tr class ="rowheader">
                    <th class="checkboxes"><input type="checkbox" class="multipageselector"/></th>
                    <th>Page</th>
                    <th>Blocks of text</th>
                    <th>Words</th>
                    <th>Translated words</th>
                    <th>Progress</th>
                </tr>
                <%for (int i = 0; i < pages.size(); i++) {
                        Page pg = pages.get(i);
                        Page.Statistics st = pg.getStatistics(targetLocale);
                %>
                <tbody class ="page ignorable <%=pg.isIgnored() ? "ignorepage" : "notignorepage"%>" pageid="<%=pg.getId()%>">
                    <tr>
                        <td class="checkboxes">
                            <div class="stringhandle"></div>
                            <a href="javascript:void()" class="includepage whenignore" title="Include in translation"><img class="rowcommand clickable" src="img/notranslate.png"></a>
                            <a href="javascript:void()" class="deletepage whennotignore" title="Exclude from translation"><img class="rowcommand clickable" src="img/translate.png"></a>
                            <input type="checkbox" class="pageselector"/>
                        </td>
                        <td class="pagename"><a target="sitepage" href="<%=site.getHost() + "/" + pg.getPage()%>"><%=site.getHost() + "/" + pg.getPage()%>
                            </a></td>
                        <td class="stringcount"><%=st.getStringCount()%></td> 
                        <td class="wordcount"><%=st.getWordCount()%></td> 
                        <td class="translatedwordcount"><%=st.getTranslatedWordCount()%></td> 
                        <td class="progress <%=st.complete() ? "complete" : "incomplete"%>"><%=st.percentage()%>%</td> 
                    </tr>
                    <tr>
                        <td colspan="6" class="stringscontainer" pageindex="<%=i%>">
                            <img src="img/wait.gif" style="margin-left: 30px;">
                        </td>
                    </tr>
                </tbody>
                <%}%>
            </table>
            <%}%>

        </div>
        <div id="xliffexportform" title="Export in XLIFF format" class="messagedialog">
            <form>
                <fieldset class="centered">
                    <input type="checkbox" name="alsohtml" id="alsohtml" />Include HTML tags
                </fieldset>
            </form>
        </div>
        <div id="xliffimportform" title="Import XLIFF translations" class="messagedialog">
            <form target="xliffoutput" enctype="multipart/form-data" id="xliffimportform" method="post" name="xliffimportform" action="/servlet/siteParser?cmd=importxliff">
                <fieldset class="centered" >
                    <legend>File to import (*.zip or *.xlf)</legend>
                    <input type="file" name="filetoupload" id="filetoupload" name="filetoupload" />
                </fieldset>
            </form>
            <iframe style="display: none" name="xliffoutput"></iframe>
        </div>
        <jsp:include page="WEB-INF/jsp/footer.jsp" />

    </body>
    <%} finally {
            dh.dispose();
        }%>
</html>