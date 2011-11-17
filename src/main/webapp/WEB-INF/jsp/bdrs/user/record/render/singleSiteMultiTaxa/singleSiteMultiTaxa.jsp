<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<jsp:useBean id="survey" scope="request" type="au.com.gaiaresources.bdrs.model.survey.Survey" />
<jsp:useBean id="recordFieldCollectionList" scope="request" type="java.util.List" />

<%@page import="au.com.gaiaresources.bdrs.model.taxa.Attribute"%>
<%@page import="au.com.gaiaresources.bdrs.model.taxa.AttributeScope"%>

<h1><c:out value="${survey.name}"/></h1>
<c:if test="${censusMethod != null}">
    <!-- using censusmethod description here in case we want to display no text / more indepth text -->
    <p><cw:validateHtml html="${censusMethod.description}"></cw:validateHtml></p>
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
             <jsp:useBean id="formField" type="au.com.gaiaresources.bdrs.controller.attribute.formfield.AbstractRecordFormField" />
	         	<c:if test="<%= formField.isPropertyFormField() %>">
	         		<c:if test="${ formField.scope == 'SURVEY'}">
						<tiles:insertDefinition name="formFieldRenderer">
						    <tiles:putAttribute name="formField" value="${formField}"/>
						    <tiles:putAttribute name="locations" value="${locations}"/>
						</tiles:insertDefinition>
					</c:if>
	         	</c:if>
	         	<c:if test="<%= formField.isAttributeFormField() %>">
	         		<c:if test="${ formField.attribute.scope == 'SURVEY'}">
						<tiles:insertDefinition name="formFieldRenderer">
						    <tiles:putAttribute name="formField" value="${formField}"/>
						    <tiles:putAttribute name="locations" value="${locations}"/>
						</tiles:insertDefinition>
					</c:if>
	         	</c:if>
            </c:forEach>
        </tbody>
    </table>
    
    
    <div id="sightingsContainer">
    	
    	<c:if test="${ not hideAddBtn }">
	        <!-- Add sightings description text -->
	        <cw:getContent key="user/singleSiteMultiTaxaTable" />
        <div id="add_sighting_panel" class="buttonpanel textright">
            <a id="maximiseLink" class="text-left" href="javascript:bdrs.util.maximise('#maximiseLink', '#sightingsContainer', 'Enlarge Table', 'Shrink Table')">Enlarge Table</a>
            <input type="hidden" id="sighting_index" name="sightingIndex" value="<%= recordFieldCollectionList.size() %>"/>  
            <input class="form_action" type="button" value="Add Sighting" onclick="bdrs.contribute.singleSiteMultiTaxa.addSighting('#sighting_index', '[name=surveyId]', '#sightingTable tbody', false, false);"/>
	    </div>
		
        </c:if>
        <table id="sightingTable" class="datatable">
            <thead>
               <tr>
                   <c:forEach items="${ sightingRowFormFieldList }" var="sightingRowFormField">
                   				
	                        <jsp:useBean id="sightingRowFormField" type="au.com.gaiaresources.bdrs.controller.attribute.formfield.AbstractRecordFormField" />
							<c:if test="<%= sightingRowFormField.isPropertyFormField() %>">
								<c:if test="${ sightingRowFormField.scope == 'RECORD' }">
									<c:choose>
										<c:when test="${ not sightingRowFormField.hidden }">
										 <th>
											<c:out value="${ sightingRowFormField.description }" />
										</th>
										</c:when>
									</c:choose>
								</c:if>
							</c:if>
							<c:if test="<%= sightingRowFormField.isAttributeFormField() %>">
								<c:if test="${ sightingRowFormField.attribute.scope == 'RECORD' }"> 
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
                    </tiles:insertDefinition>
				</c:forEach>
            </tbody>
        </table>
    </div>
    
    </div>
    <c:choose>
        <c:when test="${ preview }">
            <div class="buttonpanel textright">
                <input class="form_action" type="button" value="Go Back" onclick="window.document.location='${pageContext.request.contextPath}/bdrs/admin/survey/editAttributes.htm?surveyId=${survey.id}'"/>
                <input class="form_action" type="button" value="Continue" onclick="window.document.location='${pageContext.request.contextPath}/bdrs/admin/survey/locationListing.htm?surveyId=${survey.id}'"/>
            </div>
        </c:when>
        <c:otherwise>
                <div class="buttonpanel textright">
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
        bdrs.form.prepopulate();
		bdrs.contribute.singleSiteMultiTaxa.init('#sighting_index', '[name=surveyId]', false, false);
    });
</script>