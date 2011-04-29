<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<tiles:useAttribute name="path_descriptor_list" classname="java.util.List" ignore="true"/>
<tiles:useAttribute name="selected_path_descriptor" classname="au.com.gaiaresources.bdrs.model.threshold.PathDescriptor" ignore="true"/>
<tiles:useAttribute name="condition" classname="au.com.gaiaresources.bdrs.model.threshold.Condition" ignore="true"/>
<tiles:useAttribute name="index" ignore="true"/>

<c:choose>
    <c:when test="${ condition.id == null }">
		<select class="pathSelect" onchange="bdrs.threshold.handlers.changePropertyPath('#class_name', this, ${ index }, null);">
		    <c:forEach items="${ path_descriptor_list }" var="add_path_descriptor">
		        <jsp:useBean id="add_path_descriptor" type="au.com.gaiaresources.bdrs.model.threshold.PathDescriptor" />
		        <option value="${ add_path_descriptor.propertyPath }"
		            <c:if test="${ add_path_descriptor == selected_path_descriptor }">  
		                selected="selected"
		            </c:if>
		        >
		            <c:out value="<%= add_path_descriptor.getPropertyDescriptor().getName() %>"/>
		        </option>
		    </c:forEach>
		</select>
    </c:when>
    <c:otherwise>
        <select class="pathSelect" onchange="bdrs.threshold.handlers.changePropertyPath('#class_name', this, null, ${ condition.id });">
            <c:forEach items="${ path_descriptor_list }" var="path_descriptor">
                <jsp:useBean id="path_descriptor" type="au.com.gaiaresources.bdrs.model.threshold.PathDescriptor" />
                <option value="${ path_descriptor.propertyPath }"
                    <c:if test="${ path_descriptor == selected_path_descriptor }">  
                        selected="selected"
                    </c:if>
                >
                    <c:out value="<%= path_descriptor.getPropertyDescriptor().getName() %>"/>
                </option>
            </c:forEach>
        </select>
    </c:otherwise>
</c:choose>