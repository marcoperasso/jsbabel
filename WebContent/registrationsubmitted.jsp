<%@ page import="jsbabel.*"%>
<%@ page import="jsbabel.entities.*"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <%
        User user = Helper.getUser(session);

    %>
    <head>
        <jsp:include page="/WEB-INF/jspf/commonhead.jspf"></jsp:include>
            <title>Activate registration</title>
        </head>
        <body>
        <jsp:include page="WEB-INF/jsp/header.jsp" /> 
        <div class="content">
            <h1>Activate registration</h1>
            <div class="standalonemessage">
                <%if (user != null) {%>
                <p><%=user.getName() + ' ' + user.getSurname()%>, your registration has to be activated by choosing a password.</p>
                <p>An email with a verification code has been sent to <span style="color: blue;"><%=user.getMail()%></span>; please follow the mail instructions to complete the registration.</p>
                <p>Thank you for choosing JSBABEL. <a href="/index.jsp" title="Go back to home page">Go back to home page</a>.</p>
                <%} else {%> 
                <p>No user logged.</p> 
                <%}%>
            </div>
        </div>
        <jsp:include page="WEB-INF/jsp/footer.jsp" />

    </body>
</html>
