<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<h1>Advanced Review</h1>

<!-- for handling the page description in theme -->
<c:if test="${not empty pageDescription}">
    ${pageDescription}
</c:if>

<form id="facetForm" method="GET" action="">
    <input type="hidden" name="recordId" value = "${ recordId }"/>
	<div class="alaSightingsContent">
	    <div class="facetCol left">
	        <div class="columnBanner">Refine results for</div>
	        <c:forEach var="facet" items="${ facetList }">
	            <jsp:useBean id="facet" type="au.com.gaiaresources.bdrs.service.facet.Facet" ></jsp:useBean>
		        <c:if test="<%= facet.isActive() %>">
			        <tiles:insertDefinition name="advancedReviewFacet">
			            <tiles:putAttribute name="facet" value="${ facet }"/>
			        </tiles:insertDefinition>
		        </c:if>
	        </c:forEach>
	    </div>
	    <div class="resultCol right">
	        <div class="columnBanner">
	           <span>
                   <c:choose>
                       <c:when test="${ recordCount == 1 }">
                           <c:out value="${ recordCount }"/> record returned
                       </c:when>
                       <c:otherwise>
                           <c:out value="${ recordCount }"/> records returned
                       </c:otherwise>
                   </c:choose>
               </span>
	        </div>
	        
	        <div class="tabPane">
		        <div class="controlPanel">
		            <div>
		                <span class="searchContainer">
		                    <label for="searchText">Search within results</label>
	                        <input type="text" id="searchText" name="searchText" value="<c:out value="${ searchText }"/>"/>
	                        <input id="searchButton" class="form_action" type="submit" name="facetUpdate" value="Search"/>
	                    </span>
		                <input type="hidden" name="viewType"
		                    <c:choose>
							   <c:when test="${ tableViewSelected }">
			                       value="table"
			                   </c:when>
			                   <c:when test="${ downloadViewSelected }">
			                       value="download"
			                   </c:when>
			                    <c:otherwise>
			                       value="map"
			                   </c:otherwise>
		                    </c:choose> 
	                    />
						<a id="downloadViewTab" href="javascript: void(0);">
                            <div id="downloadViewTab" class="displayTab right <c:if test="${ downloadViewSelected }">displayTabSelected</c:if>">Download</div>
                        </a>
		                <a id="listViewTab" href="javascript: void(0);">
	                       <div class="displayTab right <c:if test="${ tableViewSelected }">displayTabSelected</c:if>">List</div>
	                    </a>
		                <a href="javascript: void(0);">
		                   <div id="mapViewTab" class="displayTab right <c:if test="${ mapViewSelected }">displayTabSelected</c:if>">Map</div>
	                    </a>
	                    <div class="clear"></div>
		            </div>
		        </div>
		        
		        <c:choose>
		           
		           <c:when test="${ tableViewSelected }">
		               <tiles:insertDefinition name="advancedReviewTableView">
	                    </tiles:insertDefinition>
                   </c:when>
				   <c:when test="${ downloadViewSelected }">
				   	    <tiles:insertDefinition name="downloadSightingsWidget" />
				   </c:when>
                    <c:otherwise>
                        <tiles:insertDefinition name="advancedReviewMapView">
                            <tiles:putAttribute name="recordId" value="${ recordId }"/>
                        </tiles:insertDefinition>
                   </c:otherwise>
		        </c:choose>
	        </div> 
	    </div>
	    <div class="clear"></div>
	</div>
</form>

<script type="text/javascript">
   jQuery(function() {
       // Insert click handlers to show and hide facet options
       <c:forEach var="facet" items="${ facetList }">
           jQuery(".${ facet.prefix }_${ facet.queryParamName }Header").click(function() {
               jQuery(".${ facet.prefix }_${ facet.queryParamName }OptContainer").slideToggle("fast", function() {
                   var collapsed = jQuery(".${ facet.prefix }_${ facet.queryParamName }OptContainer").css("display") === "none";
                   var treeNode = jQuery(".${ facet.prefix }_${ facet.queryParamName }Header .tree_node");
                   if(collapsed) {
                       treeNode.removeClass('tree_node_expanded');
                   } else {
                       treeNode.addClass('tree_node_expanded');
                   }
               });
           });
      </c:forEach>
      
      bdrs.advancedReview.initFacets('#facetForm', '.facet');
      bdrs.advancedReview.initTabHandlers();
	  
	  <c:if test="${ downloadViewSelected }">
            bdrs.review.downloadSightingsWidget.init("#facetForm", "/review/sightings/advancedReviewDownload.htm");
       </c:if>
   });

   jQuery(window).load(function() {
      <c:if test="${ mapViewSelected }">
	  // only init the map view if the map view is selected
      bdrs.advancedReview.initMapView('#facetForm',  
              'atlasSightingsMap', { geocode: { selector: '#geocode' }}, '#recordId');
	  </c:if>
   });
</script>
