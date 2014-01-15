<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ page import="jsbabel.*"%>
<%@ page import="jsbabel.entities.*"%>
<% boolean logged = Helper.isUserLogged(session);
    User user = Helper.getUser(session);
    String loginTitle = logged ? "Logoff" : "Login";%>

<script type="text/javascript">
    
    $(function() {
        var logged = <%=logged%>;
        var marquee = null;
        window.beginWaitingMessage = function()
        {
            $("#messagetouser").dialog( "open" );
            if (!marquee)
                marquee = $('marquee').marquee();   
            else
                marquee.trigger('start')
        }
        window.endWaitingMessage = function()
        {
            marquee.trigger('stop')
            $("#messagetouser").dialog( "close" );
        }
        function toggleLogin ()
        {
            if (logged)
                logoff();
            else
                login();
        };
        function resetPassword()
        {
            if (confirm("Are you sure to reset your password?"))
            {
                beginWaitingMessage();
                var mail = $("#loginemail").val();
                $.getJSON("/servlet/commandExecutor", {"cmd":"resetpwd", "email": mail}, function(data){
                    endWaitingMessage();
                    if (data.success == true)
                    {
                        location.href = "resetpwdsubmitted.jsp?mail="+encodeURIComponent(mail);
                    }
                    else
                    {
                        alert(data.message);
                        location.reload(true);
                    }
                    
                });
            }
        }
        function login()
        {
            $( "#loginform" ).dialog( "open" );
        }
        
        function logoff()
        {
            beginWaitingMessage();
            $.getJSON("/servlet/commandExecutor", {"cmd":"logoff"}, function(data){
                endWaitingMessage();
                if (data.success == true)
                    location.reload(true);
                else
                    alert(data.message);
            });
        }
        $(".logincommand").click(toggleLogin);
        $("#loginforget").click(resetPassword);
        function doLogin()
        {
            beginWaitingMessage();
            $.get("servlet/crypt", null, function(data){
                eval(data);
                var pwd = hex_md5($('#loginpassword').val());
                pwd = this.crypt(pwd);
                $.getJSON("/servlet/commandExecutor", {
                    "cmd":"login", 
                    "email":$("#loginemail").val(), 
                    "pwd":pwd,
                    "captchaanswer":$("#captchaanswer").val()
                }, function(data){
                    endWaitingMessage();
                    if (!data.success)
                        alert(data.message);
                    location.reload(true);
                });    
            }, "text");
        }
        $( "#loginform" ).dialog({
            autoOpen: false,
            width: 400,
            modal: true,
            buttons: {
                Login: doLogin,
                Cancel: function() {
                    $( this ).dialog( "close" );
                }
            },
            close: function() {
                   
            }
        })
        .keydown(function(e){
            if (e.keyCode==13)
            {
                e.preventDefault();
                e.stopPropagation();
                doLogin();
            }
        });
        
        $( "#messagetouser" ).dialog({
            autoOpen: false,
            height: 100,
            width: 500,
            modal: false,
            title:"Please wait"
            
        });
    });
</script>
<style type="text/css">

    .logininput{
        width: 100%
    }
    .loginfieldset
    {
        width: 290px;
        text-align: left;
    }

    .ui-dialog,.ui-dialog,.ui-widget, .ui-widget-content, .ui-draggable, .ui-resizable 
    {
        background:lightgrey;
    }
    ​#messagetouser, #loginform{
        display: none;
    }
</style>
<div class="topstrip"></div>
<div class="header">
    <a href="/" title="JSBABEL home" ><img src="/img/logo.png" alt="JSBABEL" class="logo"></img></a>
    <h2 class="alpha" >Alpha version</h2>
    <%if (logged) {%>
    <div class="welcomeuser"> Welcome, <%=user.getName()%> <%=user.getSurname()%></div>
    <%}%>
    <div class="menucontainer">
        <ul>
            <li><a href="/" title="JSBABEL Home">Home</a></li>
            <li><a href="/userdata.jsp?insert=true" title="Register">Register</a></li>
            <%if (logged) {%>
            <li><a href="/mysites.jsp" title="My sites">My sites</a></li>
            <li><a href="/userdata.jsp" title="My data">My data</a></li>
            <%}%>

            <li><a class="logincommand" href="javascript:void(0)" title="<%=loginTitle%>"><%=loginTitle%></a></li>
        </ul>
    </div>

</div>
<div id="loginform" title="Login" class="messagedialog">
    <form>
        <fieldset class="centered loginfieldset">
            <label for="email">Email</label>
            <input type="text" name="email" id="loginemail" value="" class="logininput" />
            <label for="loginpassword">Password</label>
            <input type="password" name="password" id="loginpassword" value="" class="logininput" />
            <br>
            <%if (Helper.isCaptchaRequired(session)) {%>
            <div class="centercontent"><img id="captcha" src="servlet/captcha"/></div>
            <label for="captcha">Please insert the above verification code: </label>
            <input name="captchaanswer" id="captchaanswer"class="logininput"/>
            <%}%>
            <br>
            <label for="forget">Forgot your password?</label>
            <input type="button" name="forget" id="loginforget" value="Reset password" class="logininput" />

        </fieldset>
    </form>
</div>
<div id="messagetouser" class="messagedialog"> <marquee behavior="scroll" scrollamount="1" direction="left" >Submitting data to server...</marquee> </div>