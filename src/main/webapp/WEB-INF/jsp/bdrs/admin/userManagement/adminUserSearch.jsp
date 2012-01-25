<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<jsp:useBean id="context" scope="request" type="au.com.gaiaresources.bdrs.servlet.RequestContext"></jsp:useBean>

<tiles:useAttribute name="approveUsers" ignore="true"/>

<div class="input_container">
<h2>Search Users</h2>
	<form class="widgetSearchForm" id="userSearchForm">
		<input type="hidden" id ="ident" name="ident" value="<%= context.getUser().getRegistrationKey() %>"/>
		<c:if test="${approveUsers}">
            <input type="hidden" name="active" value="false" />
        </c:if>
		<table id="userSearchForm">
	    	<tr>
	    		<td><input type="hidden" name="search" value="true"/></td>
			</tr>
	    	<tr>
	            <td class="formlabel">Contains:</td>
	            <td><input id ="userName" name="contains" onkeypress="return adminUserSearch.keyPressed(event)" value="<c:out value="${userName}" />" size="40"  autocomplete="off" onkeydown="doSearch(arguments[0]||event)"/></td>
	        </tr>
	        <tr>
	            <td class="formlabel">Auto Search:</td>
	            <td><input type="checkbox" id="autosearch" onclick="enableAutosubmit(this.checked)"></td>
	        </tr>
	    </table>
		<div class="buttonpanel buttonPanelRight textright">
			<input id="searchUsersButton" name="search" type="button" onclick="gridReload()" value="Search" class="form_action" />
			<input id="downloadXLS" name="downloadXLS" type="button" onclick="bdrs.downloadXls(this)" value="Download XLS" class="form_action" />
		</div>
	</form>
</div>

</br>

<table id="userList"></table>
<div id="pager2"></div>

<script type="text/javascript">

    jQuery(function() {
		var actionLinkFormatter = function(cellvalue, options, rowObject) {
	        var links = new Array();
			links.push('<a title="Edit account details" class="fixedLink" href="${pageContext.request.contextPath}/admin/profile.htm?USER_ID=' + rowObject.id + '">Edit</a>');
			<c:if test="${approveUsers}">
			     links.push('<a title="Approve account" class="fixedLink" href="javascript:approveUser(' + rowObject.id + ')">Approve</a>');
			</c:if>
	        return links.join(" | ");
	    };
		
	    jQuery("#userList").jqGrid({
	            url: getUserSearchUrl(),
	            datatype: "json",
	            mtype: "GET",
	            colNames:['Login','Given Name','Surname', 'Email Address', 'Action'],
	            colModel:[
	                {name:'userName',index:'name', width:55},
	                {name:'firstName',index:'firstName', width:90},
	                {name:'lastName', index:'lastName'},
	                {name:'emailAddress',index:'emailAddress', width:100},
	                {name:'action', width:60, sortable:false, formatter:actionLinkFormatter, align:'center'}
	            ],
	            autowidth: true,
	            jsonReader : { repeatitems: false },
	            rowNum:10,
	            rowList:[10,20,30],
	            pager: '#pager2',
	            sortname: 'name',
	            viewrecords: true,
	            sortorder: "asc",
	            height: "100%"
	    });
	
	    jQuery("#userList").jqGrid('navGrid','#pager2',{edit:false,add:false,del:false});
	    var userSearchFormParams = jQuery("#userSearchForm").serialize();
	    jQuery("#downloadXLS").data("xlsURL", "${pageContext.request.contextPath}/webservice/user/downloadUsers.htm?"+userSearchFormParams);
	});
	
	var timeoutHnd;
    var flAuto = false;
    
    function doSearch(ev){
        if(!flAuto)
            return;
        if(timeoutHnd)
            clearTimeout(timeoutHnd)
        timeoutHnd = setTimeout(gridReload,500)
    }
	
	var SEARCH_USER_URL = '${pageContext.request.contextPath}/webservice/user/searchUsers.htm';
	
	function getUserSearchUrl() {
		var params = jQuery("#userSearchForm").serialize();
		return SEARCH_USER_URL + '?' + params;
	}

    function gridReload(){
        var userSearchFormParams = jQuery("#userSearchForm").serialize();
        jQuery("#downloadXLS").data("xlsURL", "${pageContext.request.contextPath}/webservice/user/downloadUsers.htm?"+userSearchFormParams);
		jQuery("#userList").jqGrid('setGridParam',{
            url:getUserSearchUrl(),
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
	
	function approveUser(id) {
		$.ajax({
			type: "POST",
			url:"${pageContext.request.contextPath}/admin/approveUser.htm",
			data: {
				userPk: id
			},
			success: gridReload,
			error: function() {
				bdrs.message.set("Error approving user");
			}
		});
	}
	
	var adminUserSearch = {
        keyPressed: function(e) {
            if(e.keyCode == 13) {
                jQuery("#searchUsersButton").click();
                return false; // returning false will prevent the event from bubbling up.
            } else {
                return true;
            }
        }
    };  
    
</script>