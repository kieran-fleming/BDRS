<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@page import="au.com.gaiaresources.bdrs.model.location.Location"%>

<tiles:useAttribute name="location" classname="au.com.gaiaresources.bdrs.model.location.Location" ignore="true"/>
<tiles:useAttribute name="defaultLocation" classname="java.lang.Integer" ignore="true"/>
<tiles:useAttribute name="index" ignore="true"/>

<c:choose>
    <c:when test="${ location == null }">
        <tr>
            <td>
                <input type="radio" name="defaultLocationId" value="index_${ index }"/>
            </td>
            <td>
                <input type="text" name="add_name_${index}" class="location_name deferred_ketchup(required)"/>
                <input type="hidden" name="add_location" value="${index}"/>
            </td>
            <td>
                <input type="text" name="add_latitude_${index}" class="location_lat deferred_ketchup(range(-90, 90), number)"/>
            </td>
            <td>
                <input type="text" name="add_longitude_${index}" class="location_lon deferred_ketchup(range(-180, 180), number)"/>
            </td>
            <td class="textcenter">
                <a id="delete_${index}" href="javascript: void(0);" onclick="jQuery(this).parents('tr').hide().find('select, input, textarea').attr('disabled', 'disabled'); return false;">
                    <img src="${pageContext.request.contextPath}/images/icons/delete.png" alt="Delete" class="vertmiddle"/>
                </a>
            </td>
        </tr>
    </c:when>
    <c:otherwise>
        <tr>
            <td>
                <input type="radio" name="defaultLocationId" value="id_${ location.id }"
                    <c:if test="${ location.id == defaultLocationId }">
                        checked="checked"
                    </c:if> 
                />
            </td>
            <td>
                <input type="text" name="name_${location.id}" class="location_name deferred_ketchup(required)" value="${location.name}"/>
                <input type="hidden" name="location" value="${location.id}"/>
            </td>
            <td>
                <input type="text" name="latitude_${location.id}" value="<%= location.getLocation().getY() %>" class="location_lat deferred_ketchup(range(-90, 90), number)"/>
            </td>
            <td>
                <input type="text" name="longitude_${location.id}" value="<%= location.getLocation().getX() %>" class="location_lon deferred_ketchup(range(-180, 180), number)"/>
            </td>
            <td class="textcenter">
                <a id="delete_${location.id}" href="javascript: void(0);" onclick="jQuery(this).parents('tr').hide().find('select, input, textarea').attr('disabled', 'disabled'); return false;">
                    <img src="${pageContext.request.contextPath}/images/icons/delete.png" alt="Delete" class="vertmiddle"/>
                </a>
            </td>
        </tr>
    </c:otherwise>
</c:choose>
