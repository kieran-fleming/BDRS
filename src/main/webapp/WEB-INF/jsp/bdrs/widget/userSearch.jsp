<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<tiles:useAttribute name="widgetId" />
<tiles:useAttribute name="multiselect" />
<tiles:useAttribute name="scrollbars" ignore="true" />
<tiles:useAttribute name="baseQueryString" ignore="true" />

<h4>Search Users</h4>   
    <form id="${widgetId}SearchForm">
	    <table>
	        <tr>
	            <td class="formlabel">Login:</td>
	            <td><input type="text" style="width:15em" name="userName" value="<c:out value="" />" size="40"  autocomplete="off"/></td>
	        </tr>
	        <tr>
	            <td class="formlabel">Email Address:</td>
	            <td><input type="text" style="width:15em" name="emailAddress" value="<c:out value="" />" size="40"  autocomplete="off" /></td>
	        </tr>
	        <tr>
	            <td class="formlabel">Name:</td>
	            <td><input type="text" style="width:15em" name="fullName" value="<c:out value="" />" size="40"  autocomplete="off" /></td>
	        </tr>
	    </table>
    </form>
<div class="buttonpanel textright">
    <button id="${widgetId}Filter" class="form_action">Filter</button>
</div>

<div id="${widgetId}Wrapper">
<table id="${widgetId}"></table>
<div id="${widgetId}Pager"></div>
</div>

<script type="text/javascript">
    // creates a little helper object and does some data init. See the object definition in bdrs.js 
    // for more details....
    var ${widgetId}Grid = new bdrs.JqGrid("#${widgetId}", '${pageContext.request.contextPath}/webservice/user/searchUsers.htm', '${baseQueryString}');
    
    //jQuery("#${widgetId}").data("url", ${widgetId}Grid.createUrl());
    
    jQuery("#${widgetId}").jqGrid({
            //url: jQuery("#${widgetId}").data("url"),
            url: ${widgetId}Grid.createUrl(),
            datatype: "json",
            mtype: "GET",
            colNames:['Login','Given Name','Surname', 'Email Address'],
            colModel:[
                {name:'userName',index:'name', width:55},
                {name:'firstName',index:'firstName', width:90},
                {name:'lastName', index:'lastName'},
                {name:'emailAddress',index:'emailAddress', width:100}
            ],
            autowidth: true,
            jsonReader : { repeatitems: false },
            rowNum:10,
            rowList:[10,20,30],
            pager: '#${widgetId}Pager',
            sortname: 'name',
            viewrecords: true,
            sortorder: "asc",
            <c:if test="${multiselect == true}">         
                multiselect: true,
            </c:if>
            <c:if test="${scrollbars != true}">
                width: '100%',
                height: '100%',
            </c:if>
            caption:"User Listing"
    });
    
    <c:if test="${scrollbars != true}">
    jQuery("#${widgetId}Wrapper .ui-jqgrid-bdiv").css('overflow-x', 'hidden');
    </c:if>
    
    jQuery("#${widgetId}Pager").jqGrid('navGrid','#${widgetId}Pager',{edit:false,add:false,del:false});
    jQuery("#${widgetId}Filter}").click(function(){
    // turn the search form into a query string and append it to our url...
        var f = jQuery("#${widgetId}SearchForm").serialize();       
        ${widgetId}Grid.setQueryString(f);
        ${widgetId}Grid.reload();
    });

</script>