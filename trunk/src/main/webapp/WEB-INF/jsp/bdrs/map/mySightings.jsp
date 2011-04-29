<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@page import="java.awt.Color"%>
<jsp:useBean id="context" scope="request" type="au.com.gaiaresources.bdrs.servlet.RequestContext"></jsp:useBean>

<h1>My Sightings</h1>
<p>
    Filter your records by
</p>

<form id="record_filter_form" method="get" action="${pageContext.request.contextPath}/map/addRecordBaseMapLayer.htm">
    <!--<input type="hidden" name="user" value="<%= context.getUser().isAdmin() ? 0 : context.getUser().getId() %>"/>-->
    <input type="hidden" name="group" value="0"/>
    <input type="hidden" name="species" value=""/>
    <input type="hidden" name="taxon_group" value="0"/>
    <input type="hidden" name="layer_name" value="All Records"/>
    <input type="hidden" name="ident" value="<%= context.getUser().getRegistrationKey() %>"/>

    <table id="sightingFilterTable">
        <thead>
            <tr>
                <th><label for="project">Project:</label></th>
                <th><label for="date_start">From:</label></th>
                <th><label for="date_end">To:</label></th>
                <th><label for="limit">Limit:</label></th>
                <th></th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>
                    <select id="project" name="survey">
                        <c:forEach items="${surveyList}" var="survey">                            
                            <option value=${survey.id}
                            <c:if test="${survey.id == defaultSurveyId}">
                                selected="selected"</c:if>
                            >
                                <c:out value="${survey.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </td>
                <td>
                    <input id="date_start" class="datepicker_historical" type="text" name="date_start"/>
                </td>
                <td>
                    <input id="date_end" class="datepicker_historical" type="text" name="date_end"/>
                </td>
                <td>
                    <input id="limit" class="validate(integer)" type="text" name="limit" value="300"/>
                </td>
                <td>
                    <input type="submit" value="Load Records" class="form_action"/>
                </td>
            </tr>
        </tbody>
    </table>

    <c:if test="${not empty recordDateList}">
        <div class="textright">
            <label for="date_start">Select a Date:</label>
            <select id="date_start" name="date_start">
                <c:forEach var='date' items='${recordDateList}'>
                    <option value="<fmt:formatDate pattern="dd MMM yyyy" value="${date}"/>">
                        <fmt:formatDate pattern="dd MMM yyyy" value="${date}"/>
                    </option>
                </c:forEach>
            </select>
            <input type="hidden" value="<fmt:formatDate pattern="dd MMM yyyy" value="${recordDateList[0]}"/>" name="date_end"/>
        </div>
    </c:if>

</form>

<h3>Map</h3>
<div class="clear">
    <a id="mapToggle" class="left" href="javascript:void(0);">
        Collapse
    </a>
    <a class="right" href="javascript: bdrs.map.downloadKML('#record_filter_form', null);">
        Download KML
    </a>
</div>
<div class="map_wrapper clear" id="map_wrapper">
    <div id="record_base_map" class="defaultmap tracker_map"></div>
    <div class="recordCount textright"></div>
</div>


<div class="clear"></div>

<h3>List</h3>
<div class="clear">
    <a id="listToggle" class="left" href="javascript:void(0);">
        Collapse
    </a>
    <a id="xlsDownload" class="right" href="javascript:void(0);" onclick="bdrs.downloadXls(this);return false;">
        Download XLS
    </a>
</div>
<div class="list_wrapper clear">
    <table id="recordTable" class="datatable">
        <thead>
            <tr>
                <th>Date</th>
                <th>Species</th>
                <th>Location</th>
                <th>Number</th>
                <th>Notes</th>
            </tr>
        </thead>
        <tbody>
        </tbody>
    </table>
    <div class="recordCount textright"></div>
</div>


<div class="clear"></div>

<script type="text/javascript">

    // Download XLS
    bdrs.downloadXls = function(elem) {
        var xlsURL = jQuery(elem).data("xlsURL");
        if(xlsURL !== null && xlsURL !== undefined && xlsURL.length > 0) {
            window.document.location = xlsURL;
        } else {
            return false;
        }
    };

    // List Populating
    bdrs.populateList = function(formIdSelector, recordTableSelector) {
        // Get data from the webservice and populate the table
        var form = jQuery(formIdSelector);
        var url = "${pageContext.request.contextPath}/webservice/record/searchRecords.htm?"
        url = url + form.serialize();

        // Cache the URL for XLS download
        var xlsURL = "${pageContext.request.contextPath}/webservice/record/downloadRecords.htm?";
        xlsURL = xlsURL + form.serialize();
        jQuery("#xlsDownload").data("xlsURL", xlsURL);
        jQuery.getJSON(url, function(data) {
            var table = jQuery(recordTableSelector);
            var tbody = table.find("tbody");
            tbody.children().remove();

            var rec;
            var row;
            var when;
            var dateCell;
            var dateLink;
            var speciesCell;
            var locationCell;
            var numberCell;
            var notesCell;
            var taxaLookup = {};
            for(var i=0; i<data.length; i++) {
                rec = data[i];
                row = jQuery("<tr></tr>");

                when = new Date(rec.when);
                dateLink = jQuery("<a></a>").attr("href", "${pageContext.request.contextPath}/bdrs/user/surveyRenderRedirect.htm?surveyId="+rec.survey+"&recordId="+rec.id);
                dateLink.text(bdrs.util.formatDate(when));
                dateCell = jQuery("<td></td>").addClass("nowrap").append(dateLink);
                speciesCell = jQuery("<td></td>").addClass("nowrap").attr("id", "record_"+rec.id).addClass("taxon_"+rec.species);
                locationCell = jQuery("<td></td>").addClass("nowrap").text(rec.latitude+", "+rec.longitude);
                numberCell = jQuery("<td></td>").addClass("nowrap").text(rec.number == null ? "" : rec.number);
                notesCell = jQuery("<td></td>").text(rec.notes);

                row.append(dateCell).append(speciesCell).append(locationCell).append(numberCell).append(notesCell);
                tbody.append(row);

                if(taxaLookup[rec.species] === undefined) {
                    jQuery.getJSON("${pageContext.request.contextPath}/webservice/taxon/getTaxonById.htm",
                        {"id": rec.species}, function(taxon) {
                        jQuery(".taxon_"+taxon.id).addClass("scientificName").append(taxon.scientificName);
                    });
                    // Add an entry to the map. This taxon has been requested.
                    taxaLookup[rec.species] = rec.species;
                }
            }
            var mapRecordCount = jQuery('.recordCount');
            var txt = [data.length];
            txt.push(' ');
            txt.push(data.length == 1 ? "Record" : "Records");
            mapRecordCount.text(txt.join(' '));

        });
        return false;
    };

    jQuery(function() {
        bdrs.map.initBaseMap('record_base_map', null);
        bdrs.map.baseMap.events.register('addlayer', null, bdrs.map.addFeaturePopUpHandler);
        bdrs.map.baseMap.events.register('removeLayer', null, bdrs.map.removeFeaturePoupUpHandler);

        <c:if test="${not empty recordDateList}">
            jQuery('[name=date_start]').change(function(event) {
                jQuery("[name=date_end]").val(jQuery("[name=date_start]").val());

                var map = bdrs.map.baseMap;
                var layerArray = map.getLayersByName(jQuery('[name=layer_name]').val());
                for(var i=0; i<layerArray.length; i++) {
                    bdrs.map.removeLayerById(layerArray[i].id);
                }

                jQuery('#record_filter_form :submit').click();
            });
        </c:if>
        
        jQuery("#record_filter_form").submit(function() {
            bdrs.map.clearAllVectorLayers();

            // User Records
            var params = {
                placemark_color: '15E015',
                user: '<%= context.getUser().getId() %>',
                layer_name: 'My Records' 
            };
            bdrs.map.addRecordLayerHandler('#record_filter_form', null, params);

            <sec:authorize ifAnyGranted="ROLE_ADMIN">
                // All records
                bdrs.map.addRecordLayerHandler('#record_filter_form', null);
            </sec:authorize>
            
            bdrs.populateList('#record_filter_form', '#recordTable');
            return false;
        });        

        // Map Toggling
        jQuery("#mapToggle").click(function() {
            jQuery(".map_wrapper").slideToggle(function() {
                var canSee = jQuery(".map_wrapper").css('display') === 'none';
                jQuery("#mapToggle").text(canSee ? "Expand" : "Collapse");
            });
        });

        // List Toggling
        jQuery("#listToggle").click(function() {
            jQuery(".list_wrapper").slideToggle(function() {
                var canSee = jQuery(".list_wrapper").css('display') === 'none';
                jQuery("#listToggle").text(canSee ? "Expand" : "Collapse");
            });
        });

        jQuery("#record_filter_form").submit();
    });

</script>