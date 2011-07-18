<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<h1>${geoMap.name}</h1>
<p>${geoMap.description}</p>

<div class="textright buttonpanel">
    <a id="maximiseMapLink" class="text-left" href="javascript:bdrs.map.maximiseMap('#maximiseMapLink', '#map_wrapper', 'Enlarge Map', 'Shrink Map', 'review_map_fullscreen', 'review_map', '#view_base_map', bdrs.map.baseMap)">Enlarge Map</a>
</div>
<div class="map_wrapper" id="map_wrapper">
    <div id="view_base_map" class="defaultmap review_map"></div>
    <div id="geocode" class="geocode"></div>
    <div class="recordCount textright"></div>  
</div>

<div class="clear"></div>

<script type="text/javascript">

    jQuery(function() {
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
					opacity: 0.25,
					fillColor: "${assignedLayer.layer.fillColor}",
                    strokeColor: "${assignedLayer.layer.strokeColor}",
                    strokeWidth: ${assignedLayer.layer.strokeWidth},
                    size: ${assignedLayer.layer.symbolSize}
				};
				bdrs.map.addMapServerLayer(bdrs.map.baseMap, "${assignedLayer.layer.name}", bdrs.map.getBdrsMapServerUrl(), layerOptions);
            </c:when>
			<c:when test="${assignedLayer.layer.layerSource == \"SURVEY_KML\"}">
			    var layerOptions = {
                    visible: true,
                    // cluster strategy doesn't work properly for polygons
                    includeClusterStrategy: true
                };
                var layer = bdrs.map.addKmlLayer(bdrs.map.baseMap, "${assignedLayer.layer.name}", "${pageContext.request.contextPath}/bdrs/map/getLayer.htm?layerPk=${assignedLayer.layer.id}", layerOptions);
                layerArray.push(layer);
			</c:when>
			<c:when test="${assignedLayer.layer.layerSource == \"KML\"}">
                var layerOptions = {
                    visible: true,
					// cluster strategy doesn't work properly for polygons
                    includeClusterStrategy: false
                };
				var layer = bdrs.map.addKmlLayer(bdrs.map.baseMap, "${assignedLayer.layer.name}", "${pageContext.request.contextPath}/bdrs/map/getLayer.htm?layerPk=${assignedLayer.layer.id}", layerOptions);
				layerArray.push(layer);
            </c:when>
            </c:choose>
        }
        </c:forEach>

        // Add select for KML stuff
        bdrs.map.addSelectHandler(bdrs.map.baseMap, layerArray);
    });


</script>