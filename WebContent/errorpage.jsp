<%-- 
    Document   : errorpage
    Created on : Dec 10, 2012, 4:05:22 PM
    Author     : perasso
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="jsbabel.*"%>
<%@ page import="java.util.*"%>
<%@ page isErrorPage="true"%>
<!DOCTYPE html>
<html>
    <head>
        <jsp:include page="WEB-INF/jspf/commonhead.jspf"></jsp:include>
        <title>Error</title>

        <script type="text/javascript">
            $(function() {

                $("#showdetails").click(function() {
                    $('#details').fadeIn('slow', function() {
                        $("#showdetails").hide();
                        $("#hidedetails").show();
                    });
                });

                $("#hidedetails").click(function() {
                    $('#details').fadeOut('slow', function() {
                        $("#showdetails").show();
                        $("#hidedetails").hide();
                    });
                });
            })
        </script>
    </head>
    <body>

        <jsp:include page="WEB-INF/jsp/header.jsp" /> 
        <div class="content">
            <h1>Error</h1>
            <div style="padding:20px;">
                <h3>The following error has occurred:</h3>
                <%Throwable ex = exception;
                    while (ex != null) {%>
                <p><%=ex.getLocalizedMessage()%></p>
                <% ex = ex.getCause();
                    }%>
                <p><a id="showdetails" href="javascript:void(0)">Details...</a>
                    <a id="hidedetails" href="javascript:void(0)" style="display: none;">Hide details...</a></p>
                <div id="details" style="font-size: smaller;display: none;">
                    <%for (StackTraceElement ste : exception.getStackTrace()) {
                            out.write("<p>");
                            out.write(ste.toString());
                            out.write("</p>");
                        }%>
                </div>
                <p style="margin-top: 40px;"><a href="/index.jsp" title="Go back to home page">Go back to home page</a>.</p>
            </div> 
        </div>
        <jsp:include page="WEB-INF/jsp/footer.jsp" />
    </body>
</html>
