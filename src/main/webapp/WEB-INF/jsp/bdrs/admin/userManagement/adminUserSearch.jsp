<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<jsp:useBean id="context" scope="request" type="au.com.gaiaresources.bdrs.servlet.RequestContext"></jsp:useBean>

<h1>Search Users</h1>


<div class="input_container">
<h2>Enter fields</h2>
	<form id="userSearchForm">
		<input type="hidden" id ="ident" name="ident" value="<%= context.getUser().getRegistrationKey() %>"/>
		<table id="userSearchForm">
	    	<tr>
	    		<td><input type="hidden" name="search" value="true"/></td>
			</tr>
	    	<tr>
	            <td class="formlabel">Login:</td>
	            <td><input id ="userName" name="userName" value="<c:out value="${userName}" />" size="40"  autocomplete="off" onkeydown="doSearch(arguments[0]||event)"/></td>
	        </tr>
	        <tr>
	            <td class="formlabel">Email Address:</td>
	            <td><input id="emailAddress" name="emailAddress" value="<c:out value="${emailAddress}" />" size="40"  autocomplete="off" onkeydown="doSearch(arguments[0]||event)"/></td>
	        </tr>
	        <tr>
	            <td class="formlabel">Name:</td>
	            <td><input id="fullName" name="fullName" value="<c:out value="${FULL_NAME}" />" size="40"  autocomplete="off" onkeydown="doSearch(arguments[0]||event)"></td>
	        </tr>
	        <tr>
	            <td class="formlabel">Auto Search:</td>
	            <td><input type="checkbox" id="autosearch" onclick="enableAutosubmit(this.checked)"></td>
	        </tr>
	    </table>
		<div class="buttonpanel textright">
			<input name="search" type="button" onclick="gridReload()" value="Search" class="form_action" />
			<input id="downloadXLS" name="downloadXLS" type="button" onclick="bdrs.downloadXls(this)" value="Download XLS" class="form_action" />
		</div>
	</form>
</div>

</br>

<table id="userList"></table>
<div id="pager2"></div>

<script type="text/javascript">

	var actionLinkFormatter = function(cellvalue, options, rowObject) {
	    var links = new Array();
	    links.push('<a style="color:blue" href="${pageContext.request.contextPath}/admin/profile.htm?USER_ID=' + rowObject.id + '">Edit</a>');
	    return links.join(" | ");
	};
    
    jQuery("#userList").jqGrid({
            //url: jQuery("#${widgetId}").data("url"),
            url: '${pageContext.request.contextPath}/webservice/user/searchUsers.htm',
            datatype: "json",
            mtype: "GET",
            colNames:['Login','Given Name','Surname', 'Email Address', 'Action'],
            colModel:[
                {name:'userName',index:'name', width:55},
                {name:'firstName',index:'firstName', width:90},
                {name:'lastName', index:'lastName'},
                {name:'emailAddress',index:'emailAddress', width:100},
                {name:'action', width:25, sortable:false, formatter:actionLinkFormatter}
            ],
            autowidth: true,
            jsonReader : { repeatitems: false },
            rowNum:10,
            rowList:[10,20,30],
            pager: '#pager2',
            sortname: 'name',
            viewrecords: true,
            sortorder: "asc",
            caption:"User Listing"
    });

    jQuery("#userList").jqGrid('navGrid','#pager2',{edit:false,add:false,del:false});
    var userSearchFormParams = jQuery("#userSearchForm").serialize();
    jQuery("#downloadXLS").data("xlsURL", "${pageContext.request.contextPath}/webservice/user/downloadUsers.htm?"+userSearchFormParams);

    var timeoutHnd;
    var flAuto = false;

    
    function doSearch(ev){
    	if(!flAuto)
    		return;
    	if(timeoutHnd)
    		clearTimeout(timeoutHnd)
    	timeoutHnd = setTimeout(gridReload,500)
    }

    function gridReload(){
    	var userSearchFormParams = jQuery("#userSearchForm").serialize();
    	console.log(userSearchFormParams);
    	jQuery("#downloadXLS").data("xlsURL", "${pageContext.request.contextPath}/webservice/user/downloadUsers.htm?"+userSearchFormParams);
    	jQuery("#userList").jqGrid('setGridParam',{
        	url:"${pageContext.request.contextPath}/webservice/user/searchUsers.htm?"+userSearchFormParams,
        	page:1}).trigger("reloadGrid");
    }
    
    function enableAutosubmit(state){
    	flAuto = state;
    	jQuery("#submitButton").attr("disabled",state);
    }

    // Download XLS
    bdrs.downloadXls = function(elem) {
        var xlsURL = jQuery(elem).data("xlsURL");
        if(xlsURL !== null && xlsURL !== undefined && xlsURL.length > 0) {
            window.document.location = xlsURL;
        } else {
            return false;
        }
    };
</script>