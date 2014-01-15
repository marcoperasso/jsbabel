<%-- 
    Document   : registrationsubmitted
    Created on : Dec 10, 2012, 4:08:36 PM
    Author     : perasso
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <jsp:include page="WEB-INF/jspf/commonhead.jspf"></jsp:include>
            <meta name="Description" content="Page not found"/>
            <title>Page not found</title>
        </head>
        <body>
        <jsp:include page="WEB-INF/jsp/header.jsp" /> 
        <div class="content">
            <h1>Ooops! Page not found</h1>
            <div style="padding:20px;">
                <h3>Sorry, the page you requested is not available.</h3>

                <p style="margin-top: 40px;"><a href="/index.jsp" title="Go back to home page">Go back to home page</a>.</p>
            </div> 
        </div>
        <jsp:include page="WEB-INF/jsp/footer.jsp" />
    </body>
</html>
