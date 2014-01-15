<%-- 
    Document   : registrationsubmitted
    Created on : Dec 10, 2012, 4:08:36 PM
    Author     : perasso
--%>

<%@page import="jsbabel.entities.Site"%>
<%@page import="jsbabel.*"%>
<%@page import="jsbabel.entities.*"%>
<%@page import="java.util.*"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <jsp:include page="WEB-INF/jspf/commonhead.jspf"></jsp:include>
        <script type="text/javascript" src="js/jquery.selectlist.js"></script>


        <title>My sites</title>
        <style type="text/css">
            .innerfields
            {
                width: 300px;
            }
            td
            {
                vertical-align: top;
            }
            select
            {
                width: 100%
            }
            .outerfields{
                margin:0px 20px 10px 20px;
            }
            .outerfields>legend{
                font-size: larger;
                font-style: oblique;
            }
        </style>
    </head>
    <%if (!Helper.isUserLogged(session)) {
            Helper.redirectForLogin(request, response);
            return;
        }

        User user = Helper.getUser(session);

        Locale[] sortedLocales = Helper.getSortedLocales();
    %>
    <body>
        <jsp:include page="WEB-INF/jsp/header.jsp" /> 
        <div class="content">
            <h1>My sites</h1>
            <%if (user.getSites().size() == 0) {%>
            <p>You currently have no sites to translate.</p>
            <%} else {%> 
            <p>You are translating these sites:</p>
            <%}%>
            <div>

                <%
                    Site[] sites = new Site[user.getSites().size()];
                    user.getSites().toArray(sites);
                    Arrays.sort(sites);
                    for (Site site : sites) {
                        char anchor = site.getAnchor();
                %>
                <form method="post" action="servlet/settings">
                    <input type="hidden" name="siteid" class="siteid" value="<%=site.getId()%>">
                    <input type="hidden" name="sitedomain" class="sitedomain" value="<%=site.getHost()%>">
                    <fieldset class="outerfields">
                        <legend>
                            <img src="img/delete.png" class="deletesite clickable" alt="Remove this site" title="Remove this site"/>
                            <span class="jsb_notranslate"><%=site.getHost()%></span>
                        </legend>
                        <table>
                            <tr>
                                <td><fieldset class="innerfields">
                                        <Legend>Original site language</Legend>

                                        <select id="baseLanguage" name="baseLanguage" class="siteproperty jsb_notranslate">
                                            <%
                                                String bl = site.getBaseLanguage();
                                                if (Helper.isNullOrEmpty(bl)) {
                                                    bl = Helper.getLanguageCode(request.getLocale());
                                                }
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
                                    </fieldset>
                                    <fieldset class="innerfields">
                                        <legend>Anchor translation toolbar to:</legend>
                                        <input type="radio" name="anchor" value="L" class="siteproperty"
                                               <%=anchor == 'L' ? "checked" : ""%>>left margin of the page<BR />
                                        <input type="radio" name="anchor" value="C" class="siteproperty"
                                               <%=anchor == 'C' ? "checked" : ""%>>center of the page<BR />
                                    </fieldset>
                                </td>
                                <td>
                                    <fieldset class="innerfields">
                                        <legend>Translated languages</legend>
                                        <select multiple id="targetLanguage" name="targetLanguage"
                                                class="targetLanguage siteproperty jsb_notranslate" title="Select a language">
                                            <%
                                                for (Locale l : sortedLocales) {
                                            %>
                                            <option 
                                                <%=site.containsTargetLanguage(Helper.getLanguageCode(l)) ? "selected=\"selected\"" : ""%>
                                                value="<%=Helper.getLanguageCode(l)%>">
                                                <%=l.getDisplayName(l)%>
                                            </option>
                                            <%
                                                }
                                            %>
                                        </select>
                                    </fieldset>
                                </td>
                                <td>
                                    <fieldset class="innerfields">
                                        <legend>Manage site translations</legend>
                                        <%
                                            for (TargetLocale tl : site.getTargetLocales()) {
                                                Locale l = Helper.fromLanguageCode(tl.getLocale());
                                        %>
                                        <a title="<%=l.getDisplayName(l)%>" href="sitetranslations.jsp?siteid=<%=site.getId()%>&targetlocale=<%=tl.getLocale()%>" ><%=l.getDisplayName(l)%></a>
                                        <%
                                            }
                                        %>
                                    </fieldset>
                                </td>
                            </tr>
                        </table>
                        <div class="centercontent">
                            <a title="Translate" href="javascript:void(0)" class="translatesite">Translate this site</a>

                        </div>
                    </fieldset>
                </form> 
                <%
                    }
                %>

                <form id="addsiteform">
                    <fieldset class="outerfields">
                        <legend>Add a new site</legend>
                        <label for="addsite">Site url: </label>
                        <input id="sitename" style="width: 300px;" name="sitename" type="text">
                        <input type="submit" id="addbtn" value="Add">
                    </fieldset>
                </form>

            </div>
        </div>
        <jsp:include page="WEB-INF/jsp/footer.jsp" />

        <script type="text/javascript">
            this.somethingChanged = function(){
                elementChanged(this);
            }
        this.elementChanged = function(el)
        {
            var data = $(el).parents("form").serialize();
            $.post("/servlet/mysites?cmd=sitechange", data, function(res){
                if (!res.success)
                    alert(res.message);
            });
        }
        $(document).ready(function() { 
                
            $('select.targetLanguage').selectList({
                addAnimate : function(item, callback) {
                    $(item).slideDown(500, callback);
                },
                removeAnimate : function(item, callback) {
                    $(item).slideUp(500, callback);
                },
                onRemove: elementChanged,
                template : '<li title="Remove"><img src="/servlet/flag?loc=%value%"/>%text%</li>'
            });
                
            $('#addbtn').click(function(e){
                e.preventDefault();
                beginWaitingMessage();
                var formData = $(this).parents("form").serialize();
                $.getJSON("/servlet/mysites", {"cmd":"checksite", "shouldnotexist":true, "sitename":$("#sitename").val() }, function(data){
                    endWaitingMessage();
                    if (data.success == true)
                    {
                        $.post("/servlet/mysites?cmd=add", formData, function(res){
                            if (res.success)
                                location.reload();
                            else
                                alert(res.message);
                        });
                    }
                    else
                        alert(data.message);
                });
            
            });
                
            $('.deletesite').click(function(){
                if (confirm(_jsb("Are you sure to remove this site?")))
                {
                    beginWaitingMessage();
                    var f = $(this).parents("form");
                    var siteIdField = $(".siteid", f);
                    $.getJSON("/servlet/mysites", {"cmd":"deletesite", "siteid":siteIdField.val() }, function(data){
                        endWaitingMessage();
                        if (data.success == true)
                            f.remove();
                        else
                            alert(data.message);
                    });
                }
            });
            $('.translatesite').click(function(){
                var f = $(this).parents("form");
                var siteIdField = $(".siteid", f);
                var siteDomainField = $(".sitedomain", f);
                open(siteDomainField.val(), siteIdField.val());
                        
            });
                
            $(".siteproperty").change(somethingChanged);
                            
            var sample = "http://www.your site.com";
            var sitename = $("#sitename");
            sitename.focus(function() {
                if (sitename.val() == sample)
                    sitename.val("http://");
            });
            sitename.focusout(function() {
                if (sitename.val() == "http://")
                    sitename.val(sample);
            });
            sitename.val(sample);
        });
        </script>
    </body>
</html>
