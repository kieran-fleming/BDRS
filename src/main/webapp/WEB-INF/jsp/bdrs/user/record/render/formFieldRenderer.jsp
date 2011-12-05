<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<%@page import="au.com.gaiaresources.bdrs.servlet.RequestContextHolder"%>
<tiles:useAttribute name="formField" classname="au.com.gaiaresources.bdrs.controller.attribute.formfield.FormField"/>
<tiles:useAttribute name="locations" classname="java.util.Set" ignore="true"/>
<tiles:useAttribute name="errorMap" classname="java.util.Map" ignore="true"/>
<tiles:useAttribute name="valueMap" classname="java.util.Map" ignore="true"/>
<tiles:useAttribute name="editEnabled" ignore="true"/>

<c:choose>
  <c:when test="<%= formField.isDisplayFormField() %>">
     <%-- Fields for display only, such as, comments, horizontal rules, HTML, etc --%>
     <c:set var="isVisible" value="true"></c:set>
     <c:if test="<%= formField.isModerationFormField() %>">
         <c:if test="${editEnabled}">
             <c:set var="editEnabled" value="<%= RequestContextHolder.getContext().getUser().isModerator() %>"></c:set>
         </c:if>
     </c:if>
     <tr>
         <td colspan="2">
             <tiles:insertDefinition name="attributeRenderer">
                 <tiles:putAttribute name="formField" value="${formField}"/>
				 <tiles:putAttribute name="editEnabled" value="${ editEnabled }"/>
             </tiles:insertDefinition>
         </td>
     </tr>
  </c:when>
  <c:otherwise>
  <c:choose>
    <c:when test="<%= formField.isPropertyFormField() %>">
       <%-- Special Handling for Lat and Lng (Position) --%>
       <c:choose>
          <c:when test="${ 'Point' == formField.propertyName }">
          <c:if test="${not formField.hidden}">
              <tr>
                  <th>
                      <label for="latitude">Latitude</label>
                  </th>
                  <td>
                      <tiles:insertDefinition name="propertyRenderer">
                           <tiles:putAttribute name="formField" value="${formField}"/>
                           <tiles:putAttribute name="isLatitude" value="true"/>
                           <tiles:putAttribute name="errorMap" value="${ errorMap }"/>
                           <tiles:putAttribute name="valueMap" value="${ valueMap }"/>
						   <tiles:putAttribute name="editEnabled" value="${ editEnabled }"/>
                       </tiles:insertDefinition>
                  </td>
              </tr>
              </c:if>
              <c:if test="${not formField.hidden}">
              <tr>
                  <th>
                      <label for="longitude">Longitude</label>
                  </th>
                  <td>
                       <tiles:insertDefinition name="propertyRenderer">
                           <tiles:putAttribute name="formField" value="${formField}"/>
                           <tiles:putAttribute name="isLongitude" value="true"/>
                           <tiles:putAttribute name="errorMap" value="${ errorMap }"/>
                           <tiles:putAttribute name="valueMap" value="${ valueMap }"/>
						   <tiles:putAttribute name="editEnabled" value="${ editEnabled }"/>
                       </tiles:insertDefinition>
                  </td>
              </tr>
              </c:if>
          </c:when>
          <c:when test="${ 'Location' == formField.propertyName }">
            <c:if test="${not formField.hidden}">
              <c:if test="${ not empty locations }">
                  <tr>
                      <th>
                          <label for="location"><cw:validateHtml html="${ formField.description }"/></label>
                      </th>
                      <td>
                          <tiles:insertDefinition name="propertyRenderer">
                              <tiles:putAttribute name="formField" value="${ formField }"/>
                              <tiles:putAttribute name="locations" value="${ locations }"/>
                              <tiles:putAttribute name="errorMap" value="${ errorMap }"/>
                              <tiles:putAttribute name="valueMap" value="${ valueMap }"/>
							  <tiles:putAttribute name="editEnabled" value="${ editEnabled }"/>
                          </tiles:insertDefinition>
                      </td>
                  </tr>
              </c:if>
             </c:if>
          </c:when>
          <c:otherwise>
			<c:if test="${not formField.hidden}">
              <tr>
                  <th>
					<cw:validateHtml html="${ formField.description }"/>
                   </th>
                   <td>
                       <tiles:insertDefinition name="propertyRenderer">
                           <tiles:putAttribute name="formField" value="${formField}"/>
                           <tiles:putAttribute name="editEnabled" value="${ editEnabled }"/>
                       </tiles:insertDefinition>
                   </td>
               </tr>
             </c:if>
           </c:otherwise>
       </c:choose>
    </c:when>
    <c:when test="<%= formField.isAttributeFormField() %>">
        <c:set var="isVisible" value="true"></c:set>
        <c:if test="<%= formField.isModerationFormField() %>">
            <c:if test="${editEnabled}">
                <c:set var="editEnabled" value="<%= RequestContextHolder.getContext().getUser().isModerator() %>"></c:set>
            </c:if>
        </c:if>
        <tr>
            <th>
                <label for="${ formPrefix }attribute_${formField.attribute.id}">
                    <cw:validateHtml html="${formField.attribute.description}"/>
                </label>
            </th>
            <td>
                <tiles:insertDefinition name="attributeRenderer">
                    <tiles:putAttribute name="formField" value="${formField}"/>
                    <tiles:putAttribute name="editEnabled" value="${ editEnabled }"/>
                </tiles:insertDefinition>
            </td>
        </tr>
    </c:when>
</c:choose>
</c:otherwise>
</c:choose>