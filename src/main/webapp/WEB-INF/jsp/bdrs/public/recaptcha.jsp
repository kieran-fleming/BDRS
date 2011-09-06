<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<form method="POST">
    <h1>Please enter the two words below and press Submit.</h1>
	<p>
        By doing this step, you help ensure the security of this site... and help
        <a href="http://www.google.com/recaptcha/learnmore">digitise books</a> 
		at the same time.
    </p>
    <div id="cwrecaptcha" style="margin-top: 25px; margin-left: 100px;">
        <script type="text/javascript">
            var RecaptchaOptions = { theme : 'white' };
        </script>
        <cw:recaptcha/>
	    <input type="submit" class="form_action" value="Submit"/>
    </div>
</form>
