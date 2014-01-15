<?xml version="1.0" encoding="utf-8" ?>
<%@ page import="java.util.*, java.sql.*"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
         pageEncoding="utf-8"%>
<%@ page import="jsbabel.*"%>
<%@ page import="jsbabel.entities.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
    <head>
        <jsp:include page="WEB-INF/jspf/commonhead.jspf"></jsp:include>
            <title>Test page</title>
        </head>
        <body>
        <jsp:include page="WEB-INF/jsp/header.jsp" /> 
        <div class="content">
            <%=Helper.isBot(request)%>
            <br/>
            <%=request.getHeader("User-Agent")%>
        </div>
        <jsp:include page="WEB-INF/jsp/footer.jsp" />
    </body>
</html>
