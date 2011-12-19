<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<h1>Edit an Exsiting Taxonomic Group</h1>

<p>
    Taxon groups are an arbitrary grouping of taxa such as 
    &#147;My Favourite Birds&#148; or &#147;Endangered Birdlife in 
    South&#8211;West Austraila&#148;. Click on the link in the &#147;Group Name&#148; column to edit the details 
    of the taxonomic group or click on the &#147;Add Taxon Group&#148; button to add a new one.
</p>

<div class="input_container">
    <div class="buttonpanel textright">
        <input class="form_action" type="button" value="Add Taxon Group" onclick="window.document.location='${pageContext.request.contextPath}/bdrs/admin/taxongroup/edit.htm';"/>
    </div>
    <table id="taxon_group_listing" class="datatable">
        <thead>
            <tr>
                <th>Thumbnail</th>
                <th>Group Name</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach items="${ taxonGroupList }" var="group">
                <jsp:useBean id="group" type="au.com.gaiaresources.bdrs.model.taxa.TaxonGroup"/>
                <tr>
                    <td class="thumb">
                        <c:if test="${ group.thumbNail != null }">
                            <a href="${pageContext.request.contextPath}/bdrs/admin/taxongroup/edit.htm?pk=${ group.id }">
                                <img height="64" src="${pageContext.request.contextPath}/files/download.htm?<%= group.getThumbnailFileURL() %>" alt="${ group.thumbNail }"/>                                                
                            </a>
                        </c:if>
                    </td>
                    <td class="name">
                        <a href="${pageContext.request.contextPath}/bdrs/admin/taxongroup/edit.htm?pk=${ group.id }">${ group.name }</a>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>