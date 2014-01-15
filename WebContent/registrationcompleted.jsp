
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
            <title>Registration successfully completed</title>
        </head>
        <body>
        <jsp:include page="WEB-INF/jsp/header.jsp" /> 
        <div class="content">
            <h1>Registration successfully completed</h1>
            <div class="standalonemessage">
                <p>Registration for user <%=user.getName() + ' ' + user.getSurname()%> has been successfully completed.</p>
                <p>Thank you for registering, you can now <a href="/mysites.jsp" title="My sites">start translating your sites!</a></p>
            </div>
        </div>
        <jsp:include page="WEB-INF/jsp/footer.jsp" />

    </body>
</html>
