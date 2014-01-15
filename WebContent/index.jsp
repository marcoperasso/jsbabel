<?xml version="1.0" encoding="ISO-8859-1" ?>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="jsbabel.*"%>
<%@ page import="jsbabel.entities.*"%>
<%@ page import="java.util.*"%>

<!DOCTYPE html>
<html>
    <head>
        <jsp:include page="WEB-INF/jspf/commonhead.jspf"></jsp:include>
        <title>JSBABEL - Make your site multilingual (Alpha)</title>
        <meta name="Description" content="Site translation tool; include JSBABEL script in your HTML pages and you'll be able to dynamically translate them directly from the browser"/>
        <meta http-equiv="keywords" content="localization tool, translation tool">
        <link href="css/index.css" type="text/css" rel="stylesheet"/>
        <link rel="shortcut icon" href="favicon.ico" />
        <script type="text/javascript">
            $(function() {
                var src = $("#src");
                var output = $("#output");
                $(window).resize(function() {
                    output.height($(window).height());
                });
		
                var frameWindow = output[0].contentWindow;;
                var targetLocale = null;
                var safeCount = 0;
                function setTargetLocale() {
                    if (safeCount++ > 10)//per evitare loop infiniti
                        return;
			
                    if (!frameWindow.__babel)//se non fosse stato caricato ancora, riprovo
                        setTimeout(setTargetLocale, 10);
                    else {
                        frameWindow.__babel.setTargetLocale(targetLocale);
                        targetLocale = null;
                    }
                }
                output.load(function() {
                    if (targetLocale) 
                        setTargetLocale();
                });
                function downloadXLiff()
                {
                    if (src.val().indexOf("http://") == 0){
                        beginWaitingMessage();
                        $.getJSON("/servlet/mysites", {"cmd":"checksite", "sitename":src.val() }, function(data){
                            endWaitingMessage();
                            if (data.success == true)
                            {
                                download("/servlet/xliffdownloader?url=" + src.val() + "&baseLanguage=" + $('#baseLanguage').val());
                            }
                            else
                                alert(data.message);
                        });
                    }
                }
                function doDemo() {
                    if (src.val().indexOf("http://") == 0) {
                        beginWaitingMessage();
                        $.getJSON("/servlet/mysites", {"cmd":"checksite", "sitename":src.val() }, function(data){
                            endWaitingMessage();
                            if (data.success == true)
                            {
                                safeCount = 0;
                                targetLocale = $('#targetLanguage').val();
                                output.attr("src", "servlet/htmlInjector?baseLanguage="
                                    + $('#baseLanguage').val() + "&targetLanguage="
                                    + targetLocale + "&src="
                                    + src.val());//src per ultimo altrimenti mi vengono concatenati gli altri parametri nell'URL
                                $(".wrapper").fadeOut('slow');
                                output.height($(window).height());
                                output.fadeIn('slow');
                                $(".jsb_translator").hide();
                                $("#backcontainer").slideDown('slow');
                            }
                            else
                                alert(data.message);
                        });
                    }
                }
                $("#go").click(doDemo);
                $("#extract").click(downloadXLiff);
                src.keydown(function(e){
                    if (e.keyCode==13)
                    {
                        e.preventDefault();
                        e.stopPropagation();
                        doDemo();
                    }
                });
                $("#back").click(function() {
                    $("#backcontainer").slideUp('slow');
                    $(".wrapper").fadeIn('slow');
                    $(".jsb_translator").show();
                    output.fadeOut('slow');
                });
                var sample = "http://www.your site.com";
                src.focus(function() {
                    if (src.val() == sample)
                        src.val("http://");
                });
                src.focusout(function() {
                    if (src.val() == "http://")
                        src.val(sample);
                });
                src.val(sample);
            });
        </script>
        <script type="text/javascript" src="/js/procedurecontroller.js"></script>
   
    </head>
    <body>
        <div class="wrapper">
            <jsp:include page="WEB-INF/jsp/header.jsp" />

            <div class="content">
                <h1>Make your site multilingual</h1>

                <p>JSBABEL is an easy way for translating your site without modifying its structure; simply insert this javascript in your pages:<p>
                <div style="padding:10px 40px 10px 40px;text-align: center" class="jsb_notranslate">
                    <code style="font-size: 14px">
                        &lt;script type="text/javascript" src="http://www.jsbabel.com/js/babel.js"&gt;&lt;/script&gt;
                    </code>
                </div>
                <p>and you'll be able to translate your site directly from the browser.</p>
                <fieldset class="centered outer">
                    <legend>Try a demo!</legend>
                    <table class="centered index" cellpadding="0" cellspacing="0" border="0">
                        <tr>
                            <td><fieldset>
                                    <legend>Original Site language</legend>
                                    <select id="baseLanguage" name="baseLanguage" class="jsb_notranslate">
                                        <%
                                            Locale[] sortedLocales = Helper.getSortedLocales();
                                            String tl = Helper.getLanguageCode(request.getLocale());
                                            String bl = "en-US".equals(tl) ? "it-IT" : "en-US";
                                            for (Locale l : sortedLocales) {
                                        %>
                                        <option 
                                            <%=Helper.getLanguageCode(l).equals(bl) ? "selected=\"selected\"" : ""%>
                                            value="<%=Helper.getLanguageCode(l)%>">
                                            <%=l.getDisplayName(l)%>
                                        </option>
                                        <%
                                            }
                                        %>
                                    </select>
                                </fieldset></td>
                            <td><img src="img/arrow-right.png" alt="Arrow"/></td>
                            <td><fieldset>
                                    <legend>Translated Language</legend>
                                    <select id="targetLanguage" name="targetLanguage"
                                            class="baseLanguage jsb_notranslate" Title="Select a language">
                                        <%
                                            for (Locale l : sortedLocales) {
                                        %>
                                        <option
                                            <%=Helper.getLanguageCode(l).equals(tl) ? "selected=\"selected\"" : ""%>
                                            value="<%=Helper.getLanguageCode(l)%>">
                                            <%=l.getDisplayName(l)%>
                                        </option>
                                        <%
                                            }
                                        %>
                                    </select>
                                </fieldset></td>
                        </tr>
                    </table>
                    <table  class="centered index" cellpadding="0" cellspacing="0" border="0">
                        <tr>
                            <td class="label"><label for="src">Your web site:</label></td>
                            <td class="input"><input type="text" id="src" class="text" name="src"></input></td>
                            <td class="button"><a id="go" class ="actionbutton" title="Dynamically translate your site!">Translate</a></td>
                            <td class="button"><a id="extract" class ="actionbutton" title="Extract text segments in XLIFF format">Extract text</a></td>
                        </tr>
                        <tr>
                            <td colspan=3>Don't worry: you are not actually modifying your
                                web site, this is just a demo overlay that only you will see.</td>
                        </tr>
                    </table>
                </fieldset>

                <a href="mirror.jsp" style="visibility: hidden;"></a>
            </div>
            <jsp:include page="WEB-INF/jsp/footer.jsp" />
        </div>
        <div id="backcontainer">
            <a id="back" title="Back to JSBABEL">Go back to JSBABEL</a>
        </div>
        <iframe id="output"> </iframe>
    </body>
</html>