<%-- 
    Document   : registrationsubmitted
    Created on : Dec 10, 2012, 4:08:36 PM
    Author     : perasso
--%>

<%@page import="jsbabel.*"%>
<%@page import="jsbabel.entities.*"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <jsp:include page="WEB-INF/jspf/commonhead.jspf"></jsp:include>
            <title>Please login</title>
        </head>
        <body>
        <jsp:include page="WEB-INF/jsp/header.jsp" /> 
        <div class="content">
            <%if (!Helper.isUserLogged(session)) {%>
            <h1>Login needed</h1>
            <div class="standalonemessage">You are not connected. Please <a class="logincommand" href="javascript:void(0)" title="login">login</a>.</div>
            <%} else {
                    String p = request.getParameter("url");
                    if (Helper.isNullOrEmpty(p)) {
                        p = "/index.jsp";
                    }
                    response.sendRedirect(p);
                }%>
        </div>
        <jsp:include page="WEB-INF/jsp/footer.jsp" />

    </body>
</html>
