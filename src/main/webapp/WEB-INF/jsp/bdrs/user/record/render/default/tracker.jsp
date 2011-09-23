<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<%@page import="au.com.gaiaresources.bdrs.model.record.Record"%>
<jsp:useBean id="record" scope="request" type="au.com.gaiaresources.bdrs.model.record.Record" />
<jsp:useBean id="survey" scope="request" type="au.com.gaiaresources.bdrs.model.survey.Survey" />

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
    <form method="POST" action="${pageContext.request.contextPath}/bdrs/user/tracker.htm" enctype="multipart/form-data">
</c:if>
    <input type="hidden" name="surveyId" value="${survey.id}"/>
    <c:if test="${censusMethod != null}">
    <input type="hidden" name="censusMethodId" value="${censusMethod.id}"/>
    </c:if>
    <c:if test="${record != null}">
        <input type="hidden" name="recordId" value="${record.id}"/>
    </c:if>
    
    <p class="error textcenter" id="wktMessage">
    </p>
    <input type="hidden" name="wkt" value="${wkt}" />
    
    <c:if test="${!survey.recordVisibilityModifiable}">
        <input type="hidden" name="recordVisibility" value="${survey.defaultRecordVisibility}" />
    </c:if>
    
    <table class="form_table">
        <tbody>
     
            <c:if test="${survey.recordVisibilityModifiable}">
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
                </tiles:insertDefinition>
            </c:forEach>
            
            <c:forEach items="${taxonGroupFormFieldList}" var="formField">
                <tiles:insertDefinition name="formFieldRenderer">
                    <tiles:putAttribute name="formField" value="${ formField }"/>
                    <tiles:putAttribute name="locations" value="${ locations }"/>
                    <tiles:putAttribute name="errorMap" value="${ errorMap }"/>
                    <tiles:putAttribute name="valueMap" value="${ valueMap }"/>
                </tiles:insertDefinition>
            </c:forEach>
            
            <c:forEach items="${censusMethodFormFieldList}" var="formField">
                <tiles:insertDefinition name="formFieldRenderer">
                    <tiles:putAttribute name="formField" value="${ formField }"/>
                    <tiles:putAttribute name="locations" value="${ locations }"/>
                    <tiles:putAttribute name="errorMap" value="${ errorMap }"/>
                    <tiles:putAttribute name="valueMap" value="${ valueMap }"/>
                </tiles:insertDefinition>
            </c:forEach>
        </tbody>
    </table>
    
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
                <input class="form_action" type="submit" name="submit" value="Submit Sighting"/>
            </div>
        </form>
    </c:otherwise>
</c:choose>

<script type="text/javascript">
    jQuery(window).load(function() {
        // Species Autocomplete
        jQuery("#survey_species_search").autocomplete({
            source: function(request, callback) {
                var params = {};
                params.q = request.term;
                params.surveyId = ${survey.id};

                jQuery.getJSON('${pageContext.request.contextPath}/webservice/survey/speciesForSurvey.htm', params, function(data, textStatus) {
                    var label;
                    var result;
                    var taxon;
                    var resultsArray = [];
                    for(var i=0; i<data.length; i++) {
                        taxon = data[i];

                        label = [];
                        if(taxon.scientificName !== undefined && taxon.scientificName.length > 0) {
                            label.push("<b><i>"+taxon.scientificName+"</b></i>");
                        }
                        if(taxon.commonName !== undefined && taxon.commonName.length > 0) {
                            label.push(taxon.commonName);
                        }

                        label = label.join(' ');

                        resultsArray.push({
                            label: label,
                            value: taxon.scientificName,
                            data: taxon
                        });
                    }

                    callback(resultsArray);
                });
            },
            select: function(event, ui) {
                var taxon = ui.item.data;
                jQuery("[name=species]").val(taxon.id).trigger("blur");
                
                // Load Taxon Group Attributes
                // Clear the group attribute rows
                jQuery("[name^=taxonGroupAttr_]").parents("tr").remove();
                
                // Build GET request parameters
                var params = {};
                params.surveyId = jQuery("[name=surveyId]").val();
                params.taxonId = taxon.id;
                var recordIdElem = jQuery("[name=recordId]");
                if(recordIdElem.length > 0 && recordIdElem.val().length > 0) {
                    params.recordId = recordIdElem.val();
                }
                // Issue Request
                jQuery.get("${pageContext.request.contextPath}/bdrs/user/ajaxTrackerTaxonAttributeTable.htm", params, function(data) {
                    jQuery(".form_table").find("tbody").append(data);
                });
            },
            change: function(event, ui) {
                if(jQuery(event.target).val().length === 0) {
                    jQuery("[name=species]").val("").trigger("blur");
                
                    // Clear the group attribute rows
                    jQuery("[name^=taxonGroupAttr_]").parents("tr").remove();
                }
            },
            minLength: 2,
            delay: 300,
            html: true
        });
        
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