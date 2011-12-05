<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>

<h1><c:out value="${ taxonGroup.name }"/></h1>

<p>
To view more detailed information for a species, please click on either the name or thumbnail of the species you want to view.
</p>

<p>
To record a sighting of a species click on the <i><b>Record Now</b></i> button in the row for the species you'd like to record.
</p>

<display:table name="taxaPaginator.list" id="fieldGuideTaxaListingTable" 
    decorator="au.com.gaiaresources.bdrs.controller.fieldguide.FieldGuideTaxaTableDecorator"
    style="width:100%" pagesize="50" sort="external" partialList="true" size="taxaPaginator.count"
    class="datatable">
    
    <display:column property="scientificName" title="Scientific Name" sortable="true" sortName="scientificName" class="scientificName"/>
    <display:column property="commonName" title="Common Name" sortable="true" sortName="commonName"/>
    <display:column property="thumbnail" title="Thumbnail" sortable="false" class="textleft"/>
    <display:column property="recordNowButton" title="Record Now" sortable="false" class="textcenter"/>
</display:table>