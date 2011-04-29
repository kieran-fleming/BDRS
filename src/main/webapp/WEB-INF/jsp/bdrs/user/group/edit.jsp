<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<c:choose>
    <c:when test="${ group.id == null }">
        <h1>Add Group</h1>
    </c:when>
    <c:otherwise>
        <h1>Edit Group</h1>
    </c:otherwise>
</c:choose>


<form method="POST">
    <input type="hidden" name="group_pk" value="${group.id}" />
	<table>
	    <tr>
	        <td>Group Name:</td>
	        <td><input class="validate(required, maxlength(200))" type="text" style="width:40em" name="name" value="<c:out value="${group.name}" />" size="40"  autocomplete="off"></td>
	    </tr>
	    <tr>
	        <td>Group Description:</td>
	        <td><input class="validate(required, maxlength(200))" type="text" style="width:40em" name="desc" value="<c:out value="${group.description}" />" size="100" maxlength="200" autocomplete="off"></td>
	    </tr>
	</table>
	<div class="buttonpanel textright">
	    <input type="submit" class="form_action" type="button" value="Save" />
	 </div>
</form>


<h3>Users</h3>

<%-- 
<cw:getContent key="admin/groupEdit" />
 --%>

<%-- 

<c:if test="${pagedUserResult != null}">
    <display:table name="pagedUserResult.list" id="usersInGroup" 
        decorator="au.com.gaiaresources.bdrs.controller.admin.AdminUserSearchTableDecorator"
        style="width:100%" 
        pagesize="10" sort="external" partialList="true" size="pagedUserResult.count"
        class="datatable">
        <display:column property="selection" title="" class="textcenter"/>
        <display:column property="name" title="Login" sortable="true" sortName="name" escapeXml="true" />
        <display:column property="emailAddress" title="Email" sortable="true" sortName="emailAddress" />
        <display:column title="Name" sortable="true" sortName="firstName"  escapeXml="true">
            <c:out value="${usersearchresults.firstName} ${usersearchresults.lastName}" />
        </display:column>
    </display:table>
</c:if>
 --%>

<tiles:insertDefinition name="userSearch">
       <tiles:putAttribute name="widgetId" value="users"/>
       <tiles:putAttribute name="multiselect" value="true"/>
       <tiles:putAttribute name="scrollbars" value="false" />
       <tiles:putAttribute name="baseQueryString" value="parentGroupId=${group.id}" />
</tiles:insertDefinition> 
 
<div class="buttonpanel textright">
    <a href="javascript: bdrs.util.confirmExec('Are you sure you want to remove the selected users', removeUsersCallback);" class="delete"/>Remove</a>
    &nbsp;|&nbsp;
    <!-- 
    <input class="form_action" type="button" value="Add Users" onclick="window.document.location='${pageContext.request.contextPath}/bdrs/admin/group/addUsers.htm'"/></div>
     -->
    <input id="addUsers" class="form_action" type="button" value="Add Users" /></div>

<h3>Groups</h3>

<tiles:insertDefinition name="groupSearch">
       <tiles:putAttribute name="widgetId" value="groups"/>
       <tiles:putAttribute name="multiselect" value="true"/>
       <tiles:putAttribute name="scrollbars" value="false" />
       <tiles:putAttribute name="baseQueryString" value="parentGroupId=${group.id}" />
</tiles:insertDefinition>

<%-- 
<c:if test="${pagedGroupResult != null}">
    <display:table name="pagedGroupResult.list" id="groupsInGroup" 
        style="width:100%" 
        pagesize="10" sort="external" partialList="true" size="pagedGroupResult.count"
        class="datatable">
        <display:column property="selection" title="" class="textcenter"/>
        <display:column property="name" title="Group name" sortable="true" sortName="name" escapeXml="true" />
        <display:column property="description" title="Description" sortable="false" escapeXml="true" />
        <display:column property="actionLinks" title="Actions" />
    </display:table>
</c:if>
 --%>
<div class="buttonpanel textright">
    <a href="javascript: bdrs.util.confirmExec('Are you sure you want to remove the selected groups', removeGroupsCallback);" class="delete"/>Remove</a>
    &nbsp;|&nbsp;
    <input id="addGroups" class="form_action" type="button" value="Add Groups" />
</div>


<!--  DIALOGS GO HERE  ! -->
<div id="addUsersDialog" title="Add Users">
    <tiles:insertDefinition name="userSearch">
           <tiles:putAttribute name="widgetId" value="addUsersGrid"/>
           <tiles:putAttribute name="multiselect" value="true"/>
           <tiles:putAttribute name="scrollbars" value="true" />
    </tiles:insertDefinition>
</div>

<div id="addGroupsDialog" title="Add Groups">
    <tiles:insertDefinition name="groupSearch">
           <tiles:putAttribute name="widgetId" value="addGroupsGrid"/>
           <tiles:putAttribute name="multiselect" value="true"/>
           <tiles:putAttribute name="scrollbars" value="true" />
    </tiles:insertDefinition>
</div>

<script type="text/javascript">
    $(function() {
        $( "#addUsersDialog" ).dialog({
            width: 'auto',
            modal: true,
            autoOpen: false,
            buttons: {
                "Ok": function() {
                    var selected = addUsersGridGrid.getSelected();
                    var postParam = {
                        groupId: ${group.id},
                        userIds: selected.join(',')
                    };
                    bdrs.postWith('${pageContext.request.contextPath}/bdrs/admin/group/addUsers.htm', postParam);
                    $( this ).dialog( "close" );
                },
                Cancel: function() {
                    $( this ).dialog( "close" );
                }
            }
        });
        
        $( "#addGroupsDialog" ).dialog({
            width: 'auto', 
            modal: true,
            autoOpen: false,
            buttons: {
                "Ok": function() {
                    var selected = addGroupsGridGrid.getSelected();
                    var postParam = {
                        groupId: ${group.id},
                        groupIds: selected.join(',')
                    };
                    bdrs.postWith('${pageContext.request.contextPath}/bdrs/admin/group/addGroups.htm', postParam);
                    $( this ).dialog( "close" );
                },
                Cancel: function() {
                    $( this ).dialog( "close" );
                }
            }
        });
        
        $( "#addUsers" )
            .click(function() {
                addUsersGridGrid.reload();
                $( "#addUsersDialog" ).dialog( "open" );
        });
        
        $( "#addGroups" )
            .click(function() {
                addGroupsGridGrid.reload();
                $( "#addGroupsDialog" ).dialog( "open" );
        });
        
        usersGrid.setBaseQueryString("parentGroupId=${group.id}");
        usersGrid.reload();
    });
    
    var removeGroupsCallback = function() {
        var selected = groupsGrid.getSelected();
        var postParam = {
            groupId: ${group.id},
            groupIds: selected.join(',')
        };
        bdrs.postWith('${pageContext.request.contextPath}/bdrs/admin/group/removeGroups.htm', postParam);
    };
    
    var removeUsersCallback = function() {
        var selected = usersGrid.getSelected();
        var postParam = {
            groupId: ${group.id},
            userIds: selected.join(',')
        };
        bdrs.postWith('${pageContext.request.contextPath}/bdrs/admin/group/removeUsers.htm', postParam);
    };
</script>

