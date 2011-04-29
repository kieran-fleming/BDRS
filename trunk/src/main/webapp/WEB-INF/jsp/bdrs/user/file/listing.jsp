<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<h1>Managed Files</h1>
<cw:getContent key="user/managedFileListing" />

<form id="managedFileListing" method="POST" action="${pageContext.request.contextPath}/bdrs/user/managedfile/delete.htm">
	<display:table name="managedFilePaginator.list" id="managedFileListingTable" 
	    decorator="au.com.gaiaresources.bdrs.controller.file.ManagedFileTableDecorator"
	    style="width:100%" pagesize="50" sort="external" partialList="true" size="managedFilePaginator.count"
	    class="datatable">
	    
	    <display:column property="selection" title="" class="textcenter"/>
	    <display:column escapeXml="true" property="uuid" title="Identifier" sortable="false"/>
	    <display:column property="filename" title="Filename/Download" sortable="true" sortName="filename" />
	    <display:column escapeXml="true" property="contentType" title="Content Type" sortable="true" sortName="contentType" />
	    <display:column escapeXml="true" property="description" title="Description" sortable="true" sortName="description" />
	    <display:column property="actionLinks" title="Actions" class="textcenter"/>
	</display:table>

	<div class="textright">
	    <a href="javascript: bdrs.util.confirmSubmit('Are you sure you want to delete the selected media?', '#managedFileListing');" class="delete"/>Delete</a>
	    &nbsp;|&nbsp;
	    <input class="form_action" type="button" value="Add Media" onclick="window.document.location='${pageContext.request.contextPath}/bdrs/user/managedfile/edit.htm'"/>
	</div>
</form>
