<%-- 
    Document   : registrationsubmitted
    Created on : Dec 10, 2012, 4:08:36 PM
    Author     : perasso
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="jsbabel.*"%>
<%@ page import="jsbabel.entities.*"%>
<%@ page import="java.util.*"%>

<!DOCTYPE html>
<html>
    <head>
        <jsp:include page="WEB-INF/jspf/commonhead.jspf"></jsp:include>
            <title>Insert your password</title>
            <link href="/css/register.css" type="text/css" rel="stylesheet"/>
            <script type="text/javascript" src="js/jquery.complexify.js"></script>

            <script type="text/javascript" >
                function getInvalidFields()
                {
                    var pwdLength = 6;
                    var invalids = $(".required").filter(function() {
                        return this.value == "";
                    });
                    if ($('#password1').val().length < pwdLength){
                        invalids = invalids.add($('#password1'));
                    }
                    if ($('#password2').val().length < pwdLength){
                        invalids = invalids.add($('#password2'));
                    }
                    else  if ($('#password1').val() != $('#password2').val()) {
                        invalids = invalids.add($('#password2'));
                    }
                    return invalids;
                }

                $(function() {

                    $('#submitbtn').click(function(e) {
                        e.preventDefault();
                        var invalids = getInvalidFields();
                        if (invalids.length > 0) {
                            $(invalids[0]).focus();
                            invalids.addClass('invalid');
                            $('.errorMessage').addClass('visible');
                        }
                        else
                        {
                            $.get("servlet/crypt", null, function(data){
                                eval(data);
                                var pwd = hex_md5($('#password1').val());
                                pwd = this.crypt(pwd);
                                $('#password').val(pwd);
                                $('#password1').val("");
                                $('#password2').val("");
                                $("#completeForm").submit();
                            }, "text");
                        }
                    });
                
                    $('input').change(function() {
                        $(this).removeClass('invalid')
                        $('.errorMessage').removeClass('visible');
                    });
                
                    // Use the complexify plugin on the first password field
                    $('#password1').complexify({
                        minimumChars:6, 
                        strengthScaleFactor:0.7
                    }, 
                    function(valid, complexity){
                        $("#pwdmeter").css({"width": complexity + '%'})
                    });
                });
                
            </script>
        </head>
    <%
        User user = null;
        String code = "";
        try {
            code = (String) request.getParameter(Const.ActivationParam);
            if (Helper.isNullOrEmpty(code)) {
                throw new Exception(Messages.InvalidActivationCode);
            }

            DataHelper dh = new DataHelper();
            user = dh.getUserByActivationCode(code);
            if (user == null) {
                throw new Exception(Messages.InvalidActivationCode);
            }

        } catch (Exception ex) {
            Helper.log(ex);
            throw new ServletException(ex);
        }
    %>
    <body>

        <jsp:include page="WEB-INF/jsp/header.jsp" /> 
        <div class="content">
            <h1>Insert your password</h1>
            <form id="completeForm" method="post" action="servlet/completeRegistration?<%=Const.RedirectUrlParam + "=" + request.getParameter(Const.RedirectUrlParam)%>">
                <div class="centered fieldContainer">
                    <p>Welcome back, <%=user.getName()%> <%=user.getSurname()%>. Please insert your password. Required fields are signed with *.</p>
                    <p class="errorMessage">The form contains some errors. Please verify that your password is at least 6 characters and that the two password fields contains the same data.</p>
                    <fieldset id="loginData">
                        <legend>
                            Login data
                        </legend>
                        <label
                            for="password1">Password *</label>
                        <input class="required"
                               type="password" id="password1" name="password1"/>
                        <label
                            for="pwdmeter">Password strength:</label>
                        <div  class="pwdmetercontainer"><div id ="pwdmeter" class="pwdmeter"></div></div>
                        <label
                            for="password2">Repeat password *</label>
                        <input class="required"
                               type="password" id="password2" name="password2"/>
                        <input type="hidden" id="password" name="password" />
                        <input type="hidden" id="<%=Const.ActivationParam%>" name="<%=Const.ActivationParam%>" value="<%=code%>"/>
                    </fieldset>
                    <input id="verify" name="verify" type="text" style="display: none"/>
                    <div class="centercontent">
                        <input type="submit" id="submitbtn" name="submitbtn"/>
                    </div>
                </div></form>
        </div>
        <jsp:include page="WEB-INF/jsp/footer.jsp" />

    </body>
</html>
