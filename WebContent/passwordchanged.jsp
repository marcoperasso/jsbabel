
<%@ page import="jsbabel.*"%>
<%@ page import="jsbabel.entities.*"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <%
        if (!Helper.isUserLogged(session)) {
            Helper.redirectForLogin(request, response);
            return;
        }
        User user = Helper.getUser(session);
    %>
    <head>
        <jsp:include page="WEB-INF/jspf/commonhead.jspf"></jsp:include>
            <title>Password changed</title>
        </head>
        <body>
        <jsp:include page="WEB-INF/jsp/header.jsp" /> 
        <div class="content">
            <h1>Password changed</h1>
            <div class="standalonemessage">
                <p>Password for user <%=user.getName() + ' ' + user.getSurname()%> has been successfully changed.</p>
            </div>
        </div>
        <jsp:include page="WEB-INF/jsp/footer.jsp" />

    </body>
</html>
