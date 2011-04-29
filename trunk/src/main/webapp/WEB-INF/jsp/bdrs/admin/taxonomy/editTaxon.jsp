<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@page import="au.com.gaiaresources.bdrs.model.taxa.TaxonRank"%>

<jsp:useBean id="taxon" type="au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies" scope="request"/>
<jsp:useBean id="taxonProfileList" type="java.util.List" scope="request"/>
<h1>Edit Taxon</h1>

<form method="POST" action="${pageContext.request.contextPath}/bdrs/admin/taxonomy/edit.htm" enctype="multipart/form-data">
    <c:if test="${ taxon.id != null }">
	   <input id="taxonPk" type="hidden" name="taxonPk" value="${ taxon.id }"/>
   </c:if>
   
	<table class="form_table">
	    <tbody>
	        <tr>
	            <th><label for="scientificName">Scientific Name</label></th>
	            <td><input class="validate(required)" type="text" name="scientificName" value="${ taxon.scientificName }"/></td>
	        </tr>
	        <tr>
                <th><label for="commonName">Common Name</label></th>
                <td><input class="validate(required)" type="text" name="commonName" value="${ taxon.commonName }"/></td>
            </tr>
            <tr>
                <th><label for="rank">Rank</label></th>
                <td>
                    <select name="taxonRank">
                        <c:forEach var="rankEnum" items="<%= TaxonRank.values() %>">
                            <jsp:useBean id="rankEnum" type="au.com.gaiaresources.bdrs.model.taxa.TaxonRank"/>
                            <option value="<%= rankEnum.toString() %>"
                                <c:if test="<%= (taxon.getTaxonRank() != null && rankEnum.equals(taxon.getTaxonRank())) || (taxon.getTaxonRank() == null && TaxonRank.SPECIES.equals(rankEnum)) %>">
                                    selected="selected"
                                </c:if>
                            >
                                <%= rankEnum.getIdentifier() %>
                            </option>
                        </c:forEach>
                    </select>
                </td>
            </tr>
            <tr>
                <th><label for="parent">Parent</label></th>
                <td>
                    <input id="parent" type="text" name="parent" value="${ taxon.parent.scientificName }"/>
                    <input id="parentPk" type="text" name="parentPk" value="${ taxon.parent.id }" style="visibility:hidden;width:1px;padding:2px 0 0 0;margin:0 0 0 -4px;border-width:0;"/>
                </td>
            </tr>
            <tr>
                <th><label for="taxonGroup">Group</label></th>
                <td>
                    <input id="taxonGroup" class="" type="text" name="taxonGroup" value="${ taxon.taxonGroup.name }"/>
                    <input id="taxonGroupPk" class="validate(required)" type="text" name="taxonGroupPk" value="${ taxon.taxonGroup.id }" style="visibility:hidden;width:1px;padding:2px 0 0 0;margin:0 0 0 -4px;border-width:0;"/>
                </td>
            </tr>
            <tr>
                <th><label for="author">Author</label></th>
                <td><input class="" type="text" name="author" value="${ taxon.author }"/></td>
            </tr>
            <tr>
                <th><label for="year">Year</label></th>
                <td><input class="" type="text" name="year" value="${ taxon.year }"/></td>
            </tr>
	    </tbody>
	</table>
	
	<h3>Taxon Profile</h3>
	<p>
	   The taxon profile provides additional data about this taxon such as
	   distinctive markings, identifying characteristics, habitat and biology. 
	</p>
	<div class="textright buttonpanel">
       <input type="button" class="form_action" value="Add Profile" onclick="bdrs.taxonomy.addNewProfile('#newProfileIndex', '#taxonProfileTable');"/>
    </div>
    <table id="taxonProfileTable" class="datatable textcenter">
        <thead>
            <tr>
                <th>&nbsp;</th>
                <th>Type</th>
                <th>Database Name</th>
                <th>Title</th>
                <th>Content</th>
                <th>Delete</th>
            </tr>
        </thead>
        <tbody>
            <tiles:insertDefinition name="profileTableBody">
	            <tiles:putAttribute name="taxonProfileList" value="${ taxonProfileList }"/>
	            <tiles:putAttribute name="newProfileIndex" value="${ newProfileIndex }"/>
	        </tiles:insertDefinition>
        </tbody>
    </table>
   <input id="newProfileIndex" type="hidden" value="<%= taxonProfileList.size() + 1 %>"/>
    
    <h3>Group Attributes</h3>
    <p>
        Group attributes are the custom attributes of this taxon as specified
        by this taxon group.
    </p>
    <div id="taxonAttributeTable">
    	<tiles:insertDefinition name="taxonAttributeTable">
    		<tiles:putAttribute name="formFieldList" value="${ formFieldList }"/>
    	</tiles:insertDefinition>
    </div>
    
	<div class="textright buttonpanel">
	   <input type="submit" class="form_action" value="Save"/>
	</div>
</form>

<script type="text/javascript">
    jQuery(function() {
	    bdrs.taxonomy.initEditTaxon("#parent","#parentPk", 
	                                "#taxonGroup", "#taxonGroupPk",
	                                "#taxonPk", "#taxonAttributeTable", 
	                                "#taxonProfileTable", "#newProfileIndex");
    });
</script>