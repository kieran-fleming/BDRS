<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<jsp:useBean id="record" scope="request" type="au.com.gaiaresources.bdrs.model.record.Record" />
<jsp:useBean id="survey" scope="request" type="au.com.gaiaresources.bdrs.model.survey.Survey" />

<%@page import="au.com.gaiaresources.bdrs.model.taxa.Attribute"%>
<%@page import="au.com.gaiaresources.bdrs.model.taxa.AttributeScope"%>

<h1><c:out value="${survey.name}"/></h1>
<c:if test="${censusMethod != null}">
    <!-- using censusmethod description here in case we want to display no text / more indepth text -->
    <!-- escapeXml false so we can put anchors in the description -->
    <p><c:out value="${censusMethod.description}" escapeXml="false" /></p>
</c:if>

    <tiles:insertDefinition name="recordEntryMap">
        <tiles:putAttribute name="survey" value="${survey}"/>
        <tiles:putAttribute name="censusMethod" value="${censusMethod}"/>
    </tiles:insertDefinition>
	
	<c:if test="${ not preview }">
	    <form method="POST" action="${pageContext.request.contextPath}/bdrs/user/singleSiteMultiTaxa.htm" enctype="multipart/form-data">
	</c:if>
	<input type="hidden" name="surveyId" value="${survey.id}"/>
	
	<div id="tableContainer">
	<table class="form_table">
	    <tbody>
	        <c:forEach items="${formFieldList}" var="formField">
				<tiles:insertDefinition name="formFieldRenderer">
				    <tiles:putAttribute name="formField" value="${formField}"/>
				    <tiles:putAttribute name="locations" value="${locations}"/>
				</tiles:insertDefinition>
	        </c:forEach>
	    </tbody>
	</table>
	
	
	<div id="sightingsContainer">
	    <!-- Add sightings description text -->
	    <cw:getContent key="user/singleSiteMultiTaxaTable" />
		<div id="add_sighting_panel" class="textright">
	        <a id="maximiseLink" class="text-left" href="javascript:bdrs.util.maximise('#maximiseLink', '#sightingsContainer', 'Enlarge Table', 'Shrink Table')">Enlarge Table</a>
		    <input type="hidden" id="sighting_index" name="sightingIndex" value="0"/>  
		    <input class="form_action" type="button" value="Add Sighting" onclick="bdrs.contribute.singleSiteMultiTaxa.addSighting('#sighting_index', '[name=surveyId]', '#sightingTable tbody');"/>
		</div>
		<table id="sightingTable" class="datatable">
		    <thead>
		       <tr>
		           <c:forEach items="${ sightingRowFormFieldList }" var="sightingRowFormField">
		               <th>
			               <jsp:useBean id="sightingRowFormField" type="au.com.gaiaresources.bdrs.controller.attribute.formfield.AbstractRecordFormField" />
			               <c:choose>
		                       <c:when test="<%= sightingRowFormField.isPropertyFormField() %>">
		                           <c:choose>
			                            <c:when test="${ 'species' == sightingRowFormField.propertyName }">
			                                Species                                        
			                            </c:when>
			                            <c:when test="${ 'number' == sightingRowFormField.propertyName }">
			                                Number                            
			                            </c:when>
		                            </c:choose>
		                       </c:when>
		                       <c:when test="<%= sightingRowFormField.isAttributeFormField() %>">
		                           <c:out value="${ sightingRowFormField.attribute.description }"/>
		                       </c:when> 
		                   </c:choose>
	                   </th>
		           </c:forEach>
		       </tr>
		    </thead>
		    <tbody>
		    </tbody>
		</table>
	</div>
	
	</div>
	<c:choose>
	    <c:when test="${ preview }">
	        <div class="textright">
	            <input class="form_action" type="button" value="Go Back" onclick="window.document.location='${pageContext.request.contextPath}/bdrs/admin/survey/editAttributes.htm?surveyId=${survey.id}'"/>
	            <input class="form_action" type="button" value="Continue" onclick="window.document.location='${pageContext.request.contextPath}/bdrs/admin/survey/locationListing.htm?surveyId=${survey.id}'"/>
	        </div>
	    </c:when>
	    <c:otherwise>
	            <div class="textright">
	                <input class="form_action" type="submit" name="submitAndAddAnother" value="Submit and Add Another"/>
	                <input class="form_action" type="submit" name="submit" value="Submit Sightings"/>
	            </div>
	        </form>
	    </c:otherwise>
	</c:choose>

<noscript>
    <tiles:insertDefinition name="noscriptMessage"></tiles:insertDefinition>
</noscript>

<script type="text/javascript">
    jQuery(function() {
        jQuery("#script_content").removeClass("hidden");
    });
</script>

<script type="text/javascript">
    jQuery(window).load(function() {
        /**
         * Prepopulate fields
         */
        bdrs.contribute.singleSiteAllTaxa.addSighting('#sighting_index', '[name=surveyId]', '#sightingTable tbody');
        bdrs.form.prepopulate();
    });
</script>