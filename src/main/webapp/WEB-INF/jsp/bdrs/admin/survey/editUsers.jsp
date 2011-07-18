<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<%@page import="au.com.gaiaresources.bdrs.controller.survey.UserSelectionType"%>
<jsp:useBean id="listType" type="au.com.gaiaresources.bdrs.controller.survey.UserSelectionType" scope="request"/>
<jsp:useBean id="survey" type="au.com.gaiaresources.bdrs.model.survey.Survey" scope="request"/>
<jsp:useBean id="context" scope="request" type="au.com.gaiaresources.bdrs.servlet.RequestContext"></jsp:useBean>

<h1>Choose Groups &amp; Users</h1>
<form method="POST" action="${pageContext.request.contextPath}/bdrs/admin/survey/editUsers.htm">
    <input type="hidden" name="surveyId" value="${survey.id}"/>

    <div>
        <p>
            The following groups and users will be able to log a record in
            this survey.
        </p>

        <fieldset id="userSelectionTypeFieldSet">
            <c:forEach items="<%=UserSelectionType.values()%>" var="type">
                <jsp:useBean id="type" type="au.com.gaiaresources.bdrs.controller.survey.UserSelectionType" />
                <div>
                    <input id="<%= type.getCode() %>"
                           class="vertmiddle"
                           type="radio"
                           name="userSelectionType"
                           value="<%= type.toString() %>"
                           onchange="jQuery('.userSelectionWrapper').hide(); jQuery('#<%= type.getCode() %>_wrapper').show();"
                           <c:choose>
                               <c:when test="<%= type.equals(listType) && survey.isPublic() %>">
                                   checked="checked"
                               </c:when>
                               <c:when test="<%= type.equals(listType) && !survey.isPublic() %>">
                                   checked="checked"
                               </c:when>
                           </c:choose>
                    />
                    <label for="<%= type.getCode() %>" title="<%= type.getTip() %>">
                        <%= type.getName() %>
                    </label>
                </div>
            </c:forEach>
        </fieldset>
    </div>

    <div id="ALL_wrapper" class="userSelectionWrapper"
        <c:if test="<%= UserSelectionType.ALL_USERS != listType %>">
            style="display:none"
        </c:if>
    >
        <h2>All Users</h2>
        <p>
            All users will be added automatically when saved.
        </p>
    </div>

    <div id="SELECTED_wrapper" class="userSelectionWrapper"
        <c:if test="<%= UserSelectionType.SELECTED_USERS != listType %>">
            style="display:none"
        </c:if>
    >
        <h2>Selected Users</h2>
        <div class="userSelectionChoice">
            <label for="user_search">User or Group: </label>
            <input id="user_search" type="text" name="user_search" placeholder="Search" onkeydown="if(event.keyCode==13){return false;}"/>

            <h3>Users</h3>
            <p class="italics">
                Consider adding yourself as a User to this Project or the project will not be available via the Contribute menu.
            </p>
            <table id="userTable" class="datatable">
                <thead>
                    <tr>
                        <th>Username</th>
                        <th>First Name</th>
                        <th>Last Name</th>
                        <th>Email</th>
                        <th>Delete</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${survey.users}" var="user">
                        <tr>
                            <td>
                                <c:out value="${user.name}"/>
                                <input type="hidden" name="users" value="${user.id}"/>
                            </td>
                            <td><c:out value="${user.firstName}"/></td>
                            <td><c:out value="${user.lastName}"/></td>
                            <td>
                                <a href="mailto:<c:out value="${user.emailAddress}"/>">
                                    <c:out value="${user.emailAddress}"/>
                                </a>
                            </td>
                            <td class="textcenter">
                                <a href="javascript: void(0);" onclick="jQuery(this).parents('tr').remove();">
                                    <img src="${pageContext.request.contextPath}/images/icons/delete.png" alt="Delete"/>
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>

            <h3>Groups</h3>
            <table id="groupTable" class="datatable">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Description</th>
                        <th class="nowrap">No. of Users</th>
                        <th>Delete</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${survey.groups}" var="group">
                        <tr>
                            <td>
                                <c:out value="${group.name}"/>
                                <input type="hidden" name="groups" value="${group.id}"/>
                            </td>
                            <td><c:out value="${group.description}"/></td>
                            <td class="textcenter">
                                <jsp:useBean id="group" type="au.com.gaiaresources.bdrs.model.group.Group" />
                                <%= group.getUsers().size() %>
                            </td>
                            <td  class="textcenter">
                                <a href="javascript: void(0);" onclick="jQuery(this).parents('tr').remove();">
                                    <img src="${pageContext.request.contextPath}/images/icons/delete.png" alt="Delete"/>
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>

            <script type="text/javascript">
                jQuery("#user_search").autocomplete({
                   source: function(request, callback) {
                       var params = {};
                       params.q = request.term;
                       params.ident = '${context.user.registrationKey}';

                       jQuery.getJSON('${pageContext.request.contextPath}/webservice/user/searchUserAndGroup.htm', params, function(data, textStatus) {
                           var result;
                           var resultsArray = [];

                           // Users
                           var user;
                           var userArray = data.User;
                           for(var i=0; i<userArray.length; i++) {
                               result = {};
                               user = userArray[i];

                               result.value = bdrs.user.getLastFirstUsername(user);
                               result.label = "User: " + result.value;

                               result.data = user;
                               resultsArray.push(result);
                           }

                           // Groups
                           var group;
                           var groupArray = data.Group;
                           for(var i=0; i<groupArray.length; i++) {
                               result = {};
                               group = groupArray[i];

                               // It is a group
                               result.value = group.name;
                               result.label = "Group: " + group.name;

                               result.data = group
                               resultsArray.push(result);
                           }
                           callback(resultsArray);
                       });
                   },
                   select: function(event, ui) {

                       var user = null;
                       var group = null;
                       var addEntry = true;
                       var table;
                       var row = jQuery("<tr></tr>");

                       if(ui.item.data._class == "User") {
                           user = ui.item.data;
                           table = jQuery("#userTable");

                           if(jQuery('[name=users][value='+user.id+']').length > 0) {
                               // Already added. We don't want to add it twice.
                               addEntry = false;
                           }

                       } else {
                           group = ui.item.data;
                           table = jQuery("#groupTable");
                           if(jQuery('[name=groups][value='+group.id+']').length > 0) {
                               // Already added. We don't want to add it twice.
                               addEntry = false;
                           }
                       }

                       if(addEntry === true) {
                           if(user !== null) {
                               var username = jQuery("<td></td>").text(user.name);
                               var pk = jQuery("<input/>").attr({
                                   'type': 'hidden',
                                   'value': user.id,
                                   'name': 'users'
                               });
                               username.append(pk);
                               var firstName = jQuery("<td></td>").text(user.firstName);
                               var lastName = jQuery("<td></td>").text(user.lastName);
                               var email = jQuery("<td></td>");
                               var mailto = jQuery("<a></a>").text(user.emailAddress);
                               mailto.attr("href", "mailto:"+user.emailAddress);
                               email.append(mailto);
                               var del = jQuery("<td></td>").addClass("textcenter");
                               var delLink = jQuery('<a><img src="${pageContext.request.contextPath}/images/icons/delete.png" alt="Delete"/></a>');
                               delLink.attr("href", "javascript: void(0);").click(function () {
                                   jQuery(this).parents("tr").remove();
                               });
                               del.append(delLink);

                               row.append(username).append(firstName).append(lastName).append(email).append(del);
                           }

                           if(group !== null) {
                               var name = jQuery("<td></td>").text(group.name);
                               var pk = jQuery("<input/>").attr({
                                   'type': 'hidden',
                                   'value': group.id,
                                   'name': 'groups'
                               });
                               name.append(pk);
                               var description = jQuery("<td></td>").text(group.description);
                               var numUsers = jQuery("<td></td>").addClass("textcenter").text(group.users.length);
                               var del = jQuery("<td></td>").addClass("textcenter");
                               var delLink = jQuery('<a><img src="${pageContext.request.contextPath}/images/icons/delete.png" alt="Delete"/></a>');
                               delLink.attr("href", "javascript: void(0);").click(function () {
                                   jQuery(this).parents("tr").remove();
                               });
                               del.append(delLink);

                               row.append(name).append(description).append(numUsers).append(del);
                           }

                           table.find("tbody").append(row);
                       }

                       jQuery(event.target).select();
                       return false;
                   },
                   minLength: 2,
                   delay: 300,
                   html:true
                });
            </script>

        </div>
    </div>



    <div class="textright buttonpanel">
        <input type="submit" class="form_action" value="Save"/>
        <input type="submit" class="form_action" name="saveAndContinue" value="Save And Continue"/>
    </div>
</form>
