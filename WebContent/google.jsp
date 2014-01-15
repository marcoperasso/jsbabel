<%-- 
    Document   : google.jsp
    Created on : Jan 11, 2013, 10:40:26 AM
    Author     : perasso
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="google-translate-customization" content="f42727da96ea407e-9b8723ec429214bf-g4bbeb3b4143043df-f"/>
        <title>Google automatic translation</title>
    </head>
    <body>
        <% 
        String from = request.getParameter("from");
        String to = request.getParameter("to");
        %>
        <div id="google_translate_element"></div><script type="text/javascript">
            function setCookie(c_name, value, exdays) {
                var exdate = new Date();
                exdate.setDate(exdate.getDate() + exdays);
                var c_value = escape(value) + ((exdays == null) ? "" : "; expires=" + exdate.toUTCString());
                document.cookie = c_name + "=" + c_value;
            }
            setCookie("googtrans", "/<%=from%>/<%=to%>", 1);
            function googleTranslateElementInit() {
                new google.translate.TranslateElement(
                { pageLanguage: "<%=from%>",
                    includedLanguages: "<%=to%>",
                    layout: google.translate.TranslateElement.InlineLayout.SIMPLE
                }, 
                'google_translate_element');
            }
        </script>
        <script type="text/javascript" src="//translate.google.com/translate_a/element.js?cb=googleTranslateElementInit"></script>

        <h1>Hello World!</h1>
    </body>
</html>
