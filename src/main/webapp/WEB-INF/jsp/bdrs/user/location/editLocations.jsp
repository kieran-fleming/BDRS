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
<div id="locationsContainer">
    <div class="map_wrapper locations_map" id="map_wrapper">
        <div id="base_map" class="defaultmap tracker_map"></div>
        <div id="geocode" class="geocode"></div>
    </div>
<div class="locations_container">
    <div class="locationList userlocationList">
        <table id="locationList" class="datatable locationList">
            <thead>
                <tr>
                    <th>Default</th>
                    <th>Name</th>
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
    </div>
</div>
<div class="clear buttonpanel">
    <div class="textright buttonpanel">
        <input type="submit" class="form_action" value="Save"/>
        <input type="submit" class="form_action" name="saveAndContinue" value="Save And Continue"/>
    </div>
</div>
</div>
</form>

<script type="text/javascript">
    jQuery(function() {
        bdrs.location.initLocationMapAndTable(
            '/bdrs/location/ajaxAddUserLocationRow.htm');
    });
</script>