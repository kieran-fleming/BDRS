<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/markitup/jquery.markitup.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/bdrs/admin.js"></script>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/markitup/sets/html/style.css" />
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/markitup/skins/markitup/style.css" />

<h1>Edit Content</h1>

<div class="input_container">
<div>
	
	<cw:getContent key="admin/content/edit" />
	
	<label>Select the area of the content you would like to edit: </label>
	<select id="selectContentToEdit" onchange="bdrs.admin.onSelectContentEditorChange()">
		<option value="">-- Select Content --</option>
	<c:forEach items="${keys}" var="k">
		<option value="${k}">${k}</option>
	</c:forEach>
	</select>
</div>
<textarea id="markItUp"></textarea>

<sec:authorize ifAnyGranted="ROLE_ADMIN">
<div class="buttonpanel left">
    <input type="button" class="form_action" onclick="bdrs.admin.adminEditContent.resetContent()" value="Reset all content to default" />
</div>
</sec:authorize>

<div class="markItUpSubmitButton buttonpanel textright">
    <input id="resetContent" type="button" class="form_action" value="Reset Current Content Default" onclick="bdrs.admin.adminEditContent.resetCurrentContent()"/>
    <input id="submitEditContent" type="button" class="form_action" value="Save" />
</div>

<div class="clear"></div>

</div>
<script type="text/javascript">
    jQuery(document).ready(function() {
        bdrs.admin.adminEditContent.setTextArea('#markItUp');
        $('#markItUp').markItUp(bdrs.admin.myHtmlSettings);
    });
</script>
