<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<h1>Galleries</h1>

<p>
	These are your available galleries.
</p>

<p>On this page you may:</p>
<ul>
	<li>Browse your available galleries</li>
	<li>Add a new gallery</li>
	<li>Edit an existing gallery</li>
	<li>Delete an existing gallery</li>
</ul>

<tiles:insertDefinition name="galleryGrid">
       <tiles:putAttribute name="widgetId" value="galleryList"/>
       <tiles:putAttribute name="multiselect" value="false"/>
       <tiles:putAttribute name="scrollbars" value="false" />
       <tiles:putAttribute name="showActions" value="true" />
       <tiles:putAttribute name="editUrl" value="${pageContext.request.contextPath}/bdrs/admin/gallery/edit.htm" />
       <tiles:putAttribute name="deleteUrl" value="${pageContext.request.contextPath}/bdrs/admin/gallery/delete.htm" />
</tiles:insertDefinition>

<div class="buttonpanel textright">
    <input class="form_action" type="button" value="Add Gallery" onclick="window.location = '${pageContext.request.contextPath}/bdrs/admin/gallery/edit.htm';"/>
</div>

