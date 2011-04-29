<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<h1>Birds Australia Citizen Science Registration</h1>

<p class="pageContent">
	To start contributing your observations to Birds Australia Citizen Science, please 
	enter the following details:
</p>

<form:form commandName="user" action="${pageContext.request.contextPath}/cc/usersignup.htm">
    <table cellspacing="0" cellpadding="1">
        <tr>
            <td class="formlabel">Your first name:</td>
            <td><form:input path="firstName" size="40" cssErrorClass="formerrorfield" cssClass="validate(required)"/></td>
            <td><form:errors path="firstName"/></td>
        </tr>

        <tr>
            <td class="formlabel">Your last name:</td>
            <td><form:input path="lastName" size="40" cssErrorClass="formerrorfield" cssClass="validate(required)"/></td>
            <td><form:errors path="lastName"/></td>
        </tr>

        <tr>
            <td class="formlabel">Your e-mail address:</td>
            <td><form:input path="emailAddress" size="40" cssErrorClass="formerrorfield" cssClass="validate(email)"/></td>
            <td><form:errors path="emailAddress" htmlEscape="false"/></td>
        </tr>

         <tr>
            <td class="formlabel">Your password:</td>
            <td><form:password path="password" size="40" cssErrorClass="formerrorfield" cssClass="validate(required)"/></td>
            <td><form:errors path="password"/></td>
        </tr>

        <tr>
            <td class="formlabel">Your password again:</td>
            <td><form:password path="confirmPassword" size="40" cssErrorClass="formerrorfield" cssClass="validate(required)"/></td>
            <td><form:errors path="confirmPassword"/></td>
        </tr>
        
        <tr>
            <td class="formlabel">Your age:</td>
            <td>
            	<form:select path="age" cssErrorClass="formerrorfield" cssClass="validate(required)">
            		<form:option value="Under 12">Under 12</form:option>
            		<form:option value="12 - 18">12 - 18</form:option>
            		<form:option value="19 - 24">19 - 24</form:option>
            		<form:option value="25 - 35">25 - 35</form:option>
            		<form:option value="Above 36">Above 36</form:option>
            	</form:select>
            </td>
            <td><form:errors path="age"/></td>
        </tr>
        
        <tr>
            <td class="formlabel">Telephone:</td>
            <td><form:input path="telephone" size="40" cssErrorClass="formerrorfield" cssClass="validate(required)"/></td>
            <td><form:errors path="telephone"/></td>
        </tr>
                
        <tr>
            <td class="formlabel">How did you hear about us?</td>
            <td>
            	<form:select path="hearabout" cssErrorClass="" cssClass="validate(required)">
            		<form:option value="UWA">UWA</form:option>
            		<form:option value="Media">Media</form:option>
            		<form:option value="Birds Australia">Birds Australia</form:option>
            		<form:option value="Other">Other</form:option>
            	</form:select>
            </td>
            <td><form:errors path="hearabout"/></td>
        </tr>
        
        <tr>
            <td class="formlabel">Consent Form:</td>
            <td>
            	<textarea name="textfield" cols="60" rows="8" id="textfield" disabled="disabled">
I state that I consent to participate in this citizen science study conducted by students at the University of Western Australia. I understand that the information I contribute to this study will be used for research purposes, and that my personal information will not be made available to third parties. I further understand that I am free to withdraw at any time without any penalty
            	</textarea> 
            </td>
            <td></td>
        </tr>

        <tr>
            <td class="formlabel">Yes, I agree to the consent form</td>
            <td><form:checkbox path="" size="40" cssErrorClass="" value="false" cssClass="validate(required)"/></td>
            <td><form:errors path=""/></td>
        </tr>
        
        <tr>
            <td></td>
            <td>
                <input name="submit" type="submit" value="Register" class="form_action">
            </td>
        </tr>
    </table>
</form:form>
