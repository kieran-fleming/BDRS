<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<%@page import="au.com.gaiaresources.bdrs.controller.survey.SpeciesListType"%>
<jsp:useBean id="listType" type="au.com.gaiaresources.bdrs.controller.survey.SpeciesListType" scope="request"/>

<h1>Choose Taxonomy</h1>
<form method="POST" action="${pageContext.request.contextPath}/bdrs/admin/survey/editTaxonomy.htm">
    <input type="hidden" name="surveyId" value="${survey.id}"/>

    <div>
        <p>Users will be able to record</p>

        <fieldset id="speciesListTypeFieldSet">
            <c:forEach items="<%=SpeciesListType.values()%>" var="type">
                <jsp:useBean id="type" type="au.com.gaiaresources.bdrs.controller.survey.SpeciesListType" />
                <div>
                    <input id="<%= type.getCode() %>"
                           class="vertmiddle"
                           type="radio"
                           name="speciesListType"
                           value="<%= type.toString() %>"
                           <c:if test="<%= type.equals(listType) %>">
                               checked="checked"
                           </c:if>
                    />
                    <label for="<%= type.getCode() %>" title="<%= type.getTip() %>">
                        <%= type.getName() %>
                    </label>
                </div>
            </c:forEach>
        </fieldset>
    </div>

    <div id="speciesListTypeChooserWrapper">
        <div class="speciesListChooser ONE_SPECIES MANY_SPECIES" style="display: none;">
            <h2></h2>

            <label for="species_search">Species:</label>
            <input id="species_search" type="text" name="species_search"
                placeholder="Search" onkeydown="if(event.keyCode==13){return false;}"/>

            <h3>Species</h3>
            <a class="right" href="javascript:void(0);" onclick="jQuery('#speciesTable tbody tr').remove();">Remove All Species</a>
            <table id="speciesTable" class="datatable">
                <thead>
                    <tr>
                        <th>Scientific Name</th>
                        <th>Common Name</th>
                        <th>Delete</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${survey.species}" var="species">
                        <tr>
                            <td>
                                <c:out value="${species.scientificName}"/>
                                <input type="hidden" name="species" value="${species.id}"/>
                            </td>
                            <td><c:out value="${species.commonName}"/></td>
                            <td class="textcenter">
                                <a href="javascript: void(0);" onclick="jQuery(this).parents('tr').remove();">
                                    <img src="${pageContext.request.contextPath}/images/icons/delete.png" alt="Delete"/>
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>

        </div>
        <div class="speciesListChooser SPECIES_GROUP" style="display: none;">
            <h2></h2>

            <label for="speciesGroup_search">Species Group:</label>
            <input id="speciesGroup_search" type="text" name="speciesGroup_search"
                    placeholder="Search" onkeydown="if(event.keyCode==13){return false;}"/>

            <h3>Species Group</h3>
            <a class="right" href="javascript:void(0);" onclick="jQuery('#speciesGroupTable tbody tr').remove();">Remove All Groups</a>
            <table id="speciesGroupTable" class="datatable">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Delete</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${taxonGroupSet}" var="group">
                        <tr>
                            <td>
                                <c:out value="${group.name}"/>
                                <input type="hidden" name="speciesGroup" value="${group.id}"/>
                            </td>
                            <td class="textcenter">
                                <a href="javascript: void(0);" onclick="jQuery(this).parents('tr').remove();">
                                    <img src="${pageContext.request.contextPath}/images/icons/delete.png" alt="Delete"/>
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>

        </div>
        <div class="speciesListChooser ALL_SPECIES" style="display: none;">
            <h2></h2>
            <p>All species will be added automatically when saved.</p>
        </div>
    </div>

    <div class="textright buttonpanel">
        <input type="submit" class="form_action" value="Save"/>
        <input type="submit" class="form_action" name="saveAndContinue" value="Save And Continue"/>
    </div>
</form>

<script type="text/javascript">
    jQuery(function() {

        jQuery('[name=speciesListType]').change(function(event) {

            var initLookup = {
                ONE_SPECIES : function() {
                    jQuery(".ONE_SPECIES").find("h2").text("<%=SpeciesListType.ONE_SPECIES.getName()%>");
                    // Delete all but the first row (if there is one)
                    var tbody = jQuery("#speciesTable tbody");
                    var first = tbody.find("tr:first");
                    tbody.children().not(first).remove();
                },
                MANY_SPECIES : function() {
                    jQuery(".MANY_SPECIES").find("h2").text("<%=SpeciesListType.MANY_SPECIES.getName()%>");
                },
                SPECIES_GROUP : function() {
                    jQuery(".SPECIES_GROUP").find("h2").text("<%=SpeciesListType.SPECIES_GROUP.getName()%>");
                },
                ALL_SPECIES : function() {
                    jQuery(".ALL_SPECIES").find("h2").text("<%=SpeciesListType.ALL_SPECIES.getName()%>");
                }
            };
            var listType = jQuery("[name=speciesListType]:checked").val();

            var chooserElems = jQuery(".speciesListChooser");
            chooserElems.hide();

            // Perform any initialisation before revealing the div.
            initLookup[listType]();
            chooserElems.filter("."+listType).show();
        });

        jQuery("[name=speciesListType]").trigger('change');
    });

    jQuery("#species_search").autocomplete({
        source: function(request, callback) {
            var params = {};
            params.q = request.term;

            jQuery.getJSON('${pageContext.request.contextPath}/webservice/taxon/searchTaxon.htm', params, function(data, textStatus) {
                var label;
                var result;
                var taxon;
                var resultsArray = [];
                for(var i=0; i<data.length; i++) {
                    taxon = data[i];

                    label = [];
                    if(taxon.scientificName !== undefined && taxon.scientificName.length > 0) {
                        label.push("<b><i>"+taxon.scientificName+"</b></i>");
                    }
                    if(taxon.commonName !== undefined && taxon.commonName.length > 0) {
                        label.push(taxon.commonName);
                    }

                    label = label.join(' ');

                    resultsArray.push({
                        label: label,
                        value: taxon.scientificName,
                        data: taxon
                    });
                }

                callback(resultsArray);
            });
        },
        select: function(event, ui) {
            var taxon = ui.item.data;
            // Taxon already added. No need to add twice.
            var hasTaxon = jQuery("[name=species][value="+taxon.id+"]").length > 0;
            var listType = jQuery("[name=speciesListType]:checked").val();
            var tbody = jQuery("#speciesTable tbody");
            var rowCount = tbody.find("tr").length;

            var canAdd = ('ONE_SPECIES' === listType && rowCount === 0) || ('ONE_SPECIES' !== listType);

            // Debug
            // hasTaxon = false;

            if(hasTaxon === false && canAdd === true) {
                var scientificName = jQuery("<td></td>").text(taxon.scientificName);
                var pk = jQuery("<input/>").attr({
                                   'type': 'hidden',
                                   'value': taxon.id,
                                   'name': 'species'
                               });
                scientificName.append(pk);
                var commonName = jQuery("<td></td>").text(taxon.commonName);
                var del = jQuery("<td></td>").addClass("textcenter");
                var delLink = jQuery('<a><img src="${pageContext.request.contextPath}/images/icons/delete.png" alt="Delete"/></a>');
                delLink.attr("href", "javascript: void(0);").click(function () {
                    jQuery(this).parents("tr").remove();
                });
                del.append(delLink);

                var row = jQuery("<tr></tr>").append(scientificName).append(commonName).append(del);
                tbody.append(row);
            }

            jQuery(event.target).select();
            return false;
        },
        minLength: 2,
        delay: 300
    });

    jQuery("#speciesGroup_search").autocomplete({
        source: function(request, callback) {
            var params = {};
            params.q = request.term;

            jQuery.getJSON('${pageContext.request.contextPath}/webservice/taxon/searchTaxonGroup.htm', params, function(data, textStatus) {
                var label;
                var result;
                var taxonGroup;
                var resultsArray = [];
                for(var i=0; i<data.length; i++) {
                    taxonGroup = data[i];
                    resultsArray.push({
                        label: taxonGroup.name,
                        value: taxonGroup.name,
                        data: taxonGroup
                    });
                }

                callback(resultsArray);
            });
        },
        select: function(event, ui) {
            var taxonGroup = ui.item.data;
            // Taxon Group already added. No need to add twice.
            var hasTaxonGroup = jQuery("[name=speciesGroup][value="+taxonGroup.id+"]").length > 0;
            var listType = jQuery("[name=speciesListType]:checked").val();
            var tbody = jQuery("#speciesGroupTable tbody");

            // Debug
            // hasTaxonGroup = false;

            if(hasTaxonGroup === false) {
                var name = jQuery("<td></td>").text(taxonGroup.name);
                var pk = jQuery("<input/>").attr({
                   'type': 'hidden',
                   'value': taxonGroup.id,
                   'name': 'speciesGroup'
                });
                name.append(pk);
                var del = jQuery("<td></td>").addClass("textcenter");
                var delLink = jQuery('<a><img src="${pageContext.request.contextPath}/images/icons/delete.png" alt="Delete"/></a>');
                delLink.attr("href", "javascript: void(0);").click(function () {
                    jQuery(this).parents("tr").remove();
                });
                del.append(delLink);

                var row = jQuery("<tr></tr>").append(name).append(del);
                tbody.append(row);
            }

            jQuery(event.target).select();
            return false;
        },
        minLength: 2,
        delay: 300
    });

</script>