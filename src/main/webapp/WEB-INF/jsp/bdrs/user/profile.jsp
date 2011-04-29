<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<jsp:useBean id="metaList" type="java.util.List" scope="request"/>

<c:if test="${editAs == 'user'}">
<h1>My Profile</h1>
</c:if>
<c:if test="${editAs == 'admin'}">
<h1>Edit profile of <c:out value="${FIRST_NAME} ${LAST_NAME}" /> </h1>
</c:if>

<form method="POST">
<input value="${USER_ID}" type="hidden" />
<div class="input_container">
<h2>Change Details</h2>
<p>Please edit the account details below. Leave the password blank if you want to leave this unchanged.</p>
          
    <c:if test="${editAs == 'admin'}">
    <jsp:useBean id="assignedRoles" type="java.util.ArrayList" scope="request"/>
    <div>
    	<label>Assigned Roles:</label>
    </div>
    <table>
    	<tr>
	    <c:forEach items="${allowedRoles}" var="aRole">
	    <jsp:useBean id="aRole" type="java.lang.String"/>
	    <td><input type="checkbox" value="on" <c:if test="<%= assignedRoles.contains(aRole) %>">CHECKED</c:if> name="${aRole}"
	     <c:if test="${aRole=='ROLE_USER'}"> DISABLED </c:if>
	     /><label>${aRole}</label></td>
	    </c:forEach>
	    </tr>
    </table>
	</c:if>
          
    <table id="userEditForm">
    	<tr>
            <td class="formlabel">First name:</td>
            <td><input name="FIRST_NAME" type="text" value="<c:out value="${FIRST_NAME}"/>" style="width:25em" size="40"  autocomplete="off" class="validate(required, maxlength(30))"/></td>
        </tr>
        <tr>
            <td class="formlabel">Last name:</td>
            <td><input name="LAST_NAME" type="text" value="<c:out value="${LAST_NAME}" />" style="width:25em" size="40"  autocomplete="off" class="validate(required, maxlength(30))"/></td>
        </tr>

        <tr>
            <td class="formlabel">E-mail address:</td>
            <td><input name="EMAIL_ADDR" type="text" value="<c:out value="${EMAIL_ADDR}" />" style="width:25em" size="40"  autocomplete="off" class="validate(email)"/></td>
        </tr>
        <c:if test="${editAs == 'admin'}">
         <tr>
            <td class="formlabel">Active:</td>
            <td>
            	<c:choose>
            		<c:when test="${USER_ACTIVE}">
            			<input name="USER_ACTIVE" type="checkbox" value="active" checked="checked"/>
            		</c:when>
            		<c:otherwise>	
            			<input name="USER_ACTIVE" type="checkbox" value="active" />
            		</c:otherwise>
            	</c:choose>
            </td>
        </tr>
        </c:if>
        <tr>
            <td class="formlabel">User Name:</td>
            <td><input value="<c:out value="${USER_NAME}" />" type="text" size="40"  disabled="true"/></td>
        </tr>
        <tr>
            <td class="formlabel">New password:</td>
            <td><input type="password" id="password" name="PASSWORD" size="40"  autocomplete="off"  class="validate(rangelengthOrBlank(6, 12))"/></td>
        </tr>
        <tr>
            <td class="formlabel">Confirm new password:</td>
            <td><input type="password"  name="CONFIRM_PASSWORD" size="40"   autocomplete="off" class="validate(match(#password))"/></td>
        </tr>
    	<!-- insert meta data fields -->
            <tiles:insertDefinition name="userMetaDataFormFields">
                    <tiles:putAttribute name="metaList" value="${ metaList }"/>
            </tiles:insertDefinition>
    </table>

<div class="buttonpanel textright">
	<input name="submit" type="submit" value="Submit" class="form_action" />
</div>
</div>

</form>