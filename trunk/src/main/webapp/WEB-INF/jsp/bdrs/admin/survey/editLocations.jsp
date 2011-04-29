<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<%@page import="au.com.gaiaresources.bdrs.model.metadata.Metadata"%>
<jsp:useBean id="survey" type="au.com.gaiaresources.bdrs.model.survey.Survey" scope="request"/>

<h1>Choose Project Locations</h1>
<p>
    Click on the map below to add a location to the project.
</p>
<form method="POST" action="${pageContext.request.contextPath}/bdrs/admin/survey/editLocations.htm">
    <input type="hidden" name="surveyId" value="${survey.id}"/>

    <div class="map_wrapper" id="map_wrapper">
        <div id="base_map" class="defaultmap tracker_map"></div>
    </div>

    <table id="locationTable" class="datatable">
        <thead>
            <tr>
                <th>Name</th>
                <th>Latitude</th>
                <th>Longitude</th>
                <th>Delete</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach items="${survey.locations}" var="loc" varStatus="status">
                <tiles:insertDefinition name="locationRow">
                    <tiles:putAttribute name="location" value="${loc}"/>
                    <tiles:putAttribute name="index" value="${status.index}"/>
                </tiles:insertDefinition>
            </c:forEach>
        </tbody>
    </table>
    
    <div>
        <input id="restrict_locations" 
            class="vertmiddle" 
            name="restrict_locations" 
            type="checkbox"
            <c:if test="<%= survey.isPredefinedLocationsOnly() %>">
                checked="checked"
            </c:if>
        />
        <label for="restrict_locations" class="vertmiddle">
            Restrict record locations to this list only
        </label>
    </div>

    <div class="textright buttonpanel">
        <input type="submit" class="form_action" value="Save"/>
        <input type="submit" class="form_action" name="saveAndContinue" value="Save And Continue"/>
    </div>
</form>

<script type="text/javascript">
    jQuery(function() {
        bdrs.location.initLocationMapAndTable();
    });
</script>