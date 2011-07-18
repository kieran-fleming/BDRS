<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<tiles:useAttribute name="facet" classname=" au.com.gaiaresources.bdrs.controller.review.sightings.facet.Facet" ignore="true"/>

<div class="facet">
    
	<h4 class="${ facet.queryParamName }Header">
	   <div class='tree_node <c:choose><c:when test="${ !facet.containsSelected }">tree_node_collapsed</c:when><c:otherwise>tree_node_expanded</c:otherwise></c:choose>'></div>
	   <c:out value="${ facet.displayName }"/>
    </h4>
	<div class="facetOptions ${ facet.queryParamName }OptContainer"
	    <%-- Open the facet if it contains a selected option --%>
	    <%-- c:if test="${ !facet.containsSelected }"> style="display:none"</c:if --%>
	>
		<c:forEach var="facetOption" items="${ facet.facetOptions }">
		    <div>
		        <input id="${ facet.queryParamName }_${ facetOption.value }"
                    type="checkbox" 
                    value="${ facetOption.value }" 
                    name="${ facet.queryParamName }"
                    <c:if test="${ facetOption.selected }">
                        checked="checked"
                    </c:if>
                />
		        <label for="${ facet.queryParamName }_${ facetOption.value }">
		            <c:out value="${ facetOption.displayName }"/>&nbsp;(<c:out value="${ facetOption.count }"/>)
		        </label>
		    </div>
		</c:forEach>
    </div>
</div>