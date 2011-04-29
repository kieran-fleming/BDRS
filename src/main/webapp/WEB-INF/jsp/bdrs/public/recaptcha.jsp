<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<form method="POST">
    <h1>Please enter the two words below and press Submit.</h1>
    <div id="cwrecaptcha" style="margin-top: 25px; margin-left: 100px;">
        <script type="text/javascript">
            var RecaptchaOptions = { theme : 'white' };
        </script>
        <cw:recaptcha/>
	    <input type="submit" class="form_action" value="Submit"/>
    </div>
</form>
