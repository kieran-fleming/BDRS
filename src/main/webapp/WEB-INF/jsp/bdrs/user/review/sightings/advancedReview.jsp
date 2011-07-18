<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>


<h1>Advanced Review</h1>

<form id="facetForm" method="GET" action="">
	<div class="alaSightingsContent">
	    <div class="facetCol left">
	        <div class="columnBanner">Refine results for</div>
	        <c:forEach var="facet" items="${ facetList }">
		        <tiles:insertDefinition name="advancedReviewFacet">
		            <tiles:putAttribute name="facet" value="${ facet }"/>
		        </tiles:insertDefinition>
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
               <c:if test="${ surveyId != null }">
                   <a id="recordDownload" class="right" href="javascript:void(0);">Download</a>
                   <input type="hidden" name="surveyId" value="${ surveyId }"/>
               </c:if>
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
		                       <c:when test="${ mapViewSelected }">
		                           value="map"
		                       </c:when>
		                       <c:otherwise>
		                           value="table"
	                           </c:otherwise>
		                    </c:choose> 
	                    />
		                <a id="listViewTab" href="javascript: void(0);">
	                       <div class="displayTab right <c:if test="${ !mapViewSelected }">displayTabSelected</c:if>">List</div>
	                    </a>
		                <a href="javascript: void(0);">
		                   <div id="mapViewTab" class="displayTab right <c:if test="${ mapViewSelected }">displayTabSelected</c:if>">Map</div>
	                    </a>
	                    <div class="clear"></div>
		            </div>
		        </div>
		        
		        <c:choose>
		           <c:when test="${ mapViewSelected }">
	                    <tiles:insertDefinition name="advancedReviewMapView">
	                    </tiles:insertDefinition>
		           </c:when>
		           <c:otherwise>
		               <tiles:insertDefinition name="advancedReviewTableView">
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
           jQuery(".${ facet.queryParamName }Header").click(function() {
               jQuery(".${ facet.queryParamName }OptContainer").slideToggle("fast", function() {
                   var collapsed = jQuery(".${ facet.queryParamName }OptContainer").css("display") === "none";
                   var treeNode = jQuery(".${ facet.queryParamName }Header .tree_node");
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
      bdrs.advancedReview.initRecordDownload("#facetForm", "#recordDownload");
   });
</script>
