<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@page import="java.awt.Color"%>

<form id="record_filter_form" method="get" action="${pageContext.request.contextPath}/map/addRecordBaseMapLayer.htm" onsubmit="bdrs.map.addRecordLayerHandler('#record_filter_form', '#record_map_layers', '#record_base_map_hover_tip');return false;">
    <h2>View Records on a Map</h2>
    <div class="input_container">
        <p>
            Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nam pharetra
            eleifend lectus non luctus. Fusce est massa, vestibulum id dignissim
            a, ullamcorper non augue. Pellentesque ac libero enim, vel euismod
            massa. Praesent et felis sed erat malesuada malesuada. Donec
            ullamcorper augue vitae arcu tristique cursus.
        </p>

        <table id="record_map_filter_table" class="form_table">
            <thead>
                <tr>
                    <th colspan="2">Filter</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <th>
                        <label for="layer_name">Layer Name</label>
                    </th>
                    <td>
                        <input type="text" name="layer_name" id="layer_name" class="validate(required)" value="Untitled Layer"/>
                    </td>
                </tr>
                <tr>
                    <th>
                        <label for="placemark_color">Point Colour</label>
                    </th>
                    <td>
                        <%
                            Color c = Color.getHSBColor((float)Math.random(), 1.0f, 1.0f);
                            String hexColor = Integer.toHexString( c.getRGB() & 0x00ffffff );
                        %>
                        <input type="text" name="placemark_color" id="placemark_color" class="colorpicker validate(required)" value="<%=hexColor%>"/>
                    </td>
                </tr>
                <tr>
                    <th>
                        <label for="species">Species</label>
                    </th>
                    <td>
                        <input type="text" name="species" id="species" placeholder="Search Scientific Names"/>
                    </td>
                </tr>
                <tr>
                    <th>
                        <label for="user">User</label>
                    </th>
                    <td>
                        <select name="user" id="user">
                            <option value="0" selected="selected">----------</option>
                            <c:forEach items="${users}" var="user">
                                <c:choose>
                                    <c:when test="${not empty user.firstName and not user.lastName}">
                                        <option value="${user.id}">
                                            <c:out value="${user.firstName}"/>&nbsp;<c:out value="${user.lastName}"/>&nbsp;
                                        </option>
                                    </c:when>
                                    <c:otherwise>
                                        <option value="${user.id}">
                                            <c:out value="${user.name}"/>
                                        </option>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                        </select>
                    </td>
                </tr>
                <tr>
                    <th>
                        <label for="group">Group</label>
                    </th>
                    <td>
                        <select name="group" id="group">
                            <option value="0" selected="selected">----------</option>
                            <c:forEach items="${groups}" var="group">
                                <option value="${group.id}">
                                    <c:out value="${group.name}"/>
                                </option>
                            </c:forEach>
                        </select>
                    </td>
                </tr>
                <tr>
                    <th>
                        <label for="survey">Survey</label>
                    </th>
                    <td>
                        <select name="survey" id="survey">
                            <option value="0" selected="selected">----------</option>
                            <c:forEach items="${surveys}" var="survey">
                                <option value="${survey.id}">
                                    <c:out value="${survey.name}"/>
                                </option>
                            </c:forEach>
                        </select>
                    </td>
                </tr>
                <tr>
                    <th>
                        <label for="taxon_group">Taxon Group</label>
                    </th>
                    <td>
                        <select name="taxon_group" id="taxon_group">
                            <option value="0" selected="selected">----------</option>
                            <c:forEach items="${taxonGroups}" var="taxonGroup">
                                <option value="${taxonGroup.id}">
                                    <c:out value="${taxonGroup.name}"/>
                                </option>
                            </c:forEach>
                        </select>
                    </td>
                </tr>
                <tr>
                    <th rowspan="2">
                        <label for="date_start">Date Range</label>
                    </th>
                    <td>
                        <input type="text" name="date_start" id="date_start" class="datepicker" placeholder="From"/>
                    </td>
                </tr>
                <tr>
                    <td>
                        <input type="text" name="date_end" id="date_end" class="datepicker" placeholder="To"/>
                    </td>
                </tr>
            </tbody>
        </table>

        <table id="record_map_layers_table" class="form_table">
            <thead>
                <tr>
                    <th>Layers</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>
                        <select id="record_map_layers" size="11"></select>
                    </td>
                </tr>
                <tr id="layer_actions">
                    <td>
                        <a href="javascript: bdrs.map.downloadKML('#record_filter_form', '#record_map_layers');">Download&nbsp;KML</a>
                        <input type="button" value="Clear" class="form_action" onclick="bdrs.map.clearAllLayers('#record_map_layers');"/>
                        <input type="button" value="Remove" class="form_action" onclick="bdrs.map.removeLayer('#record_map_layers');"/>
                    </td>
                </tr>
            </tbody>
        </table>

        <div class="float_divider"></div>

    </div>
    <div class="page_action_button_panel">
        <input class="form_action" type="submit" value="Add Layer"/>
    </div>
</form>

<div id="record_base_map_hover_tip">&nbsp;</div>
<div class="map_wrapper" id="map_wrapper">
    <div id="record_base_map" class="defaultmap tracker_map"></div>
    <div id="geocode" class="geocode"></div>
</div>

<script type="text/javascript">
    jQuery(function() {
        bdrs.map.initBaseMap('record_base_map', { geocode: { selector: '#geocode' }});
        //jQuery(".colorpicker").jPicker({images: { clientPath: '${pageContext.request.contextPath}/images/jpicker/' }});
    });

</script>