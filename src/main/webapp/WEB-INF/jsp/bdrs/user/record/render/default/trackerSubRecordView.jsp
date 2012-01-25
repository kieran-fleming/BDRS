<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>


<%@page import="au.com.gaiaresources.bdrs.model.record.Record"%>
<jsp:useBean id="record" scope="request" type="au.com.gaiaresources.bdrs.model.record.Record" />
<jsp:useBean id="survey" scope="request" type="au.com.gaiaresources.bdrs.model.survey.Survey" />

<%-- bread crumb style parent navigation --%>
<div>
	<h2>
		Parent Records
	</h2>
	
	<div class="trackerRelatedRecordsSection">
		<c:choose>
		    <c:when test="${not empty parentRecordList}">
			    <c:forEach var="r" items="${parentRecordList}" varStatus="loopStatus">
			        <a href="${pageContext.request.contextPath}/bdrs/user/surveyRenderRedirect.htm?recordId=${r.id}&selectedTab=${selectedTab}"><c:out value="${r.censusMethod.name}" />&nbsp;</a>
					<c:if test="${not loopStatus.last}">
						<span class="breadcrumbSeparator">&nbsp;>&nbsp;&nbsp;</span>
					</c:if>
			    </c:forEach>
		    </c:when>
		    <c:otherwise>
		        <p>
		            This record has no parents.
		        </p>
		    </c:otherwise>
	    </c:choose>
	</div>
</div>

<h2>Child Records</h2>

<c:forEach var="cm" items="${record.censusMethod.censusMethods}">
	<c:set var="gridTargetId" value="gridTarget${cm.id}" />
	<div>
		<c:if test="${ recordWebFormContext.editable }">
			<div class="buttonpanel right">
		       <input type="button" value="Add Sub Record" class="form_action"
			    onclick="javascript:window.location = '${pageContext.request.contextPath}/bdrs/user/surveyRenderRedirect.htm?surveyId=${survey.id}&censusMethodId=${cm.id}&parentRecordId=${record.id}'" />
		    </div>
		</c:if>
		<h3><c:out value="${cm.name}" /></h3>
	    <div id="${gridTargetId}"></div>
	</div>
	
	<script type="text/javascript">
    jQuery(function() {
        bdrs.contribute.tracker.createRecordGrid("#${gridTargetId}", "${record.id}", "${cm.id}");
    });
    </script>
</c:forEach>

<c:choose>
	<c:when test="${not empty record.censusMethod.censusMethods}">
		<c:if test="${ not recordWebFormContext.editable and recordWebFormContext.existingRecord }">
		    <c:if test="${recordWebFormContext.unlockable}">
		        <div class="buttonpanel textright">
		            <tiles:insertDefinition name="unlockRecordWidget">
		                <tiles:putAttribute name="recordId" value="${recordWebFormContext.recordId}" />
		                <tiles:putAttribute name="surveyId" value="${recordWebFormContext.surveyId}" />
		            </tiles:insertDefinition>
		        </div>
		    </c:if>
		</c:if>
	</c:when>
	<c:otherwise>
		<div class="trackerRelatedRecordsSection">
			<p>
                There are no eligible sub census methods.
            </p>
		</div>
	</c:otherwise>
</c:choose>