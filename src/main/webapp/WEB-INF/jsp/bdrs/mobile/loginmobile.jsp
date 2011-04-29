<div id="loginBox" class="input_container">

	<h2><span class="hideSmallDevice">Data Collection<br/></span></h2>
	<h2>Mobile Portal Sign In</h2>
	<div class="hideSmallDevice">
		<p>Welcome to the Data Collection Portal.</p>
		<p>If you have not already registered, you can do so <a href="">here</a>.</p>
		<p>If you already have a Username and Password, please sign in below.</p>
	</div>

	<div id=loginInput>
		<form method="post" action="j_spring_security_check" id="loginForm">
				<dl>
					<dt class="hideElement">
						<label for="groupname">Username:</label>
					</dt>
					<dt>
						<input id="groupname" name="j_username" type="text" placeholder="Username:"/>
					</dt>
					<dt class="hideElement">
						<label for="grouppass">Password:</label>
					</dt>
					<dt>
						<input id="grouppass" name="j_password" type="password" placeholder="Password:" />
					</dt>
					<dt>
						<input id="j_submit" class="form_action hideElement" type="submit" value="Log In" />
						<div id="submitDiv">Log In</div>
					</dt>
				</dl>
			    <p><a href="${pageContext.request.contextPath}/reminder.htm">I forgot my password</a></p>
			    <p><a href="${pageContext.request.contextPath}/trin/usersignup.htm">I want to sign up</a></p>
			</form>
	</div>
	
</div>


<script type="text/javascript">
jQuery('#submitDiv').click(function(){
	jQuery('#j_submit').click();	
});
</script>