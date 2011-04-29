<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<h1>Taxonomy Listing</h1>
<p>
    Enter the name of a taxon below to view the properties of the taxon, or 
    click on &#8220;Add Taxon&#8221; to create a new taxonomic entry.
</p>

<table class="form_table">
    <tbody>
        <tr>
            <th>
                <label class="strong" for="">Taxon Name:</label>
            </th>
            <td>
                <input type="text" name="taxonomySearch" id="taxonomySearch"/>
            </td>
        </tr>
    </tbody>
</table>

<input type="hidden" name="selectedTaxonPk" id="selectedTaxonPk"/>
<div id="taxonProperties"></div>


<div class="textright">
    <input id="editTaxon" disabled="disabled" class="form_action" type="button" value="Edit Taxon" 
        onclick="window.document.location='${pageContext.request.contextPath}/bdrs/admin/taxonomy/edit.htm?pk='+jQuery('#selectedTaxonPk').val();"/>
    <input class="form_action" type="button" value="Add Taxon" onclick="window.document.location='${pageContext.request.contextPath}/bdrs/admin/taxonomy/edit.htm';"/>
</div>

<script type="text/javascript">
    jQuery(function() {
        bdrs.taxonomy.initListing('#taxonomySearch',
                                  '#selectedTaxonPk',
                                  '#taxonProperties',
                                  '#editTaxon');
    });
</script>