<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<h1>Import Taxon Information</h1>

<div class="atlas_import_container">
    <cw:getContent key="admin/taxonomy/listing" />
    <form method="POST" action="${pageContext.request.contextPath}/bdrs/admin/taxonomy/importNewProfiles.htm" onsubmit="importALAProfile();">
            <div class="input_container">
        <table class="form_table">
                <tbody>
                    <tr>
                        <th title="Enter a list of LSIDs for species to import from the ALA">Enter a comma separated list of LSIDs</th>
                        <td>
                            <textarea class="validate(required)" id="guidList" name="guids" rows="10" cols="300"></textarea>
                        </td>
                    </tr>
                    <tr>
                        <th title="Enter the group to import these species to, or leave blank for the default">Taxon Group for Import</th>
                        <td>
                            <input id="taxonGroup" type="text" name="taxonGroup" value="${ taxon.taxonGroup.name }"/>
                            <input id="taxonGroupPk" type="text" name="taxonGroupPk" value="${ taxon.taxonGroup.id }" style="visibility:hidden;width:1px;padding:2px 0 0 0;margin:0 0 0 -4px;border-width:0;"/>
                        </td>
                    </tr>
                    <tr>
                        <th title="Do you wish to import full species profiles?">Import short profile(s) only?</th>
                        <td>
                            <div class="checkbox_container">
                                <input type="checkbox" id="shortProfile"></input>
                            </div>
                        </td>
                    </tr>
                </tbody>
            </table>
            
            <div class="buttonpanel textright">
                <input class="form_action" type="submit" value="Import Taxon"/>
            </div>
    </div>
        </form>
</div>

<script type="text/javascript">
    var importALAProfile = function() { 
        if (jQuery('#guidList').val()) {
            return confirm("Are you sure? Existing entries imported from ALA will be replaced!")
        }
        return false;
    }
</script>