<%@ page import="jsbabel.*"%>
<%@ page import="jsbabel.entities.*"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <jsp:include page="/WEB-INF/jspf/commonhead.jspf"></jsp:include>
            <title>Password reset</title>
        </head>
        <body>
        <jsp:include page="WEB-INF/jsp/header.jsp" /> 
        <div class="content centercontent">
            <h1>Password reset</h1>
            <div class="standalonemessage">
                <p>An email with a verification code has been sent to <span style="color: blue;"><%=request.getParameter("mail")%></span>; please follow the mail instructions to introduce a new password.</p>
                <p><a href="/index.jsp" title="Go back to home page">Go back to home page</a>.</p>
            </div>
        </div>
        <jsp:include page="WEB-INF/jsp/footer.jsp" />

    </body>
</html>
