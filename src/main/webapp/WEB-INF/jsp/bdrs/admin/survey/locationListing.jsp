<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<%@page import="au.com.gaiaresources.bdrs.model.metadata.Metadata"%>
<jsp:useBean id="survey" type="au.com.gaiaresources.bdrs.model.survey.Survey" scope="request"/>

<h1>Edit Project: Locations</h1>

<cw:getContent key="admin/editProject/editLocations" />

<div class="textright buttonpanel">
	<input title="Add a location from another project." id="addExistingLocationButton" class="form_action" type="button" value="Add Existing Location" />
    <input title="Create a new location for this project." class="form_action" type="button" value="Create Location" onclick="window.document.location='${pageContext.request.contextPath}/bdrs/admin/survey/editLocation.htm?surveyId=${ survey.id }';"/>
</div>
<form method="POST" action="${pageContext.request.contextPath}/bdrs/admin/survey/locationListing.htm">
<input type="hidden" name="surveyId" value="${ survey.id }"/>
<table id="location_listing" class="datatable">
    <thead>
        <tr>
            <th>Location Name</th>
            <th>Delete</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach items="${survey.locations}" var="location">
            <jsp:useBean id="location" type="au.com.gaiaresources.bdrs.model.location.Location"/>
            <tr>
                <td class="name">
                    <input type="hidden" name="location" value="${ location.id }"/>
                    <a href="${pageContext.request.contextPath}/bdrs/admin/survey/editLocation.htm?surveyId=${ survey.id }&locationId=${ location.id }">${ location.name }</a>
                </td>
                <td class="textcenter">
                    <a id="delete_${location.id}" href="javascript: void(0);" onclick="jQuery(this).parents('tr').hide().find('select, input, textarea').attr('disabled', 'disabled'); return false;">
                        <img src="${pageContext.request.contextPath}/images/icons/delete.png" alt="Delete" class="vertmiddle"/>
                    </a>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>
<div>
        <input id="restrict_locations" 
            class="vertmiddle restrict_locations" 
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

<%-- divs used to instantitate grid and dialog --%>
<div id="surveyLocationListingDialog">
<table id="surveyLocationListingGrid"></table>
<div id="surveyLocationListingPager"></div>
</div>

<%-- files included in bdrs.js using bdrs.require --%>
<script type="text/javascript">

jQuery(function() {
    bdrs.location.createGetSurveyLocationsForUserDialogGrid('#surveyLocationListingGrid', 
															'#surveyLocationListingPager',
															'#surveyLocationListingDialog',
															'#addExistingLocationButton',
															'#location_listing tbody',
															${survey.id});
	
});

</script>
