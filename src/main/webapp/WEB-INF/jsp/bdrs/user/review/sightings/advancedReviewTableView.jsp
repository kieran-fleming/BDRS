<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<tiles:useAttribute name="sortBy" classname="java.lang.String" ignore="true"/>
<tiles:useAttribute name="sortOrder" classname="java.lang.String" ignore="true"/>
<tiles:useAttribute name="resultsPerPage" classname="java.lang.Integer" ignore="true"/>
<tiles:useAttribute name="pageCount" classname="java.lang.Long" ignore="true"/>
<tiles:useAttribute name="pageNumber" classname="java.lang.Long" ignore="true"/>

<div class="alaSightingsTableViewContent">
    <div class="sortPanel">
        <div class="left">
            <label for="resultsPerPage">Results per page</label>
            <select id="resultsPerPage" name="resultsPerPage">
                <option <c:if test="${resultsPerPage == 10}">selected="selected"</c:if>>10</option>
                <option <c:if test="${resultsPerPage == 20}">selected="selected"</c:if>>20</option>
                <option <c:if test="${resultsPerPage == 50}">selected="selected"</c:if>>50</option>
                <option <c:if test="${resultsPerPage == 100}">selected="selected"</c:if>>100</option>
            </select>
        </div>
        <div class="right">
            <label for="sortOrder">Sort Order</label>
            <select id="sortOrder" name="sortOrder">
                <option value="ASC"
                    <c:if test="${ 'ASC' == sortOrder }">
                        selected="selected"
                    </c:if>
                >
                    ascending
                </option>
                <option value="DESC"
                    <c:if test="${ 'DESC' == sortOrder }">
                        selected="selected"
                    </c:if>
                >
                    descending
                </option>
            </select>
        </div>
        
        <div class="right">
            <label for="sortBy">Sort By</label>
            <select id="sortBy" name="sortBy">
                <option value="record.when"
                    <c:if test="${ 'when' == sortBy }">
                        selected="selected"
                    </c:if>
                >
                    date
                </option>
                <option value="species.scientificName"
                    <c:if test="${ 'species.scientificName' == sortBy }">
                        selected="selected"
                    </c:if>
                >
                    scientific name
                </option>
                <option value="species.commonName"
                    <c:if test="${ 'species.commonName' == sortBy }">
                        selected="selected"
                    </c:if>
                >
                    common name
                </option>
                <option value="location.name"
                    <c:if test="${ 'location.name' == sortBy }">
                        selected="selected"
                    </c:if>
                >
                    location
                </option>
                <option value="censusMethod.type"
                    <c:if test="${ 'censusMethod.type' == sortBy }">
                        selected="selected"
                    </c:if>
                >
                    type
                </option>
            </select>
        </div>
        
        <div class="clear"></div>
    </div>
	
	<span id="alaSightingsTable" class="alaSightingsTable"></span>
	
</div>
<div class="textcenter">
   <div id="searchNavBar">
     <input type="hidden" value="${ pageNumber }" name="pageNumber" id="pageNumber"/>
     <ul>
     <c:choose>
        <c:when test="${ pageNumber == 1 }">
            <li id="prevPage">&#171;&nbsp;Previous</li>
        </c:when>
        <c:otherwise>
            <li id="prevPage"><a class="pageLink" href="javascript:bdrs.advancedReview.pageSelected(${pageNumber-1});">&#171;&nbsp;Previous</a></li>
        </c:otherwise>
     </c:choose>
     <c:forEach var="i" begin="1" end="${pageCount}" step="1" varStatus ="status">
         <c:choose>
            <c:when test="${ pageNumber == i }">
                <li class="currentPage"><c:out value="${i}"/></li>
            </c:when>
            <c:otherwise>
                <li><a class="pageLink" href="javascript:bdrs.advancedReview.pageSelected(${i});"><c:out value="${i}"/></a></li>
            </c:otherwise>
         </c:choose>
     </c:forEach>
     <c:choose>
        <c:when test="${ pageNumber == pageCount }">
            <li id="nextPage">Next&nbsp;&#187;</li>
        </c:when>
        <c:otherwise>
            <li id="nextPage"><a class="pageLink" href="javascript:bdrs.advancedReview.pageSelected(${pageNumber+1});">Next&nbsp;&#187;</a></li>
        </c:otherwise>
     </c:choose>
     </ul>
   </div>
</div>

<script type="text/javascript">
    jQuery(function() {
        bdrs.advancedReview.initTableView('#facetForm',  
            '#alaSightingsTable', 'select[name=sortOrder]', 'select[name=sortBy]', 
            'select[name=resultsPerPage]');    
    });
</script>
