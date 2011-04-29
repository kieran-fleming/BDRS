<h1>Biological Data Recording System Sign in</h1>
<p class="pageContent">
Welcome to the Biological Data Recording System.
</p>
<p class="pageContent">
    If you have not already registered, you can do so 
    <a href="${pageContext.request.contextPath}/vanilla/usersignup.htm">here</a>.
</p>
<p class="pageContent">
    If you already have a Username and Password, please sign in below.
</p>
<div id="browser" class="message" style="display:none;">
    <span class="boldtext">
        Your current browser is not supported on this website.
        Please upgrade to a modern browser before proceeding.
    </span>
    
    <p>
        <a href="http://www.google.com/chrome/">Google Chrome</a>&nbsp;|&nbsp;
        <a href="http://www.mozilla.com/">Mozilla Firefox</a>&nbsp;|&nbsp;
        <a href="http://www.apple.com/safari/">Apple Safari</a>&nbsp;|&nbsp;
        <a href="http://www.opera.com/">Opera</a>
    </p>
</div>

<form method="post" action="j_spring_security_check" class="input_container" id="signin">
    <table cellspacing="0" cellpadding="1">
        <tr>
            <td>Please enter your username&nbsp;</td>
            <td><input name="j_username" type="text"/> </td>
        </tr>
        <tr>
            <td>Please enter your password&nbsp;</td>
            <td>
                <input name="j_password" type="password"/>
            </td>
            <td>
                <input id="j_submit" class="button form_action" type="submit" value="Sign In"/>
            </td>
        </tr>
        <tr>
            <td/>
            <td>
                <a href="${pageContext.request.contextPath}/reminder.htm">Forgot your password?</a>
            </td>
        </tr>
    </table>
</form>

<script type="text/javascript">
    var browser = jQuery.browser;
    if(browser.msie && browser.version === "6.0") {
        jQuery("#browser").show();
    }
</script>
