<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<tiles:useAttribute name="recordId" classname="java.lang.String" ignore="true"/>

<div class="alaSightingsMapViewContent">
	<div class="map_wrapper" id="map_wrapper">
	    <input type="hidden" id="recordId" value="${ recordId }" />
	    <div id="atlasSightingsMap" class="defaultmap review_map"></div>
	    <div class="recordCount textright"></div>
	</div>
</div>

<script type="text/javascript">
    jQuery(function() {
        
    });
</script>

