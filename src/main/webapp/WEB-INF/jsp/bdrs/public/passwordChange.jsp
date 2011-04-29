<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %> 
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<p class="title">Change Your Password</p>

<p>Please enter your new password twice.</p>

<form:form commandName="password" method="POST">  
    <table cellspacing="0" cellpadding="1">
        <tr>
            <tiles:insertDefinition name="wrapFormInput">
                <tiles:putAttribute name="path">password</tiles:putAttribute>
                <tiles:putAttribute name="label">Password</tiles:putAttribute>
                <tiles:putAttribute name="type">password</tiles:putAttribute>
            </tiles:insertDefinition>
        </tr>
        
        <tr>
            <tiles:insertDefinition name="wrapFormInput">
                <tiles:putAttribute name="path">confirmPassword</tiles:putAttribute>
                <tiles:putAttribute name="label">Confirm Password</tiles:putAttribute>
                <tiles:putAttribute name="type">password</tiles:putAttribute>
            </tiles:insertDefinition>
        </tr>
        
        <tr>
            <td/>
            <td><input type="submit" value="Change Password" class="button"/></td>
        </tr>
    </table>
</form:form>
