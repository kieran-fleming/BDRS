<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<jsp:useBean id="context" scope="request" type="au.com.gaiaresources.bdrs.servlet.RequestContext"></jsp:useBean>

<h1>${geoMap.name}</h1>
<p>${geoMap.description}</p>

<!-- for now, only allow download of records when the map has no anonymous access -->
<!-- consider adding map metadata to have the option of allowing record downloads -->
<c:if test="${!geoMap.anonymousAccess}">
    <div class="left">
	    <div>
	        <a href="javascript: bdrs.map.downloadRecordsForActiveLayers(bdrs.map.baseMap, 'KML');">
	            Download KML for records
	        </a>
	        <span>&nbsp;|&nbsp;<span/>
	        <a href="javascript: bdrs.map.downloadRecordsForActiveLayers(bdrs.map.baseMap, 'SHAPEFILE');">
	            Download SHP for records
	        </a>
	    </div>
	</div>
</c:if>

<div class="right">
	<span class="hideMapHide">
		<a id=mapScrollToggle class="text-left" href="javascript:void(0);">
	        Zoom Scroll [<span id="scrollableChecker">x</span>]
	    </a>
	    <span>&nbsp; |&nbsp;</span>
	    <a id="maximiseMapLink" class="text-left" href="javascript:bdrs.map.maximiseMap('#maximiseMapLink', '#map_wrapper', 'Enlarge Map', 'Shrink Map (esc)', 'review_map_fullscreen', 'review_map', '#view_base_map', bdrs.map.baseMap)">
	    	<span>Enlarge Map</span>
	    </a>
	    <span>&nbsp; |&nbsp;</span>
	 </span>
     <a id="mapToggle" class="text-left" href="javascript:bdrs.map.collapseMap($('.map_wrapper'),$('#mapToggle'))">
        Hide Map
    </a>
</div>

<div class="clear"></div>

<div class="map_wrapper" id="map_wrapper">
    <div id="view_base_map" class="defaultmap review_map"></div>
    <div id="geocode" class="geocode"></div>
    <div class="recordCount textright"></div>  
</div>

<div class="clear"></div>

<script type="text/javascript">

    jQuery(window).load(function() {
		
        <c:choose>
	        <c:when test="${geoMap.anonymousAccess}">
                bdrs.map.initBaseMap('view_base_map', {isPublic:true, ajaxFeatureLookup: true, geocode:{selector:'#geocode'}});
	        </c:when>
	        <c:otherwise>
                bdrs.map.initBaseMap('view_base_map', {isPublic:false, ajaxFeatureLookup: true, geocode: { selector: '#geocode' }});
	        </c:otherwise>
	    </c:choose>
        
        var layerArray = new Array();
        <c:forEach items="${assignedLayers}" var="assignedLayer">
        {
			<c:choose>
            <c:when test="${assignedLayer.layer.layerSource == \"SHAPEFILE\" || assignedLayer.layer.layerSource == \"SURVEY_MAPSERVER\"}">
			    var layerOptions = {
					bdrsLayerId: ${assignedLayer.layer.id},
					visible: ${assignedLayer.visible},
					opacity: bdrs.map.DEFAULT_OPACITY,
					fillColor: "${assignedLayer.layer.fillColor}",
                    strokeColor: "${assignedLayer.layer.strokeColor}",
                    strokeWidth: ${assignedLayer.layer.strokeWidth},
                    size: ${assignedLayer.layer.symbolSize},
					upperZoomLimit: ${assignedLayer.upperZoomLimit != null ? assignedLayer.upperZoomLimit : 'null'},
					lowerZoomLimit: ${assignedLayer.lowerZoomLimit != null ? assignedLayer.lowerZoomLimit: 'null'}
				};
				bdrs.map.addMapServerLayer(bdrs.map.baseMap, "${assignedLayer.layer.name}", bdrs.map.getBdrsMapServerUrl(), layerOptions);
            </c:when>
			<c:when test="${assignedLayer.layer.layerSource == \"SURVEY_KML\"}">
			    var layerOptions = {
                    visible: ${assignedLayer.visible},
                    // cluster strategy doesn't work properly for polygons
                    includeClusterStrategy: true,
                    upperZoomLimit: ${assignedLayer.upperZoomLimit != null ? assignedLayer.upperZoomLimit : 'null'},
                    lowerZoomLimit: ${assignedLayer.lowerZoomLimit != null ? assignedLayer.lowerZoomLimit: 'null'}
                };
                var layer = bdrs.map.addKmlLayer(bdrs.map.baseMap, "${assignedLayer.layer.name}", "${pageContext.request.contextPath}/bdrs/map/getLayer.htm?layerPk=${assignedLayer.layer.id}", layerOptions);
                layerArray.push(layer);
			</c:when>
			<c:when test="${assignedLayer.layer.layerSource == \"KML\"}">
                var layerOptions = {
                    visible: ${assignedLayer.visible},
					// cluster strategy doesn't work properly for polygons
                    includeClusterStrategy: false,
                    upperZoomLimit: ${assignedLayer.upperZoomLimit != null ? assignedLayer.upperZoomLimit : 'null'},
                    lowerZoomLimit: ${assignedLayer.lowerZoomLimit != null ? assignedLayer.lowerZoomLimit: 'null'}
                };
				var layer = bdrs.map.addKmlLayer(bdrs.map.baseMap, "${assignedLayer.layer.name}", "${pageContext.request.contextPath}/bdrs/map/getLayer.htm?layerPk=${assignedLayer.layer.id}", layerOptions);
				layerArray.push(layer);
            </c:when>
            </c:choose>
        }
        </c:forEach>

        // Add select for KML stuff
        bdrs.map.addSelectHandler(bdrs.map.baseMap, layerArray);
		
		// In order to force correct map centering in IE7
	    jQuery("#view_base_map").removeClass("defaultmap");
		bdrs.map.centerMap(bdrs.map.baseMap);
		jQuery("#view_base_map").addClass("defaultmap");
        
		// Map Scroll Zoom Toggling
        jQuery('#mapScrollToggle').toggle(function(){
        	bdrs.map.scrollZoom(false, bdrs.map.baseMap);
        	jQuery('#scrollableChecker').text(' ');
        },
        function(){
        	bdrs.map.scrollZoom(true, bdrs.map.baseMap);
        	jQuery('#scrollableChecker').text('x');
        });

        // Check for Map Scroll Zoom Cookie
        if (bdrs.util.cookie.read('cookie.map.zoomscroll') === "false") {
        	bdrs.map.scrollZoom(false, bdrs.map.baseMap);
        	jQuery('#scrollableChecker').text(' ');
        } else {
        	bdrs.map.scrollZoom(true, bdrs.map.baseMap);
        	jQuery('#scrollableChecker').text('x');
        }
    });

</script>