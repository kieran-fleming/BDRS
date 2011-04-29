<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<h1>Select a Taxonomic Group</h1>

<c:forEach items="${ taxonGroups }" var="taxonGroup">
    <jsp:useBean id="taxonGroup" type="au.com.gaiaresources.bdrs.model.taxa.TaxonGroup"/>
    <div class="left field_guide_group">
        <c:if test="${ taxonGroup.thumbNail != null}">
	        <a href="${pageContext.request.contextPath}/fieldguide/taxa.htm?groupId=${ taxonGroup.id }">
                <img class="max_size_img" src="${pageContext.request.contextPath}/files/download.htm?<%= taxonGroup.getThumbnailFileURL() %>"/>
	        </a>
        </c:if>
        <div class="textcenter" class="clear">
	        <a href="${pageContext.request.contextPath}/fieldguide/taxa.htm?groupId=${ taxonGroup.id }">
	            <c:out value="${ taxonGroup.name }"/>
	        </a>   
        </div>
    </div>
</c:forEach>
<div class="clear"></div>