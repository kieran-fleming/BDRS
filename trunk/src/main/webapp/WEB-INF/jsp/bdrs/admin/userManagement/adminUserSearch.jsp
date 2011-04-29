<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>

<h1>Search Users</h1>

<form method="GET">
<div class="input_container">
<h2>Enter fields</h2>
          
    <table id="userSearchForm">
    	<tr>
    		<td><input type="hidden" name="search" value="true"/></td>
		</tr>
    	<tr>
            <td class="formlabel">Login:</td>
            <td><input name="name" value="<c:out value="${name}" />" size="40"  autocomplete="off"/></td>
        </tr>
        <tr>
            <td class="formlabel">Email Address:</td>
            <td><input name="emailAddress" value="<c:out value="${emailAddress}" />" size="40"  autocomplete="off"/></td>
        </tr>

        <tr>
            <td class="formlabel">Name:</td>
            <td><input name="FULL_NAME" value="<c:out value="${FULL_NAME}" />" size="40"  autocomplete="off"></td>
        </tr>
        
        <tr>
            <td class="formlabel"></td>
            <td><i>(Leave fields empty to display all users)</i></td>
        </tr>
    </table>

<div class="buttonpanel textright">
	<input name="search" type="submit" value="Search" class="form_action" />
</div>
</div>

</form>

<c:if test="${pagedUserResult != null}">
	<display:table name="pagedUserResult.list" id="usersearchresults" 
		decorator="au.com.gaiaresources.bdrs.controller.admin.AdminUserSearchTableDecorator"
		style="width:100%" 
		pagesize="10" sort="external" partialList="true" size="pagedUserResult.count"
		class="datatable">
		<display:column property="name" title="Login" sortable="true" sortName="name" escapeXml="true" />
		<display:column property="emailAddress" title="Email" sortable="true" sortName="emailAddress" />
		<display:column title="Name" sortable="true" sortName="firstName"  escapeXml="true">
			<c:out value="${usersearchresults.firstName} ${usersearchresults.lastName}" />
		</display:column>
		<display:column property="actionLinks" title="Actions" />
	</display:table>
</c:if>
	
