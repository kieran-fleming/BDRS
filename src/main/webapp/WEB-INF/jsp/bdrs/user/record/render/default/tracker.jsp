<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<%@page import="au.com.gaiaresources.bdrs.model.record.Record"%>
<jsp:useBean id="record" scope="request" type="au.com.gaiaresources.bdrs.model.record.Record" />
<jsp:useBean id="survey" scope="request" type="au.com.gaiaresources.bdrs.model.survey.Survey" />

<h1><c:out value="${survey.name}"/></h1>
<p>
    Click on the map to enter the location of the sighting.
</p>


<div id="record_base_map_hover_tip">&nbsp;</div>
<div class="map_wrapper" id="map_wrapper">
    <div id="base_map" class="defaultmap tracker_map"></div>
</div>

<c:if test="${ not preview }">
    <form method="POST" action="${pageContext.request.contextPath}/bdrs/user/tracker.htm" enctype="multipart/form-data">
</c:if>
    <input type="hidden" name="surveyId" value="${survey.id}"/>
    <c:if test="${record != null}">
        <input type="hidden" name="recordId" value="${record.id}"/>
    </c:if>
    
    <table class="form_table">
        <tbody>
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
        </tbody>
    </table>
    
<c:choose>
    <c:when test="${ preview }">
        <div class="textright">
            <input class="form_action" type="button" value="Go Back" onclick="window.document.location='${pageContext.request.contextPath}/bdrs/admin/survey/editAttributes.htm?surveyId=${survey.id}'"/>
            <input class="form_action" type="button" value="Continue" onclick="window.document.location='${pageContext.request.contextPath}/bdrs/admin/survey/editLocations.htm?surveyId=${survey.id}'"/>
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
    bdrs.survey.location.LAYER_NAME = 'Position Layer';

    bdrs.survey.location.updateLocation = function(pk) {
        if(pk > 0) {
            jQuery.get("${pageContext.request.contextPath}/webservice/location/getLocationById.htm", {id: pk}, function(data) {
                var wkt = new OpenLayers.Format.WKT();
                var feature = wkt.read(data.location);

                var lat = jQuery('input[name=latitude]').val(feature.geometry.y).blur();
                var lon = jQuery('input[name=longitude]').val(feature.geometry.x).blur();

                var layer = bdrs.map.baseMap.getLayersByName(bdrs.survey.location.LAYER_NAME)[0];
                layer.removeFeatures(layer.features);

                var lonLat = new OpenLayers.LonLat(
                    feature.geometry.x, feature.geometry.y);
                lonLat = lonLat.transform(bdrs.map.WGS84_PROJECTION,
                                          bdrs.map.GOOGLE_PROJECTION);
                layer.addFeatures(new OpenLayers.Feature.Vector(
                    new OpenLayers.Geometry.Point(lonLat.lon, lonLat.lat)));
            });
        }
        else {
            jQuery('input[name=latitude]').val("").blur();
            jQuery('input[name=longitude]').val("").blur();
        }
    }

    jQuery(function() {
        var layerName = bdrs.survey.location.LAYER_NAME;
        bdrs.map.initBaseMap('base_map');
        
        <c:choose>
            <c:when test="<%= survey.isPredefinedLocationsOnly() %>">
                var layer = bdrs.map.addPositionLayer(layerName);                          
            </c:when>
            <c:otherwise>
                var layer = bdrs.map.addSingleClickPositionLayer(layerName, 'input[name=latitude]', 'input[name=longitude]');
            </c:otherwise>
        </c:choose>

        var lat = jQuery('input[name=latitude]');
        var lon = jQuery('input[name=longitude]');
        if(lat.val().length > 0 && lon.val().length > 0) {
            var lonLat = new OpenLayers.LonLat(
                    parseFloat(lon.val()), parseFloat(lat.val()));
            lonLat = lonLat.transform(bdrs.map.WGS84_PROJECTION,
                                      bdrs.map.GOOGLE_PROJECTION);
            var feature = new OpenLayers.Feature.Vector(
                new OpenLayers.Geometry.Point(lonLat.lon, lonLat.lat));
            layer.addFeatures(feature);
            bdrs.map.baseMap.setCenter(lonLat);
        }

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
                jQuery("[name=species]").val(taxon.id);
                
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
            minLength: 2,
            delay: 300,
            html: true
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
</script>