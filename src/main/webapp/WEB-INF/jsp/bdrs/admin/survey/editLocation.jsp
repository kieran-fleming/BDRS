<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<%@page import="au.com.gaiaresources.bdrs.model.metadata.Metadata"%>

<%@page import="java.util.Set"%>
<%@page import="java.util.HashSet"%>
<%@page import="au.com.gaiaresources.bdrs.model.location.Location"%>
<jsp:useBean id="location" type="au.com.gaiaresources.bdrs.model.location.Location" scope="request"/>
<jsp:useBean id="survey" type="au.com.gaiaresources.bdrs.model.survey.Survey" scope="request"/>

<h1>Edit Location</h1>

<form method="POST" action="${pageContext.request.contextPath}/bdrs/admin/survey/editLocation.htm" enctype="multipart/form-data">
    <input type="hidden" name="surveyId" value="${survey.id}"/>
    <c:if test="${location != null}">
        <input type="hidden" name="locationId" value="${location.id}"/>
    </c:if>
<div id="locationsContainer" class="input_container locationsContainer">
  <div id="locationMetadata">
      <table>
        <tr>
          <th><label class="textright" for="locationName">Site Name</label></th>
          <td>
            <input type="text" name="locationName" id="locationName" class="locationMetadata validate(required)" 
                <c:if test="${location != null}">value="<c:out value="${location.name}"/>"</c:if>
            />
          </td>
        </tr>
        <tr>
          <th><label class="textright" for="locationDescription">Site Description</label></th>
          <td>
            <textarea maxlength="255" rows="2" name="locationDescription" id="locationDescription" class="locationMetadata"><c:if test="${location != null}"><c:out value="${description}"/></c:if></textarea>
          </td>
        </tr>
      </table>
  </div>
  <div class="clear"></div>
  <div class="locations_container attributesContainer form_table locationAttributes">
<tiles:insertDefinition name="locationEntryMap">
    <tiles:putAttribute name="survey" value="${survey}"/>
</tiles:insertDefinition>
                <input type="hidden" name="location_WKT"  />
                <table>
                  <tr>
                    <th><label class="right textright" for="latitude">Latitude</label></th>
                    <td>
                        <input type="text" name="latitude" id="latitude" class="validate(range(-90,90), number)" />
                    </td>
                  </tr>
                  <tr>
                    <th><label class="right textright" for="longitude">Longitude</label></th>
                    <td>
                        <input type="text" name="longitude" id="longitude" class="validate(range(-180,180), number)" />
                    </td>
                  </tr>
                  <tr>
                    <th><label class="right textright" for="locationArea">Site Area (Ha)</label></th>
                    <td>
                        <input type="text" name="locationArea" readonly="readonly" />
                    </td>
                  </tr>
            </table>
        <!-- create a set for the location -->
        <c:forEach items="${locationFormFieldList}" var="formField">
                <tiles:insertDefinition name="formFieldVerticalRenderer">
                    <tiles:putAttribute name="formField" value="${ formField }"/>
                    <tiles:putAttribute name="errorMap" value="${ errorMap }"/>
                    <tiles:putAttribute name="valueMap" value="${ valueMap }"/>
                </tiles:insertDefinition>
            </c:forEach>
</div>
<div class="clear"></div>
<div class="buttonpanel">
    <div class="textright buttonpanel">
        <input type="submit" class="form_action" name="goback" value="Go Back" onclick="window.document.location='${pageContext.request.contextPath}/bdrs/admin/survey/locationListing.htm?surveyId=${survey.id}'"/>
        <input type="submit" class="form_action" name="save" value="Save"/>
    </div>
</div>
</div>
</form>