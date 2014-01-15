<%-- 
    Document   : registrationsubmitted
    Created on : Dec 10, 2012, 4:08:36 PM
    Author     : perasso
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="jsbabel.*"%>
<%@ page import="jsbabel.entities.*"%>
<%@ page import="java.util.*"%>
<!DOCTYPE html>
<html>
    <head>
        <jsp:include page="WEB-INF/jspf/commonhead.jspf"></jsp:include>
            <title></title>
        </head>
        <body>
        <jsp:include page="WEB-INF/jsp/header.jsp" /> 
        <div class="content">
            <h1>Translated sites</h1>
            <%DataHelper dh = new DataHelper();
                try {
                    for (Site s : dh.getSites()) {
                        for (TargetLocale targetLocale : s.getTargetLocales()) {
                            String targetUrl = SiteMapGenerator.getDynamicUrl(targetLocale.getLocale(), s.getHost(), false);
            %>
            <p class="centercontent"><a href="<%=targetUrl%>"><%=s.getHost()%></a> </p>
            <%                    }
                    }
                } finally {
                    dh.dispose();
                }
            %> 
        </div>
        <jsp:include page="WEB-INF/jsp/footer.jsp" />

    </body>
</html>
