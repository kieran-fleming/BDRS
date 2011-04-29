<h1>Birds Australia Citizen Science Tracker Sign in</h1>
<p class="pageContent">
Welcome to the Birds Australia Citizen Science program.
</p>
<p class="pageContent">
    If you have not already registered, you can do so <a href="${pageContext.request.contextPath}/cc/usersignup.htm">here</a>.
</p>
<p class="pageContent">
    If you already have a Username and Password, please sign in below.
</p>
<form method="post" action="j_spring_security_check" class="input_container">
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
