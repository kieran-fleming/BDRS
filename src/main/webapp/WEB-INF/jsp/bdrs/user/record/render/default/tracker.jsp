<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<%@page import="au.com.gaiaresources.bdrs.model.record.Record"%>
<jsp:useBean id="record" scope="request" type="au.com.gaiaresources.bdrs.model.record.Record" />
<jsp:useBean id="survey" scope="request" type="au.com.gaiaresources.bdrs.model.survey.Survey" />

<h1><c:out value="${survey.name}"/></h1>
<c:if test="${censusMethod != null}">
    <!-- using censusmethod description here in case we want to display no text / more indepth text -->
    <p><cw:validateHtml html="${censusMethod.description}"/></p>
</c:if>

<c:if test="${parentRecordId != null or record.parentRecord != null or (censusMethod != null and !empty censusMethod.censusMethods)}">
	<div class="controlPanel ie7">
		<a id="record_tab_handle" class="tab_handle" href="javascript:void(0);">
	        <div
	            <c:choose>
	                <c:when test="${ \"record\" == selectedTab }">
	                    class="displayTab left displayTabSelected"
	                </c:when>
	                <c:otherwise>
	                    class="displayTab left"
	                </c:otherwise>
	            </c:choose>
	            >
	            Record
	        </div>
	    </a>
	    <a id="sub_record_tab_handle" class="tab_handle" href="javascript:void(0);">
	        <div
	            <c:choose>
	                <c:when test="${ \"subRecord\" == selectedTab }">
	                    class="displayTab left displayTabSelected"
	                </c:when>
	                <c:otherwise>
	                    class="displayTab left"
	                </c:otherwise>
	            </c:choose>
	            >
	            Related Records
	        </div>
	    </a>
		<div class="clear"></div>
	</div>
</c:if>

<script type="text/javascript">
	jQuery(function() {
		var selectedTab = "${selectedTab}";
		var surveyId = "${survey.id}";
		var censusMethodId = "${censusMethod.id}";
		var recordId = "${record.id}";
        bdrs.contribute.tracker.init(selectedTab, surveyId, censusMethodId, recordId);
	});
</script>

 <c:choose>
    <c:when test="${ \"record\" == selectedTab }">
        <tiles:insertDefinition name="trackerRecordView">
        </tiles:insertDefinition>        
    </c:when>
	<c:when test="${ \"subRecord\" == selectedTab }">
        <tiles:insertDefinition name="trackerSubRecordView">
        </tiles:insertDefinition>       
    </c:when>
</c:choose>
