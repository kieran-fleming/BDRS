<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<tiles:insertDefinition name="title">
    <tiles:putAttribute name="title" value="My Locations"/>
</tiles:insertDefinition>

<cw:getContent key="user/locations/edit.vm" />

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
							<th>Latitude</th>
							<th>Longitude</th>
		                    <th>Delete</th>
		                </tr>
		            </thead>
		            <tbody>
		            </tbody>
		        </table>
		    </div>
		</div>
	    <div class="textright buttonpanel">
	        <input id="saveAndExit" type="submit" class="form_action" value="Save"/>
	    </div>
	</div>
</form>

<script type="text/javascript">

    jQuery(function() {
			
		var locationArray = new Array();	
		<c:forEach items="${locations}" var="loc" varStatus="status">
        {
			var locationJson = {
				id: '${loc.id}',
				latitude: bdrs.map.roundLatOrLon(${loc.y}),
				longitude: bdrs.map.roundLatOrLon(${loc.x}),
				name: '${loc.name}',
				wkt: '${loc.location}'
			};
			
			<c:if test="${loc.id == defaultLocationId}">
			     locationJson.defaultLocation = true;
			</c:if>
			
			locationArray.push(locationJson);
		}
        </c:forEach>
		
		bdrs.location.initLocationMapAndTable(locationArray, "#locationList tbody");
    });
</script>