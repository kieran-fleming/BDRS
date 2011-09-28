<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<h1>Search Taxonomy</h1>
<p>
    Enter the name of a taxon below to view the properties of the taxon, or 
    click on &#8220;Add Taxon&#8221; to create a new taxonomic entry.
</p>

<table class="form_table">
    <tbody>
        <tr>
            <th class="searchTaxonomyHeader">
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


<div class="buttonpanel textright">
    <input id="editTaxon" disabled="disabled" class="form_action" type="button" value="Edit Taxon" 
        onclick="window.document.location='${pageContext.request.contextPath}/bdrs/admin/taxonomy/edit.htm?pk='+jQuery('#selectedTaxonPk').val();"/>
    <input class="form_action" type="button" value="Add Taxon" onclick="window.document.location='${pageContext.request.contextPath}/bdrs/admin/taxonomy/edit.htm';"/>
</div>

<div class="atlas_import_container">
    <p>
      To import a species or group of species from the ALA, enter a single lsid
      or list of lsids, separated by commas, in the text area below.
    </p>
    <textarea id="guidList" rows="10" cols="300"></textarea>
    <div class="checkbox_container">
        <label for="shortProfile">import short profile(s) only</label>
        <input type="checkbox" id="shortProfile" checked="checked"></input>
    </div>
    <div class="textright">
        <input class="form_action" type="button" value="Import Taxon" onclick="importALAProfile();"/>
    </div>
</div>

<script type="text/javascript">
    jQuery(function() {
        bdrs.taxonomy.initListing('#taxonomySearch',
                                  '#selectedTaxonPk',
                                  '#taxonProperties',
                                  '#editTaxon');
    });

    var importALAProfile = function() { 
        if (jQuery('#guidList').val()) {
            var answer = confirm("Are you sure? Existing entries imported from ALA will be replaced!")
            if (answer) {
                window.document.location='${pageContext.request.contextPath}/bdrs/admin/taxonomy/importNewProfiles.htm?guids='+jQuery('#guidList').val()+
                    (jQuery('#shortProfile').is(':checked') ? '&shortProfile='+jQuery('#shortProfile').val() : '');
            }
        }
    }
</script>