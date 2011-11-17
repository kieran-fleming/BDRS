<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<tiles:useAttribute name="formField" classname="au.com.gaiaresources.bdrs.controller.attribute.formfield.FormField"/>
<tiles:useAttribute name="locations" classname="java.util.Set" ignore="true"/>
<tiles:useAttribute name="errorMap" classname="java.util.Map" ignore="true"/>
<tiles:useAttribute name="valueMap" classname="java.util.Map" ignore="true"/>

<div class="verticalFormField">
<c:choose>
  <c:when test="<%= formField.isDisplayFormField() %>">
     <%-- Fields for display only, such as, comments, horizontal rules, HTML, etc --%>
             <tiles:insertDefinition name="attributeRenderer">
                 <tiles:putAttribute name="formField" value="${formField}"/>
             </tiles:insertDefinition>
  </c:when>
  <c:otherwise>
  <c:choose>
    <c:when test="<%= formField.isPropertyFormField() %>">
       <%-- Special Handling for Lat and Lng (Position) --%>
       <c:choose>
          <c:when test="${ 'point' == formField.propertyName }">
              <label for="latitude">Latitude</label>
              <tiles:insertDefinition name="propertyRenderer">
                  <tiles:putAttribute name="formField" value="${formField}"/>
                  <tiles:putAttribute name="isLatitude" value="true"/>
                  <tiles:putAttribute name="errorMap" value="${ errorMap }"/>
                  <tiles:putAttribute name="valueMap" value="${ valueMap }"/>
              </tiles:insertDefinition>
              <label for="longitude">Longitude</label>
              <tiles:insertDefinition name="propertyRenderer">
                  <tiles:putAttribute name="formField" value="${formField}"/>
                  <tiles:putAttribute name="isLongitude" value="true"/>
                  <tiles:putAttribute name="errorMap" value="${ errorMap }"/>
                  <tiles:putAttribute name="valueMap" value="${ valueMap }"/>
              </tiles:insertDefinition>
          </c:when>
          <c:when test="${ 'location' == formField.propertyName }">
              <c:if test="${ not empty locations }">
                  <label for="location">Location</label>
                  <tiles:insertDefinition name="propertyRenderer">
                      <tiles:putAttribute name="formField" value="${ formField }"/>
                      <tiles:putAttribute name="locations" value="${ locations }"/>
                      <tiles:putAttribute name="errorMap" value="${ errorMap }"/>
                      <tiles:putAttribute name="valueMap" value="${ valueMap }"/>
                  </tiles:insertDefinition>
              </c:if>
          </c:when>
          <c:otherwise>
              ${ formField.description }
             <tiles:insertDefinition name="propertyRenderer">
                 <tiles:putAttribute name="formField" value="${formField}"/>
             </tiles:insertDefinition>
          </c:otherwise>
       </c:choose>
    </c:when>
    <c:when test="<%= formField.isAttributeFormField() %>">
        <label for="${ formPrefix }attribute_${formField.attribute.id}">
            <c:out value="${formField.attribute.description}"/>
        </label>
        <tiles:insertDefinition name="attributeRenderer">
            <tiles:putAttribute name="formField" value="${formField}"/>
        </tiles:insertDefinition>
    </c:when>
</c:choose>
</c:otherwise>
</c:choose>
</div>