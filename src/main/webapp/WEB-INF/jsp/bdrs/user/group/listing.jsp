<!-- list the user groups currently in the database  -->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>

<h1>User Groups</h1>

<%-- 
<c:if test="${pagedGroupResult != null}">
    <display:table name="pagedGroupResult.list" id="usersearchresults" 
        decorator="au.com.gaiaresources.bdrs.controller.admin.AdminUserSearchTableDecorator"
        style="width:100%" 
        pagesize="10" sort="external" partialList="true" size="pagedGroupResult.count"
        class="datatable">
        <display:column property="name" title="Group name" sortable="true" sortName="name" escapeXml="true" />
        <display:column property="description" title="Description" sortable="false" escapeXml="true" />
        <display:column property="actionLinks" title="Actions" />
    </display:table>
</c:if>
--%>



<tiles:insertDefinition name="groupSearch">
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


<%-- 
<tiles:insertDefinition name="userSearch">
       <tiles:putAttribute name="id" value="yomomma"/>
       <tiles:putAttribute name="multiselect" value="false"/>
</tiles:insertDefinition>
 --%>

<!-- testing out searching for users webservice and widget.... -->

<!-- 
<table id="searchUsers"></table>
<div id="searchUsersPager"></div>
 -->

<!--
<script src="{pageContext.request.contextPath}/js/jquery.layout.js" type="text/javascript"></script>
-->

<!-- 
<script src="{pageContext.request.contextPath}/js/ui.multiselect.js" type="text/javascript"></script>
-->
 <!-- 
<script src="${pageContext.request.contextPath}/js/jquery.jqGrid.min.js" type="text/javascript"></script>
 -->




<!-- 
<script src="{pageContext.request.contextPath}/js/jquery.tablednd.js" type="text/javascript"></script>
<script src="{pageContext.request.contextPath}/js/jquery.contextmenu.js" type="text/javascript"></script>
-->

<!-- 

<script type="text/javascript">
    //$.jgrid.no_legacy_api = true;
   // $.jgrid.useJSON = true;
</script>

<script type="text/javascript">
    //jQuery().ready(function() {
	    jQuery("#searchUsers").jqGrid({
		    url:'${pageContext.request.contextPath}/webservice/user/searchUsers.htm',
		    datatype: "json",
		    mtype: "GET",
		    colNames:['Login','Given Name','Surname', 'Email Address'],
		    colModel:[
		        //{name:'id',index:'id', width:55},
		        {name:'userName',index:'name', width:55},
		        {name:'firstName',index:'firstName', width:90},
		        {name:'lastName', index:'lastName'},
		        {name:'emailAddress',index:'emailAddress', width:100}
		    ],
		    autowidth: true,
		    jsonReader : { repeatitems: false },
		    rowNum:10,
		    rowList:[10,20,30],
		    pager: '#searchUsersPager',
		    sortname: 'id',
		    viewrecords: true,
		    sortorder: "desc",
		    caption:"Search Users"
		});
		jQuery("#searchUsersPager").jqGrid('navGrid','#searchUsersPager',{edit:false,add:false,del:false});
	//});
</script>
 -->
 