<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<%@page import="au.com.gaiaresources.bdrs.model.record.Record"%>
<%@page import="au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordPropertyFormField"%>
<%@page import="au.com.gaiaresources.bdrs.controller.attribute.AttributeFormField"%>
<%@page import="au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordAttributeFormField"%>
<%@page import="au.com.gaiaresources.bdrs.model.taxa.AttributeType"%>

<jsp:useBean id="survey" scope="request" type="au.com.gaiaresources.bdrs.model.survey.Survey" />
<c:set var="index" value="0" scope="page"></c:set>
<jsp:useBean id="index" type="java.lang.String" />
<c:forEach items="<%= survey.getSpecies() %>" var="surveySpecies">
    <jsp:useBean id="surveySpecies" type="au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies" />
    <tr>
        <td>
           <%= surveySpecies.getScientificName() %>
           <input type="hidden" name="${index }_species" value="<%= surveySpecies.getId() %>"/>
        </td>
        <c:forEach items="${ formFieldList }" var="formField">
            <jsp:useBean id="formField" type="au.com.gaiaresources.bdrs.controller.attribute.formfield.AbstractRecordFormField" />
                <c:choose>
                    <c:when test="<%= formField.isPropertyFormField() && !Record.RECORD_PROPERTY_SPECIES.equals(((RecordPropertyFormField)formField).getPropertyName()) %>">
                        <td>
                            <tiles:insertDefinition name="propertyRenderer">
                                <tiles:putAttribute name="formField" value="${ formField }"/>
                                <tiles:putAttribute name="formPrefix" value="${ index }_"/>
                            </tiles:insertDefinition>
                        </td>
                    </c:when>
                    <c:when test="<%= formField.isAttributeFormField() %>">
                        <td>
                            <tiles:insertDefinition name="attributeRenderer">
                                <tiles:putAttribute name="formField" value="${ formField }"/>
                                <tiles:putAttribute name="formPrefix" value="${ index }_"/>
                            </tiles:insertDefinition>
                        </td>
                    </c:when>
                </c:choose>
       </c:forEach>
   </tr>
   <c:set var="index" value="${index + 1}"></c:set>
</c:forEach>
