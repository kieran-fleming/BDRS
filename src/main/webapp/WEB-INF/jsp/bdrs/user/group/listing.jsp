<!-- list the user groups currently in the database  -->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<h1>User Groups</h1>

<cw:getContent key="admin/groupListing" />

<tiles:insertDefinition name="groupGrid">
       <tiles:putAttribute name="widgetId" value="groupList"/>
       <tiles:putAttribute name="multiselect" value="false"/>
       <tiles:putAttribute name="scrollbars" value="false" />
       <tiles:putAttribute name="showActions" value="true" />
       <tiles:putAttribute name="editUrl" value="${pageContext.request.contextPath}/bdrs/admin/group/edit.htm" />
       <tiles:putAttribute name="deleteUrl" value="${pageContext.request.contextPath}/bdrs/admin/group/delete.htm" />
</tiles:insertDefinition>

<div class="buttonpanel textright">
    <input class="form_action" type="button" value="Add Group" onclick="bdrs.postWith('${pageContext.request.contextPath}/bdrs/admin/group/create.htm', {});"/>
</div>



 