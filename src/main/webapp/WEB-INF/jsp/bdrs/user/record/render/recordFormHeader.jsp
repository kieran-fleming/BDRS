<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%-- required attributes --%>
<tiles:useAttribute name="recordWebFormContext" />  <%-- RecordWebFormContext --%>

<c:choose>
    <c:when test="${ not recordWebFormContext.preview and not recordWebFormContext.editable and recordWebFormContext.existingRecord }">
    	<c:if test="${recordWebFormContext.unlockable}">
    		<div class="buttonpanel textright">
	            <tiles:insertDefinition name="unlockRecordWidget">
	                <tiles:putAttribute name="recordId" value="${recordWebFormContext.recordId}" />
	                <tiles:putAttribute name="surveyId" value="${recordWebFormContext.surveyId}" />                    
	            </tiles:insertDefinition>
	        </div>
    	</c:if>
    </c:when>
</c:choose>