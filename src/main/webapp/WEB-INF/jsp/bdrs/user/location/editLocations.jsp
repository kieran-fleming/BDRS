<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<h1>Choose User Locations</h1>
<p>
    Click on the map below to add a location for this user.
</p>
<form method="POST" action="${pageContext.request.contextPath}/bdrs/location/editUserLocations.htm">
    <c:if test="${redirect != null}">
        <input type="hidden" name="redirect" value="${redirect}"/>
    </c:if>
    <div class="map_wrapper" id="map_wrapper">
        <div id="base_map" class="defaultmap tracker_map"></div>
        <div id="geocode" class="geocode"></div>
    </div>

    <table id="locationTable" class="datatable">
        <thead>
            <tr>
                <th>Default</th>
                <th>Name</th>
                <th>Latitude</th>
                <th>Longitude</th>
                <th>Delete</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach items="${locations}" var="loc" varStatus="status">
                <tiles:insertDefinition name="userLocationRow">
                    <tiles:putAttribute name="location" value="${loc}"/>
                    <tiles:putAttribute name="index" value="${status.index}"/>
                </tiles:insertDefinition>
            </c:forEach>
        </tbody>
    </table>

    <div class="textright buttonpanel">
        <input type="submit" class="form_action" value="Save"/>
        <input type="submit" class="form_action" name="saveAndContinue" value="Save And Continue"/>
    </div>
</form>

<script type="text/javascript">
    jQuery(function() {
        bdrs.location.initLocationMapAndTable('/bdrs/location/ajaxAddUserLocationRow.htm');
    });
</script>