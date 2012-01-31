<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<jsp:useBean id="survey" scope="request" type="au.com.gaiaresources.bdrs.model.survey.Survey" />
<jsp:useBean id="recordFieldCollectionList" scope="request" type="java.util.List" />

<%@page import="au.com.gaiaresources.bdrs.model.taxa.Attribute"%>
<%@page import="au.com.gaiaresources.bdrs.model.taxa.AttributeScope"%>

<%-- Access the facade to retrieve the preference information --%>
<jsp:useBean id="bdrsPluginFacade" scope="request" type="au.com.gaiaresources.bdrs.servlet.BdrsPluginFacade"></jsp:useBean>
<c:set var="showScientificName" value="<%= bdrsPluginFacade.getPreferenceBooleanValue(\"taxon.showScientificName\") %>" />

<h1><c:out value="${survey.name}"/></h1>
<c:if test="${censusMethod != null}">
    <!-- using censusmethod description here in case we want to display no text / more indepth text -->
    <p><cw:validateHtml html="${censusMethod.description}"></cw:validateHtml></p>
</c:if>

    <tiles:insertDefinition name="recordEntryMap">
        <tiles:putAttribute name="survey" value="${survey}"/>
        <tiles:putAttribute name="censusMethod" value="${censusMethod}"/>
    </tiles:insertDefinition>
	
	<c:if test="${ not preview && recordWebFormContext.editable }">
	    <form method="POST" action="${pageContext.request.contextPath}/bdrs/user/singleSiteAllTaxa.htm" enctype="multipart/form-data">
	</c:if>
	<input type="hidden" name="surveyId" value="${survey.id}"/>
	
    <%-- the record form header contains the unlock form icon --%>
    <tiles:insertDefinition name="recordFormHeader">
        <tiles:putAttribute name="recordWebFormContext" value="${recordWebFormContext}" />
    </tiles:insertDefinition>
	
	<div id="tableContainer">
	<table class="form_table">
	    <tbody>
	        <c:forEach items="${formFieldList}" var="formField">
	         <jsp:useBean id="formField" type="au.com.gaiaresources.bdrs.controller.attribute.formfield.AbstractRecordFormField" />
	         	<c:if test="<%= formField.isPropertyFormField() %>">
	         		<c:if test="${ formField.scope == 'SURVEY'}">
						<tiles:insertDefinition name="formFieldRenderer">
						    <tiles:putAttribute name="formField" value="${formField}"/>
						    <tiles:putAttribute name="locations" value="${locations}"/>
							<tiles:putAttribute name="editEnabled" value="${ recordWebFormContext.editable }" />
						</tiles:insertDefinition>
					</c:if>
	         	</c:if>
	         	<c:if test="<%= formField.isAttributeFormField() %>">
	         		<c:if test="${ formField.attribute.scope == 'SURVEY' || formField.attribute.scope == 'SURVEY_MODERATION'}">
						<tiles:insertDefinition name="formFieldRenderer">
						    <tiles:putAttribute name="formField" value="${formField}"/>
						    <tiles:putAttribute name="locations" value="${locations}"/>
							<tiles:putAttribute name="editEnabled" value="${ recordWebFormContext.editable }" />
						</tiles:insertDefinition>
					</c:if>
	         	</c:if>
	        </c:forEach>
	    </tbody>
	</table>
	
	
	<div id="sightingsContainer">
	    <!-- Add sightings description text -->
	    <cw:getContent key="user/singleSiteMultiTaxaTable" />
		
		<c:if test="${ recordWebFormContext.editable }">
			<div id="add_sighting_panel" class="buttonpanel textright">
		        <a id="maximiseLink" class="text-left" href="javascript:bdrs.util.maximise('#maximiseLink', '#sightingsContainer', 'Enlarge Table', 'Shrink Table')">Enlarge Table</a>
			    <input type="hidden" id="sighting_index" name="sightingIndex" value="<%= recordFieldCollectionList.size() %>"/>  
			    <input class="form_action" type="button" value="Add Sighting" onclick="bdrs.contribute.singleSiteMultiTaxa.addSighting('#sighting_index', '[name=surveyId]', '#sightingTable tbody', false, false, ${showScientificName});"/>
			</div>
        </c:if>
		
		<table id="sightingTable" class="datatable">
		    <thead>
		       <tr>
		           <c:forEach items="${ sightingRowFormFieldList }" var="sightingRowFormField">
				           <jsp:useBean id="sightingRowFormField" type="au.com.gaiaresources.bdrs.controller.attribute.formfield.AbstractRecordFormField" />
		                   <c:if test="<%= sightingRowFormField.isPropertyFormField() %>">
								<c:choose>
									<c:when test="${ not sightingRowFormField.hidden }">
										<c:if test="${sightingRowFormField.scope == 'RECORD'}">
											<th>
												<c:out value="${ sightingRowFormField.description }" />
											</th>
										</c:if>
									</c:when>
								</c:choose>
							</c:if> 
							<c:if test="<%= sightingRowFormField.isAttributeFormField() %>">
								<c:if test="${ sightingRowFormField.attribute.scope == 'RECORD' || sightingRowFormField.attribute.scope == 'RECORD_MODERATION'}">
									<th>
									<c:out value="${ sightingRowFormField.attribute.description }" />
									</th>
								</c:if>
							</c:if>
						
		           </c:forEach>
		       </tr>
		    </thead>
		    <tbody>
		    	<%-- Insert existing records here. --%>
                
                <c:forEach items="${recordFieldCollectionList}" var="recordFormFieldCollection">
                    <tiles:insertDefinition name="singleSiteMultiTaxaRow">
                        <tiles:putAttribute name="recordFormFieldCollection" value="${recordFormFieldCollection}"/>
						<tiles:putAttribute name="editEnabled" value="${ recordWebFormContext.editable }" />
                    </tiles:insertDefinition>
                </c:forEach>
		    </tbody>
		</table>
	</div>
	
	</div>
	
<%-- the record form footer contains the 'form' close tag --%>
<tiles:insertDefinition name="recordFormFooter">
    <tiles:putAttribute name="recordWebFormContext" value="${recordWebFormContext}" />                    
</tiles:insertDefinition>

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
        bdrs.form.prepopulate();
    });
</script>