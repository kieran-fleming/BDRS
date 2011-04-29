<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<tr>
	<c:forEach items="${ formFieldList }" var="formField">
	    <jsp:useBean id="formField" type="au.com.gaiaresources.bdrs.controller.attribute.formfield.AbstractRecordFormField" />
        <td>
            <c:choose>
                <c:when test="<%= formField.isPropertyFormField() %>">
                    <tiles:insertDefinition name="propertyRenderer">
                       <tiles:putAttribute name="formField" value="${ formField }"/>
                   </tiles:insertDefinition>
                </c:when>
                <c:when test="<%= formField.isAttributeFormField() %>">
                    <tiles:insertDefinition name="attributeRenderer">
                        <tiles:putAttribute name="formField" value="${ formField }"/>
                    </tiles:insertDefinition>
                </c:when>    
            </c:choose>
        </td>
	</c:forEach>
</tr>
