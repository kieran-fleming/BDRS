<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<tiles:useAttribute name="taxon" classname="au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies" ignore="true"/>
<%@page import="au.com.gaiaresources.bdrs.model.taxa.TaxonRank"%>

<c:if test="${ taxon.parent != null }">
    <tiles:insertDefinition name="fieldGuideTaxonRankRow">
        <tiles:putAttribute name="taxon" value="${ taxon.parent }"/>
    </tiles:insertDefinition>
</c:if>

<tr class="fieldguide_taxon_rank_row">
    <th class="textright"><c:out value="<%= taxon.getTaxonRank().getIdentifier() %>"/></th>
    <td
        <c:choose>
            <c:when test="<%= TaxonRank.SPECIES.equals(taxon.getTaxonRank()) %>">
                class="scientificName"
            </c:when>
            <c:when test="<%= TaxonRank.GENUS.equals(taxon.getTaxonRank()) %>">
                class="scientificName"
            </c:when>
        </c:choose> 
    >
        <a href="${pageContext.request.contextPath}/fieldguide/taxon.htm?id=${ taxon.id }">
            <c:out value="${ taxon.scientificName }"/>
        </a>
    </td>
</tr>
