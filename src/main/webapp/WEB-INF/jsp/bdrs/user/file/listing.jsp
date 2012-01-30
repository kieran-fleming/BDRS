<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<h1>Manage Files</h1>

<cw:getContent key="user/managedFileListing" />

<div class="input_container">
<h3>Search Files</h3>
<form class="widgetSearchForm" id="searchForm" method="GET">
    <table>
        <tr>
            <td class="formlabel"><label for="searchText">Search for</label></td>
            <td><input type="text" name="fileSearchText" id="searchText" size="20" onkeypress="return keyHandler.keyPressed(event);"/></td>
            <td class="formlabel"><label for="user">For user</label></td>
            <td><input type="text" name="userSearchText" id="user" size="20" onkeypress="return keyHandler.keyPressed(event);"/></td>
        </tr>
        <tr>
        	<td class="formlabel">Images only</td>
        	<td> <input type="checkbox" name="imagesOnly" value="True"/></td>
        </tr>	
    </table>

<div class="buttonpanel buttonPanelRight textright">
    <input type="button" id="managedFileSearch" class="form_action" value="Search" onclick="gridReload();"/>
</div>	
<br/>
<p>
    <strong>Searching for a User will match Managed Files either created by or updated by that user.  However only the 
user that last updated the Managed File will be displayed.</strong>
</p>
</form>


</div>

<div class="buttonpanel textright">
<form id="deleteMedia" action="${pageContext.request.contextPath}/bdrs/user/managedfile/delete.htm" method="POST">
    <a href="javascript: bdrs.util.confirmExec('Are you sure you want to delete the selected media?', addSelectedMediaToForm);" class="delete"/>Delete</a>
    &nbsp;|&nbsp;
    <input class="form_action" type="button" value="Add Media" onclick="window.document.location='${pageContext.request.contextPath}/bdrs/user/managedfile/edit.htm'"/>
</form>
</div>

<div class="auto-row-height-jqgrid">
<table id="managedFilesList"></table>
</div>
<div id="pager2"></div>

<script type="text/javascript">

    jQuery(function() {
    	
    	
    	var downloadURL = function(file) {
    		return '${pageContext.request.contextPath}/files/download.htm?'+file;
    	};
    	var actionLinkFormatter = function(cellvalue, options, rowObject) {
	        var editLink = ('<a title="Edit managed file" style="color:blue" href="${pageContext.request.contextPath}/bdrs/user/managedfile/edit.htm?id=' + rowObject.id + '">Edit</a>');
	        return editLink;
	    };
	    var previewFormatter = function(cellvalue, options, rowObject) {
	    	if (rowObject.contentType.substring(0, 5) === 'image') {
	    		return "<img style='width:80px;height:80px;align:center' src='"+downloadURL(rowObject.fileURL)+"'></img>";
	    	}
	    	return "";
	    };
	    var filenameFormatter = function(cellvalue, options, rowObject) {
	    	return "<a href='"+downloadURL(rowObject.fileURL)+"'>"+rowObject.filename+"</a>";
	    };
	   
	    jQuery("#managedFilesList").jqGrid({
	            url: getUserSearchUrl(),
	            datatype: "json",
	            mtype: "GET",
	            colNames:['Id', 'Identifier','Last Modified', 'User', 'Filename / Download', 'Description', 'Preview', 'Action'],
	            colModel:[
	                {name:'id', hidden:true, key:true},
	                {name:'uuid',sortable:false, width:100},
	                {name:'updatedAt',index:'file.updatedAt', sorttype:'date', width:60},
	                {name:'updatedBy.name',index:'updatedBy.firstName', width:60},
	                {name:'filename', index:'filename', width:100, formatter:filenameFormatter},	       
	                {name:'description', index:'description'},
	                {name:'fileURL', width:68, sortable: false, formatter:previewFormatter, valign:'center'},
	                {name:'action', width:40, sortable:false, formatter:actionLinkFormatter, align:'center'}
	            ],
	            height: '100%',
	            autowidth: true,
	            jsonReader : { repeatitems: false },
	            rowNum:10,
	            rowList:[10,20,30],
	            pager: '#pager2',
	            sortname: 'file.updatedAt',
	            viewrecords: true,
	            sortorder: "desc",
	            caption:"Managed Files",
	            multiselect: true
	    });
	
	    jQuery("#managedFilesList").jqGrid('navGrid','#pager2',{edit:false,add:false,del:false});
	});
	
	var SEARCH_USER_URL = '${pageContext.request.contextPath}/bdrs/user/managedfile/service/search.htm';
	
	function getUserSearchUrl() {
		var params = jQuery("#searchForm").serialize();
		return SEARCH_USER_URL + '?' + params;
	};

    function gridReload(){
        jQuery("#managedFilesList").jqGrid('setGridParam',{
            url:getUserSearchUrl(),
            page:1}).trigger("reloadGrid");
    };
    
	var keyHandler = {
        keyPressed: function(e) {
            if(e.keyCode == 13) {
                jQuery("#managedFileSearch").click();
                return false; // returning false will prevent the event from bubbling up.
            } else {
                return true;
            }
        }
    };  
    
	function addSelectedMediaToForm() {
		var selected = jQuery("#managedFilesList").jqGrid('getGridParam','selarrrow');
		if (selected.length > 0) {
			var hiddenFields = "";
			for (var i in selected) {
				hiddenFields+='<input type="hidden" name="managedFilePk" value="'+selected[i]+'"/>';
			}
			
			jQuery('#deleteMedia').append(hiddenFields).submit();
		}
	};
</script>


