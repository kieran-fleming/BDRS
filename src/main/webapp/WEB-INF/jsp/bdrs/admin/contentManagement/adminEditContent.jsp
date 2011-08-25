<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/markitup/jquery.markitup.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/bdrs/admin.js"></script>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/markitup/sets/html/style.css" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/markitup/skins/markitup/style.css" />

<h1>Edit Website Content</h1>

<sec:authorize ifAnyGranted="ROLE_ADMIN">
<div>
    <button onclick="bdrs.admin.adminEditContent.resetContent()">Reset all content to default</button>
</div>
</sec:authorize>

<div class="input_container">
<div>
	<p>This page is where you can edit a range of pages throughout the web site.  To do this, you select the area you want to change, and then you can modify the text using the editor that is present in the page below.  These changes will be reflected immediately across the web site when you save them.</p>
	<label>Select the area of the content you would like to edit: </label>
	<select id="selectContentToEdit" onchange="bdrs.admin.onSelectContentEditorChange()">
	<c:forEach items="${keys}" var="k">
		<option value="${k}">${k}</option>
	</c:forEach>
	</select>
</div>
<textarea id="markItUp"></textarea>
<div class="markItUpSubmitButton buttonpanel textright">
    <input id="resetContent" type="button" class="form_action"  value="Reset Current Content Default" onclick="bdrs.admin.adminEditContent.resetCurrentContent()"/>
    <input id="submitEditContent" type="button" class="form_action"  value="Save" />
</div>
</div>
<script type="text/javascript">
    jQuery(document).ready(function() {
        $('#markItUp').markItUp(bdrs.admin.myHtmlSettings);
    });
</script>
