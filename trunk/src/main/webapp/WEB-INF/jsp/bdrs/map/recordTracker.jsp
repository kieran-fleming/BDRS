<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@page import="java.awt.Color"%>

<c:choose>
    <c:when test="${pageContext.request.parameterMap['page'][0] == 'recentsightings'}">
        <h1>Last 300</h1>
        <p>
            The map below shows you the last 300 sightings that have been recorded in the system.
        </p>
    </c:when>
    <c:when test="${pageContext.request.parameterMap['page'][0] == 'datesightings'}">
        <h1>By Date</h1>
        <p>
            The map below shows you the sightings on the date you have chosen from the drop &#8211; down list.
        </p>
    </c:when>
</c:choose>

<form id="record_filter_form" method="get" action="${pageContext.request.contextPath}/map/addRecordBaseMapLayer.htm" onsubmit="bdrs.map.addRecordLayerHandler('#record_filter_form', '#record_map_layers');return false;">
    <c:forEach var='item' items='${pageContext.request.parameterMap}'>
        <input type="hidden" name="${item.key}" value="${item.value[0]}"/>
    </c:forEach>

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

    <input type="submit" value="Submit" class="form_action" style="display:none"/>
</form>

<div class="map_wrapper" id="map_wrapper">
    <div id="record_base_map" class="defaultmap tracker_map"></div>
</div>

<script type="text/javascript">
    jQuery(function() {
        bdrs.map.initBaseMap('record_base_map', 'record_map_layers');
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

        jQuery('#record_filter_form :submit').click();

    });

</script>