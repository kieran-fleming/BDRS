<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>
<jsp:useBean id="context" scope="request" type="au.com.gaiaresources.bdrs.servlet.RequestContext"></jsp:useBean>

<%@page import="au.com.gaiaresources.bdrs.model.survey.Survey"%>

<tiles:useAttribute name="survey" classname="au.com.gaiaresources.bdrs.model.survey.Survey" ignore="true"/>
<tiles:useAttribute name="censusMethod" ignore="true"/>
<tiles:useAttribute name="mapEditable" ignore="true" />

<c:if test="${mapEditable == null}">
	<c:set var="mapEditable" value="true" />
</c:if>

<cw:getContent key="user/recordSightingMapDescription" />

<div class="clear"></div>

<div class="map_wrapper" id="map_wrapper">
    <div id="base_map" class="defaultmap tracker_map review_map"></div>
    <div id="geocode" class="geocode"></div>
</div>

<div class="clear"></div>

<script type="text/javascript">
    //make some page scope constants
    var entryForm = {
        latSelector: "input[name=latitude]",
        longSelector: "input[name=longitude]",
        wktSelector: "input[name=wkt]",
        locSelector: "select[name=location]"
    };
    
    var toolActivatedHandler = function(toolId) {
        if (toolId == bdrs.map.control.DRAW_POINT) {
            jQuery(entryForm.longSelector).removeAttr('disabled');
            jQuery(entryForm.latSelector).removeAttr('disabled');
        } else {
            jQuery(entryForm.longSelector).attr('disabled', 'disabled');
            jQuery(entryForm.latSelector).attr('disabled', 'disabled');
        }
    };
    
    jQuery(window).load(function() {
        bdrs.map.initWktOnChangeValidation('input[name=wkt]', '#wktMessage');
        
        var layerName = bdrs.survey.location.LAYER_NAME;
        bdrs.map.initBaseMap('base_map', { geocode: { selector: '#geocode' }, hideShowMapLink : true});
        bdrs.map.addLocationLayer(bdrs.map.baseMap, bdrs.survey.location.LOCATION_LAYER_NAME);
    
        <c:choose>
		    <c:when test="${survey.predefinedLocationsOnly || not mapEditable}">
                var layer = bdrs.map.addPositionLayer(layerName);
            </c:when>
            <c:otherwise>
               var layer = null;
               <c:choose>
                   <c:when test="${censusMethod != null and (censusMethod.drawLineEnabled or censusMethod.drawPolygonEnabled)}">
                      layer = bdrs.map.addSingleFeatureDrawLayer(bdrs.map.baseMap, layerName, {
                          latSelector: entryForm.latSelector,
                          longSelector: entryForm.longSelector,
                          wktSelector: entryForm.wktSelector,
                          toolActivatedHandler: toolActivatedHandler,
                          initialDrawTool: ${not empty wkt ? 'bdrs.map.control.DRAG_FEATURE' : 'bdrs.map.control.DRAW_POINT'},
                          <c:choose>
                             <c:when test="${censusMethod != null}">
                                 drawPoint: ${censusMethod.drawPointEnabled},
                                 drawLine: ${censusMethod.drawLineEnabled},
                                 drawPolygon: ${censusMethod.drawPolygonEnabled}
                             </c:when>
                             <c:otherwise>
                                 // default for non census method forms
                                 drawPoint: true,
                                 drawLine: false,
                                 drawPolygon: false
                             </c:otherwise>
                          </c:choose>
                      });
                   </c:when>
                   <c:otherwise>
                       layer = bdrs.map.addSingleClickPositionLayer(bdrs.map.baseMap, layerName, entryForm.latSelector, entryForm.longSelector);
                   </c:otherwise>
               </c:choose>
                
                bdrs.map.addLonLatChangeHandler(layer, entryForm.longSelector, entryForm.latSelector);
            </c:otherwise>
        </c:choose>
    
        var lat = jQuery(entryForm.latSelector);
        var lon = jQuery(entryForm.longSelector);
        var wkt = jQuery(entryForm.wktSelector);
        var loc = jQuery(entryForm.locSelector);
        var point;

        // center and zoom the map to either the wkt string, lat/lon pair, or default
        if (wkt && wkt.val() && wkt.val().length > 0) {
            // use the wkt string to determine zoom level and map center if available
            var geom = OpenLayers.Geometry.fromWKT(wkt.val());
            if (geom) {
                geom.transform(bdrs.map.WGS84_PROJECTION, bdrs.map.GOOGLE_PROJECTION);
    
                var feature = new OpenLayers.Feature.Vector(geom);
                layer.addFeatures(feature);
                
                // zoom the map to show the currently selected location
                var geobounds = feature.geometry.getBounds();
                var zoom = bdrs.map.baseMap.getZoomForExtent(geobounds);
                bdrs.map.baseMap.setCenter(geobounds.getCenterLonLat(), zoom);
            }
        } else if((lat && lon && lat.val() && lon.val()) && (lat.val().length > 0 && lon.val().length > 0)) {
            // if there is a lat/lon point, but no wkt string, use them to 
            // determine zoom level and map center
            var lonLat = new OpenLayers.LonLat(
                    parseFloat(lon.val()), parseFloat(lat.val()));
            lonLat = lonLat.transform(bdrs.map.WGS84_PROJECTION,
                                      bdrs.map.GOOGLE_PROJECTION);
            point = new OpenLayers.Geometry.Point(lonLat.lon, lonLat.lat);
            layer.addFeatures(new OpenLayers.Feature.Vector(point));
            
            bdrs.map.centerMap(bdrs.map.baseMap, lonLat, 10);
        } else if (loc && loc.val() > 0) {
            // add the location on initial screen render
            jQuery.ajax({
                url: '${pageContext.request.contextPath}/webservice/location/getLocationById.htm?id='+loc.val(), 
                success: function(data) {
                    var wkt = new OpenLayers.Format.WKT(bdrs.map.wkt_options);
                    var feature = wkt.read(data.location);
    
                    // add the location geometry to the map
                    var loclayer = bdrs.map.baseMap.getLayersByName(bdrs.survey.location.LOCATION_LAYER_NAME)[0];
                    loclayer.removeFeatures(loclayer.features);
    
                    loclayer.addFeatures(feature);
    
                    // zoom the map to show the currently selected location
                    var geobounds = feature.geometry.getBounds();
                    if (point) {
                        geobounds.extend(point);
                    }
                    var zoom = bdrs.map.baseMap.getZoomForExtent(geobounds);
                    bdrs.map.baseMap.setCenter(geobounds.getCenterLonLat(), zoom);
                },
                async: false
            });
        } else {
            // default center map
            bdrs.map.centerMap(bdrs.map.baseMap, null, 3);
        }
    });
</script>