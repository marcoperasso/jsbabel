<%@page import="jsbabel.Helper"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="jsbabel.*"%>
<%@page import="jsbabel.entities.*"%>
<%@page import="java.util.*"%>
<%@page import=" java.lang.reflect.*"%>
<!DOCTYPE html>
<html>
    <%
        boolean newUser = "true".equals(request.getParameter("insert"));
        User user = Helper.getUser(session);
        if (!newUser && user == null) {
            Helper.redirectForLogin(request, response);
            return;
        }

    %>
    <head>
        <jsp:include page="WEB-INF/jspf/commonhead.jspf"></jsp:include>

        <link href="css/register.css" type="text/css" rel="stylesheet"/>
        <title><%=newUser ? "User registration form" : "User data"%></title>

        <script type="text/javascript" >
                
            function reloadCaptcha() {
                $("#captcha").each(function(){
                    var src = this.src;
                    var pos = src.indexOf('?');
                    if (pos >= 0) {
                        src = src.substr(0, pos);
                    }
                    var date = new Date();
                    this.src = src + '?v=' + date.getTime();})
            }
            function getInvalidFields()
            {
                var invalids = $(".required").filter(function() {
                    return this.value == "";
                });
                    
                //controllo approvazione condizioni
                var chk = $('input[type="checkbox"].required');
                if (chk.attr("checked") != "checked") {
                    invalids = invalids.add(chk);
                }
    
                //controllo email
                var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
                if (!re.test($('#mail').val())) {
                    invalids = invalids.add($('#mail'));
                }
                return invalids;
            }

            $(function() {
                $( "#birthDate" ).datepicker({"autoSize":true, dateFormat: "dd/mm/yy"});
                $('#submit').click(function(e) {
                    var invalids = getInvalidFields();
                    if (invalids.length > 0) {
                        $(invalids[0]).focus();
                        invalids.addClass('invalid');
                        $('.errorMessage').addClass('visible');
                    }
                    else
                    {
                        beginWaitingMessage();
                        $.post("servlet/registerUser", $("#userform").serialize(), function(data){
                            endWaitingMessage();
                            if (!data.success)
                            {
                                alert(data.message);
                                reloadCaptcha();
                                $("#captchaanswer").val("");
                            }
                            else
                            {
            <%if (newUser) {%>
                                    location.href = "/registrationsubmitted.jsp";
            <%} else {%>
                                    alert(_jsb("Your data has been succesfully saved"));
            <%}%>
                                }
            
                            }, "json");
                        }
                    });
                
                    $('input').change(function() {
                        $(this).removeClass('invalid')
                        $('.errorMessage').removeClass('visible');
                    });
                
            <%if (user != null) {
                    for (Field field : User.class.getDeclaredFields()) {
                        HiddenField ann = field.getAnnotation(HiddenField.class);
                        if (ann != null) {
                            continue;
                        }
                        field.setAccessible(true);
                        Object propObj = field.get(user);
                        if (propObj != null) {
                            String sStringVal = field.getType() == Date.class
                                    ? Helper.formatDate((Date) propObj)
                                    : propObj.toString();
            %>
                    $("#<%=field.getName()%>").val("<%=sStringVal%>");
            <%
                        }
                    }
                }%>
                    });
                
        </script>
    </head>
    <body>
        <jsp:include page="WEB-INF/jsp/header.jsp" />
        <div class="content">
            <h1><%=newUser ? "User registration form" : "User data"%></h1>
            <form id="userform" method="post" action="servlet/registerUser<%=newUser ? "newuser=true" : ""%>" target="">
                <div class="centered fieldContainer">
                    <%if (newUser) {%>
                    <h4 style="background: orange;color:blue;">WARNING! This is an alpha version, registration is available 
                        only for trial purposes; if required by technical needs, your data may be cancelled without notice.</h4>
                    <input name="alpha" id="alpha" type="checkbox" class="required" value="alpha"/>
                    <span class="formlabel">I know this is an alpha version and therefore not deeply tested, 
                        I'm aware that some important features may be missing.</span>
                    <br><br>
                    <%}%>
                    <span class="formlabel">Required fields are signed with *.</span>
                    <span class="errorMessage">The form contains some errors.</span>
                    <fieldset id="nameData">
                        <legend>
                            Personal data
                        </legend>
                        <label for="name">First name *</label>
                        <input class="required"
                               type="text" id="name" name="name"/>
                        <label for="surname">Last name *</label>
                        <input class="required" type="text" id="surname"
                               name="surname"/>
                        <label for="mail">Email (will be your user id) *</label>
                        <input type="text" id="mail" name="mail" class="required" <%=newUser ? "" : "readonly=\"readonly\""%>/>
                        <label for="gender">Gender *</label> 
                        <select class="required" id="gender" name="gender">
                            <option value="Unspecified">Unspecified</option>
                            <option value="Female">Female</option>
                            <option value="Male">Male</option>
                        </select>
                        <label for="birthDate">Birth date *</label>
                        <input class="required" type="text" id="birthDate"
                               name="birthDate"/>
                        <label for="company">Company</label>
                        <input
                            type="text" id="company" name="company"/>
                        <label for="taxCode">Tax Code</label>
                        <input
                            type="text" id="taxCode" name="taxCode"/>
                    </fieldset>
                    <fieldset id="addressData">
                        <legend>
                            Address data
                        </legend>
                        <label for="address">Street *</label>
                        <input class="required"
                               type="text" id="address" name="address"/>
                        <label for="city">City *</label>
                        <input class="required" type="text" id="city" name="city"/>
                        <label
                            for="country">Country *</label>
                        <input class="required"
                               type="text" id="country" name="country"/>
                        <label for="zip">Zip Code *</label>
                        <input class="required" type="text" id="zip" name="zip"/>

                    </fieldset>
                    <%if (newUser) {%>
                    <fieldset id="legalsData">
                        <legend>
                            Legals
                        </legend>
                        <img src="img/pdf.gif" alt="pdf" style="vertical-align: top;"
                             width="15" height="16" /><a href="terms and conditions.pdf" target="_blank"> Terms and Conditions in PDF format.</a>
                        <br />
                        <input name="agree" id="agree" type="checkbox" class="required" value="agree"/>
                        I have downloaded, printed and read the Terms and Conditions and I agree with them *
                    </fieldset>
                    <fieldset>
                        <legend>Security</legend>
                        <div class="centercontent"><img id="captcha" src="servlet/captcha"/></div>
                        <label for="captcha">Please insert the above verification code*: </label>
                        <input type="text" name="captchaanswer" id="captchaanswer"class="required"/>
                    </fieldset>
                    <%}%>
                    <input id="verify" name="verify" type="text" style="display: none"/>
                    <div class="centercontent">
                        <input type="button" id="submit" name="submit" value="Submit"/>
                    </div>
                </div>
            </form>
        </div>
        <jsp:include page="WEB-INF/jsp/footer.jsp" />

    </body>
</html>