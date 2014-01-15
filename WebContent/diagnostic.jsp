<%@ page contentType="text/html; charset=UTF-8" language="java"%>
<%@ page import="jsbabel.*"%>
<%@ page import="java.util.*"%>

<p>
    <%
            boolean message = false;
            String msg = (String) request.getParameter("msg");
            if (null != msg) {
                    message = true;
    %>
    <span class="info"><%=msg%></span>
    <%
            }
            msg = (String) request.getParameter("err");
            if (null != msg) {
                    message = true;
    %>
    <span class="error"><%=msg%></span>
    <%}%>
    <%if (message) {%>
    <span>&nbsp;-&nbsp;<%=new Date().toString() %></span>
    <%}%>
</p>