<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<div class="alaSightingsMapViewContent">
	<div class="map_wrapper" id="map_wrapper">
	    <div id="atlasSightingsMap" class="defaultmap review_map"></div>
	    <div class="recordCount textright"></div>
	</div>
</div>

<script type="text/javascript">
    jQuery(function() {
        bdrs.advancedReview.initMapView('#facetForm',  
                                      'atlasSightingsMap', { geocode: { selector: '#geocode' }});
    });
</script>

