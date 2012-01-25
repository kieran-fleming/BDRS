<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>


<%@page import="au.com.gaiaresources.bdrs.model.record.Record"%>
<jsp:useBean id="record" scope="request" type="au.com.gaiaresources.bdrs.model.record.Record" />
<jsp:useBean id="survey" scope="request" type="au.com.gaiaresources.bdrs.model.survey.Survey" />

<%-- Access the facade to retrieve the preference information --%>
<jsp:useBean id="bdrsPluginFacade" scope="request" type="au.com.gaiaresources.bdrs.servlet.BdrsPluginFacade"></jsp:useBean>
<c:set var="showScientificName" value="<%= bdrsPluginFacade.getPreferenceBooleanValue(\"taxon.showScientificName\") %>" />

<div>
    <tiles:insertDefinition name="recordEntryMap">
        <tiles:putAttribute name="survey" value="${survey}"/>
        <tiles:putAttribute name="censusMethod" value="${censusMethod}"/>
        <tiles:putAttribute name="mapEditable" value="${recordWebFormContext.editable}" />
    </tiles:insertDefinition>
    
    <c:if test="${ not preview and recordWebFormContext.editable}">
        <form method="POST" action="${pageContext.request.contextPath}/bdrs/user/tracker.htm" enctype="multipart/form-data">
    </c:if>
        <input type="hidden" name="parentRecordId" value="${parentRecordId}"/>
        <input type="hidden" name="surveyId" value="${survey.id}"/>
        <c:if test="${censusMethod != null}">
        <input type="hidden" name="censusMethodId" value="${censusMethod.id}"/>
        </c:if>
        <c:if test="${record != null}">
            <input type="hidden" name="recordId" value="${record.id}"/>
        </c:if>
        
        <p class="error textcenter" id="wktMessage">
        </p>
        
        <%-- only include the wkt input if we are drawing lines or polygons 
           if the wkt key/value pair exists in the post dictionary, the lat/lon
           fields will be ignored as the wkt entry takes precedence.
        --%>
        <c:if test="<%= !survey.isPredefinedLocationsOnly() %>" >
           <c:if test="${censusMethod != null and (censusMethod.drawLineEnabled or censusMethod.drawPolygonEnabled)}">
             <input type="hidden" name="wkt" value="${wkt}" />
           </c:if>
        </c:if>
        
        <c:if test="${!survey.recordVisibilityModifiable}">
            <input type="hidden" name="recordVisibility" value="${survey.defaultRecordVisibility}" />
        </c:if>
        
        <table class="form_table tracker_form_table">
            <tbody>
         
                <c:if test="${recordWebFormContext.editable and survey.recordVisibilityModifiable}">
                    <tr>
                        <th title="Who will be able to view your record. \nOwner only means only you will be able to see the record. \nControlled will allow others to see that when and where you have contributed but no additional data will be supplied. \nFull public will show all details of the record to all other users.">Record Visibility</th>
                        <td>
                            <select name="recordVisibility">
                                <c:forEach items="<%=au.com.gaiaresources.bdrs.model.record.RecordVisibility.values()%>" var="recVis">
                                <jsp:useBean id="recVis" type="au.com.gaiaresources.bdrs.model.record.RecordVisibility" />
                                    <option value="<%= recVis.toString() %>"
                                    <c:if test="<%= recVis.equals(record.getRecordVisibility()) %>">selected="selected"</c:if>
                                    >
                                    <%= recVis.getDescription() %></option>
                                </c:forEach>
                            </select>
                        </td>
                    </tr>
                </c:if>
    
                <c:forEach items="${surveyFormFieldList}" var="formField">
                    <tiles:insertDefinition name="formFieldRenderer">
                        <tiles:putAttribute name="formField" value="${ formField }"/>
                        <tiles:putAttribute name="locations" value="${ locations }"/>
                        <tiles:putAttribute name="errorMap" value="${ errorMap }"/>
                        <tiles:putAttribute name="valueMap" value="${ valueMap }"/>
                        <tiles:putAttribute name="editEnabled" value="${ recordWebFormContext.editable }"/>
                    </tiles:insertDefinition>
                </c:forEach>
                
                <c:forEach items="${taxonGroupFormFieldList}" var="formField">
                    <tiles:insertDefinition name="formFieldRenderer">
                        <tiles:putAttribute name="formField" value="${ formField }"/>
                        <tiles:putAttribute name="locations" value="${ locations }"/>
                        <tiles:putAttribute name="errorMap" value="${ errorMap }"/>
                        <tiles:putAttribute name="valueMap" value="${ valueMap }"/>
                        <tiles:putAttribute name="editEnabled" value="${ recordWebFormContext.editable }"/>
                    </tiles:insertDefinition>
                </c:forEach>
                
                <c:forEach items="${censusMethodFormFieldList}" var="formField">
                    <tiles:insertDefinition name="formFieldRenderer">
                        <tiles:putAttribute name="formField" value="${ formField }"/>
                        <tiles:putAttribute name="locations" value="${ locations }"/>
                        <tiles:putAttribute name="errorMap" value="${ errorMap }"/>
                        <tiles:putAttribute name="valueMap" value="${ valueMap }"/>
                        <tiles:putAttribute name="editEnabled" value="${ recordWebFormContext.editable }"/>
                    </tiles:insertDefinition>
                </c:forEach>
            </tbody>
        </table>
    
	<input id="submitAndSwitchToSubRecordTab" name="submitAndSwitchToSubRecordTab" value="true" type="submit" class="hidden" />
	
    <%-- the record form footer contains the 'form' close tag --%>
    <tiles:insertDefinition name="recordFormFooter">
        <tiles:putAttribute name="recordWebFormContext" value="${recordWebFormContext}" />
    </tiles:insertDefinition>
</div>

<script type="text/javascript">
    jQuery(window).load(function() {
        // Species Autocomplete
        var recordIdElem = jQuery("[name=recordId]");
        var speciesAutocompleteArgs = {
            surveySpeciesSearchSelector: "#survey_species_search",
            speciesIdSelector: "[name=species]",
            taxonAttrRowSelector:"[name^=taxonGroupAttr_]",
            surveyId: jQuery("[name=surveyId]").val(),
            recordId: (recordIdElem.length > 0 && recordIdElem.val().length > 0) ? recordIdElem.val() : undefined,
            editable: "${recordWebFormContext.editable}",
            attributeTbodySelector: ".form_table tbody", 
            showScientificName: ${showScientificName}
        };
        bdrs.contribute.initSpeciesAutocomplete(speciesAutocompleteArgs);
        
        
        jQuery("#number").change(function(data) {
            jQuery("#survey_species_search").trigger("blur");
        });
            
        jQuery(".acomplete").autocomplete({
            source: function(request, callback) {
                var params = {};
                params.ident = bdrs.ident;
                params.q = request.term;
                var bits = this.element[0].id.split('_');
                params.attribute = bits[bits.length-1];
                

                jQuery.getJSON('${pageContext.request.contextPath}/webservice/attribute/searchValues.htm', params, function(data, textStatus) {
                    callback(data);
                });
            },
            minLength: 2,
            delay: 300
        });
    });

    /**
     * Prepopulate fields
     */
     jQuery(function(){
         bdrs.form.prepopulate();
     });
</script>