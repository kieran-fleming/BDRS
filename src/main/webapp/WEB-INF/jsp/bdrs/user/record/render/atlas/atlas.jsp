<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<%@page import="au.com.gaiaresources.bdrs.model.record.Record"%>
<%@page import="au.com.gaiaresources.bdrs.model.taxa.TaxonRank"%>
<%@page import="au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordPropertyFormField"%>


<%@page import="au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordPropertyType"%>
<jsp:useBean id="record" scope="request" type="au.com.gaiaresources.bdrs.model.record.Record" />
<jsp:useBean id="taxon" scope="request" type="au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies" />
<jsp:useBean id="survey" scope="request" type="au.com.gaiaresources.bdrs.model.survey.Survey" />
<jsp:useBean id="formFieldMap" scope="request" type="java.util.HashMap" />

<span class="atlasSighting">
	<div class="sepBottom">
	    <h1>
	        <div class="left">Share a Sighting</div>
	        <!-- a class="right" href="${pageContext.request.contextPath}/fieldguide/taxon.htm?id=${ taxon.id }">Species Page</a-->
	        <div class="clear"></div>
	    </h1>
	</div>
	
	
	<c:if test="${ not preview }">
    <form method="POST" action="${pageContext.request.contextPath}/bdrs/user/tracker.htm" enctype="multipart/form-data">
        <input type="hidden" name="surveyId" value="${survey.id}"/>
        <input type="hidden" id="redirecturl" name="redirecturl" value="/review/sightings/advancedReview.htm"/>
        <c:if test="${record != null}">
            <input type="hidden" id="recordId" name="recordId" value="${record.id}"/>
        </c:if>
        
        <span style="display:none">
            <c:set var="formField" value="<%= formFieldMap.get(RecordPropertyType.SPECIES) %>"/>
            <tiles:insertDefinition name="formFieldRenderer">
                <tiles:putAttribute name="formField" value="${ formField }"/>
                <tiles:putAttribute name="errorMap" value="${ errorMap }"/>
                <tiles:putAttribute name="valueMap" value="${ valueMap }"/>
            </tiles:insertDefinition>
        </span>
    </c:if>
	<div class="leftCol left">
		<h3 class="left">
		    <span class="scientificName">
			    <c:out value="${ taxon.scientificName }"/>
		    </span>
			&nbsp;:&nbsp;
			<c:out value="${ taxon.commonName }"/>
		</h3>
			<c:forEach items="${ taxon.infoItems }" var="profile">
			    <jsp:useBean id="profile" type="au.com.gaiaresources.bdrs.model.taxa.SpeciesProfile"/>
			    <c:if test="${ not empty profile.content }">
			        <c:choose>
			            <c:when test="<%= profile.getType().equals(profile.SPECIES_PROFILE_THUMBNAIL) %>">
                                <a class="left" href="http://bie.ala.org.au/species/${ taxon.scientificName }">
				                <!-- a class="left" href="${pageContext.request.contextPath}/files/downloadByUUID.htm?uuid=${ profile.content }"-->
				                    <img class="thumb" src="${pageContext.request.contextPath}/files/downloadByUUID.htm?uuid=${ profile.content }"/>
				                </a>
			                
			            </c:when>
			            <c:otherwise>
			            </c:otherwise>
			        </c:choose>
			    </c:if>
			</c:forEach>
		    

        <table class="form_table">
            <tbody>
				<c:set var="formField" value="<%= formFieldMap.get(RecordPropertyType.WHEN) %>"/>
				<tiles:insertDefinition name="formFieldRenderer">
				    <tiles:putAttribute name="formField" value="${ formField }"/>
				    <tiles:putAttribute name="errorMap" value="${ errorMap }"/>
				    <tiles:putAttribute name="valueMap" value="${ valueMap }"/>
				</tiles:insertDefinition>
				
				<c:set var="formField" value="<%= formFieldMap.get(RecordPropertyType.TIME) %>"/>
				<tiles:insertDefinition name="formFieldRenderer">
				    <tiles:putAttribute name="formField" value="${ formField }"/>
				    <tiles:putAttribute name="errorMap" value="${ errorMap }"/>
				    <tiles:putAttribute name="valueMap" value="${ valueMap }"/>
				</tiles:insertDefinition>
				
                <c:set var="formField" value="<%= formFieldMap.get(RecordPropertyType.NUMBER) %>"/>
                <tiles:insertDefinition name="formFieldRenderer">
                    <tiles:putAttribute name="formField" value="${ formField }"/>
                    <tiles:putAttribute name="errorMap" value="${ errorMap }"/>
                    <tiles:putAttribute name="valueMap" value="${ valueMap }"/>
                </tiles:insertDefinition>
                
	            <tr>
	                <th>
	                    <label for="locationName">Location</label>
                    </th>
                    <td>
                        <textarea name="locationName" id="locationName" class="locationTextArea"><c:if test="${ record != null && record.location != null }">${ record.location.name }</c:if></textarea>
                        &nbsp;
                        <!-- 
                        <a href="javascript: bdrs.survey.location.updateLocation(-1)">Clear</a>
                        -->
                    </td>
	            </tr>
	            
                <c:set var="formField" value="<%= formFieldMap.get(RecordPropertyType.POINT) %>"/>
                <tiles:insertDefinition name="formFieldRenderer">
                    <tiles:putAttribute name="formField" value="${ formField }"/>
                    <tiles:putAttribute name="errorMap" value="${ errorMap }"/>
                    <tiles:putAttribute name="valueMap" value="${ valueMap }"/>
                </tiles:insertDefinition>

                <c:set var="formField" value="<%= formFieldMap.get(RecordPropertyType.ACCURACY) %>"/>
                <tiles:insertDefinition name="formFieldRenderer">
                    <tiles:putAttribute name="formField" value="${ formField }"/>
                    <tiles:putAttribute name="label" value="Coordinate ncertainty"/>
                    <tiles:putAttribute name="errorMap" value="${ errorMap }"/>
                    <tiles:putAttribute name="valueMap" value="${ valueMap }"/>
                </tiles:insertDefinition>

                <c:set var="formField" value="<%= formFieldMap.get(RecordPropertyType.NOTES) %>"/>
		        <tiles:insertDefinition name="formFieldRenderer">
		            <tiles:putAttribute name="formField" value="${ formField }"/>
		            <tiles:putAttribute name="sublabel" value="(weather conditions, observed behaviour, etc.)"/>
		            <tiles:putAttribute name="errorMap" value="${ errorMap }"/>
		            <tiles:putAttribute name="valueMap" value="${ valueMap }"/>
		        </tiles:insertDefinition>
		        
		        <tiles:insertDefinition name="formFieldRenderer">
		            <tiles:putAttribute name="formField" value="${ fileFormField }"/>
		            <tiles:putAttribute name="errorMap" value="${ errorMap }"/>
		            <tiles:putAttribute name="valueMap" value="${ valueMap }"/>
		        </tiles:insertDefinition>

			</tbody>
		</table>

        <div class="clear"></div>

		<div class="buttonpanel">
		    <c:choose>
		        <c:when test="${ preview }">
		            <div class="buttonpanel textright">
		                <input class="form_action" type="button" value="Go Back" onclick="window.document.location='${pageContext.request.contextPath}/bdrs/admin/survey/editAttributes.htm?surveyId=${survey.id}'"/>
		                <input class="form_action" type="button" value="Continue" onclick="window.document.location='${pageContext.request.contextPath}/bdrs/admin/survey/locationListing.htm?surveyId=${survey.id}'"/>
		            </div>
		        </c:when>
		        <c:otherwise>
		                <div class="buttonpanel textright">
		                    <!-- only show the delete button if it is an existing record -->
		                    <c:if test="${record.id != null} & false">
                                <input class="form_action" type="button" name="submitDelete" value="Delete Record" onclick="bdrs.survey.deleteRecord()" />
                            </c:if>
                            <input class="form_action" type="submit" name="submit" value="Save Changes"/>
		                </div>
		        </c:otherwise>
		    </c:choose>
	    </div>
	</div>
	
	<div class="rightCol right">
	   <label for="locationName">Bookmarked locations</label>
	   <div class="atlasSightingRecordLocations">
            <c:set var="formField" value="<%= formFieldMap.get(RecordPropertyType.LOCATION) %>"/>
            <tiles:insertDefinition name="propertyRenderer">
               <tiles:putAttribute name="formField" value="${ formField }"/>
               <tiles:putAttribute name="locations" value="${ locations }"/>
            </tiles:insertDefinition>
        </div>
        <div id="geocode" class="geocode atlasgeocode"></div>
        <div class="buttonpanel">
            <input id="bookmarkLocation" class="form_action" type="button" value="Bookmark location"/>
               <input type="checkbox" name="defaultLocation" id="defaultLocation"/>
               <label for="defaultLocation">Set as default</label>
        </div>
        <div class="map_wrapper" id="map_wrapper">
            <div id="base_map" class="defaultmap atlasmap left"></div>
        </div>
        <p class="mapHints clear">
            <span class="boldtext">Hints:</span> click and drag the marker 
            to fine-tune the location coordinates.
        </p>
	</div>
	
	<div class="clear"></div>
	
    <c:if test="${ not preview }">
        </form>
    </c:if>

</span>

<script type="text/javascript">
    bdrs.survey.location.LAYER_NAME = 'Position Layer';
    bdrs.survey.location.LOCATION_LAYER_NAME = 'Location Layer';
    <c:choose>
        <c:when test="${ defaultLocation == null }">
            bdrs.survey.location.DEFAULT_LOCATION_ID = null;
        </c:when>
        <c:otherwise>
            bdrs.survey.location.DEFAULT_LOCATION_ID = ${ defaultLocation.id };
        </c:otherwise>
    </c:choose> 

    bdrs.survey.location.updateLocation = function(pk) {
        if(pk > 0) {
        	jQuery.get("${pageContext.request.contextPath}/webservice/location/getLocationById.htm", {id: pk}, function(data) {
                var wkt = new OpenLayers.Format.WKT(bdrs.map.wkt_options);
	            var feature = wkt.read(data.location);
	            var point = feature.geometry.getCentroid().transform(
	                    bdrs.map.GOOGLE_PROJECTION,
	                    bdrs.map.WGS84_PROJECTION);
	            var lat = jQuery('input[name=latitude]').val(point.y).blur();
	            var lon = jQuery('input[name=longitude]').val(point.x).blur();
	
	            // add the location point to the map
	            var layer = bdrs.map.baseMap.getLayersByName(bdrs.survey.location.LAYER_NAME)[0];
	            layer.removeAllFeatures();
	
	            var lonLat = new OpenLayers.LonLat(point.x, point.y);
	            lonLat = lonLat.transform(bdrs.map.WGS84_PROJECTION,
	                                      bdrs.map.GOOGLE_PROJECTION);
	            layer.addFeatures(new OpenLayers.Feature.Vector(
	                new OpenLayers.Geometry.Point(lonLat.lon, lonLat.lat)));
	
	            // add the location geometry to the map
	            var loclayer = bdrs.map.baseMap.getLayersByName(bdrs.survey.location.LOCATION_LAYER_NAME)[0];
		        if (loclayer) {
			        loclayer.removeAllFeatures();
	            	loclayer.addFeatures(feature);
		        }
		        
	            // zoom the map to show the currently selected location
	            var geobounds = feature.geometry.getBounds();
	            var zoom = bdrs.map.baseMap.getZoomForExtent(geobounds);
	            bdrs.map.baseMap.setCenter(geobounds.getCenterLonLat(), zoom);
                jQuery("#locationName").val(data.name);
                jQuery("#bookmarkLocation").attr("disabled", "disabled");
                
                if( bdrs.survey.location.DEFAULT_LOCATION_ID !== null && 
                    bdrs.survey.location.DEFAULT_LOCATION_ID === parseInt(pk,10)) {
                    jQuery("#defaultLocation").attr("checked", "checked");
                } else {
	                jQuery("#defaultLocation").removeAttr("checked", "checked");
	            }
            });
        }
        else {
            var layer = bdrs.map.baseMap.getLayersByName(bdrs.survey.location.LAYER_NAME)[0];
            layer.removeAllFeatures();
        
            jQuery('input[name=latitude]').val("").blur();
            jQuery('input[name=longitude]').val("").blur();
            jQuery("#locationName").val("");
            jQuery("#bookmarkLocation").attr("disabled", "disabled");
        }
    };

    jQuery(function() {
        // Create the center point of the map if we have one.
        var lat = jQuery('input[name=latitude]');
        var lon = jQuery('input[name=longitude]');


        var latLonChangeHandler = function() {
            var callback = function(name) {
                jQuery("#locationName").val(name);
                jQuery("#location").val(-1);
            };
        
            bdrs.map.latLonToName(  jQuery('input[name=latitude]').val(), 
                                    jQuery('input[name=longitude]').val(), 
                                    callback);
            jQuery("#bookmarkLocation").removeAttr("disabled");
        };
        lat.change(latLonChangeHandler);
        lon.change(latLonChangeHandler);
        
        // Bookmark Location
        var bookmarkText = "-- select bookmarked location --";
        if (jQuery("#location option").size() == 1) {
            bookmarkText = "-- no bookmarked locations --";
            jQuery("#location").attr("disabled", "disabled");
        }
        jQuery("#location option[value=-1]").text(bookmarkText);
        var bookmarkLocation = jQuery("#bookmarkLocation");
        bookmarkLocation.click(function(event) {
            event.preventDefault();
            
            var locationName = jQuery("#locationName").val();
            var lat = parseFloat(jQuery('input[name=latitude]').val());
            var lon = parseFloat(jQuery('input[name=longitude]').val());
            var isDefault = jQuery("#defaultLocation:checked").length > 0;
            
            bdrs.user.bookmarkUserLocation(locationName, lat, lon, isDefault, function(loc) {
                var opt = jQuery("<option></option>");
                opt.text(loc.name);
                opt.attr({value: loc.id});
                jQuery("#location").append(opt).val(loc.id);
                // when a bookmark is added, remove the disabled attribute
                // and set the text at -1 option to "-- select bookmarked location --"
                jQuery("#location").removeAttr("disabled");
                jQuery("#location option[value=-1]").text("-- select bookmarked location --");
            });
        });
        
        var jLoc = jQuery("#location");
        // Set the initial state of the bookmarks button
        if((jLoc.val() > 0) || (lat.val().length === 0 && lon.val().length === 0)) {
            // Location already bookmarked or there is no lat and lon
            bookmarkLocation.attr("disabled", "disabled");
        } else {
            bookmarkLocation.removeAttr("disabled");
        }

        // Set the default location if no location has been set.        
        if( bdrs.survey.location.DEFAULT_LOCATION_ID !== null && 
            parseInt(jLoc.val(),10) === -1 &&
            lat.val().length === 0 && 
            lon.val().length === 0 &&
            jLoc.find('option[value='+bdrs.survey.location.DEFAULT_LOCATION_ID+']').length > 0) {
            
            jLoc.val(bdrs.survey.location.DEFAULT_LOCATION_ID).trigger("change");
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
    
    
    bdrs.survey.deleteRecord = function() {
        if(confirm('Are you sure you want to delete this record?')) {
            var recordId = jQuery('#recordId').val();
            var redirecturl = jQuery('#redirecturl').val();
            
            var url = bdrs.contextPath+"/bdrs/user/deleteRecord.htm";
            var param = {
                recordId: recordId,
                redirecturl: redirecturl
            };
            bdrs.postWith(url, param);
        }
    };
    
    /**
    * Prepopulate fields
    */
    jQuery(function(){
        bdrs.form.prepopulate();
    });
    
    /**
     *  OpenLayers must be done after window load on IE.
     */
    jQuery(window).load(function() {
    	var lat = jQuery('input[name=latitude]');
        var lon = jQuery('input[name=longitude]');
    
        var olLonLat = null;
        if(lat.val().length > 0 && lon.val().length > 0) {
            olLonLat = new OpenLayers.LonLat(
                    parseFloat(lon.val()), parseFloat(lat.val()));
        }

        var geocodeClickHandler = function(lonLat) {
            lonLat.transform(bdrs.map.GOOGLE_PROJECTION, bdrs.map.WGS84_PROJECTION);
            lat.val(bdrs.map.roundLatOrLon(lonLat.lat)).trigger("change");
            lon.val(bdrs.map.roundLatOrLon(lonLat.lon)).trigger("change");
        }
        
        var layerName = bdrs.survey.location.LAYER_NAME;
        var mapOptions = {
            mapCenter: olLonLat,
            mapZoom: -1,
            geocode : {
                selector: '#geocode',
                zoom: -1,
                useKeyHandler: false,
                clickHandler: geocodeClickHandler
            }
        };
        bdrs.map.initBaseMap('base_map', mapOptions);
//        bdrs.map.addLocationLayer(bdrs.map.baseMap, bdrs.survey.location.LOCATION_LAYER_NAME);
        bdrs.map.centerMap(bdrs.map.baseMap, olLonLat, -1);
        
        <c:choose>
            <c:when test="<%= survey.isPredefinedLocationsOnly() %>">
                var layer = bdrs.map.addPositionLayer(layerName);
            </c:when>
            <c:otherwise>
                var layer = bdrs.map.addSingleClickPositionLayer(bdrs.map.baseMap, layerName, 'input[name=latitude]', 'input[name=longitude]');
				bdrs.map.addLonLatChangeHandler(layer, 'input[name=longitude]', 'input[name=latitude]');
            </c:otherwise>
        </c:choose>
        
        // If there is an intial point, add the feature.
        if(olLonLat !== undefined && olLonLat !== null) {
            olLonLat = olLonLat.transform(bdrs.map.WGS84_PROJECTION, bdrs.map.GOOGLE_PROJECTION);
            var feature = new OpenLayers.Feature.Vector(
                new OpenLayers.Geometry.Point(olLonLat.lon, olLonLat.lat));
            layer.addFeatures(feature);
            // zoom to feature extent
            var geobounds = feature.geometry.getBounds();
            var zoom = bdrs.map.baseMap.getZoomForExtent(geobounds);
            bdrs.map.baseMap.setCenter(geobounds.getCenterLonLat(), 10);
        }
    });
</script>





