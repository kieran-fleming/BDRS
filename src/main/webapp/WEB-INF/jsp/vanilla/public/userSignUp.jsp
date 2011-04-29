<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<jsp:useBean id="metaList" type="java.util.List" scope="request"/>

<h1>Biological Data Recording System Registration</h1>

<p class="pageContent">
	To start contributing your observations to the Biological Data Recording System,
	please enter the following details:
</p>

<form method="POST">
    <fieldset>
	    <h2>Account Details</h2>
	    <table cellspacing="0" cellpadding="1">
	        <tr>
	            <td class="formlabel">Desired username*:</td>
	            <td><input id="username" name="username" type="text" size="40" class="validate(required)"/>
	               <span style="padding:0 0 0 0.5em"  id="unavail" class="hidden error"><img src="${pageContext.request.contextPath}/images/vanilla/icon_cross_red12x12.png"/>&nbsp;This username is unavailable</span>
                   <span style="padding:0 0 0 0.5em"  id="avail" class="hidden success"><img src="${pageContext.request.contextPath}/images/vanilla/icon_tick_green12x12.png"/>&nbsp;Username available</span>
	            </td>
	        </tr>
	        <tr>
	            <td class="formlabel">Email Address*:</td>
	            <td><input name="email" type="text" size="40" class="validate(email)"/></td>
	        </tr>
	        <tr>
	            <td class="formlabel">First Name*:</td>
	            <td><input name="firstName" type="text" size="40" class="validate(required)"/></td>
	        </tr>
	        <tr>
	            <td class="formlabel">Last Name*:</td>
	            <td><input name="lastName" type="text" size="40" class="validate(required)"/></td>
	        </tr>
	        <tr>
	            <td class="formlabel">Choose a password*:</td>
	            <td><input id="password" type="password" name="password" size="40" class="validate(required)"/></td>

	        </tr>
	        <tr>
	            <td class="formlabel">Repeat your password*:</td>
	            <td><input name="password2" type="password" size="40" class="validate(match(#password))"/></td>
	        </tr>	 
	        <!-- insert meta data fields -->
	        <tiles:insertDefinition name="userMetaDataFormFields">
                    <tiles:putAttribute name="metaList" value="${ metaList }"/>
            </tiles:insertDefinition>
	    </table>
    </fieldset>
    <div class="textcenter">
    	<input class="textcenter form_action" name="submit" type="submit" value="Register">
    </div>
</form>
<script type="text/javascript">
    jQuery(function () {
		jQuery("#username").keyup(function (object) {
	    	var params = {};
            params.q = object.target.value;
            jQuery.getJSON('${pageContext.request.contextPath}/webservice/user/checkUsername.htm', params, function(data, textStatus) {
                if(data['available']) {
                	jQuery("#avail").removeClass('hidden');
                	jQuery("#unavail").addClass('hidden');
                	jQuery("#holder").val(params.q);
                } else {
                	jQuery("#avail").addClass('hidden');
                	jQuery("#unavail").removeClass('hidden');
                	jQuery("#holder").val('');
                }
            });
		});
    });
</script>

