<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<%@ page import="au.com.gaiaresources.bdrs.model.taxa.SpeciesProfile" %>

<tiles:useAttribute name="profile" classname="au.com.gaiaresources.bdrs.model.taxa.SpeciesProfile" ignore="true"/>
<tiles:useAttribute name="index" ignore="true"/>

<tr>
    <c:choose>
        <c:when test="${ profile.id == null }">
            <td class="drag_handle">
                <input type="hidden" class="sort_weight" name="new_profile_weight_${index}" value="${ profile.weight }"/>
            </td>
            <td>
                <input type="hidden" name="new_profile" value="${ index }"/>
                <select name="new_profile_type_${ index }">
                    <c:forEach var="entry" items="<%=  SpeciesProfile.SPECIES_PROFILE_TYPE_VALUES %>">
                        <option value="${ entry.key }" <c:if test="${profile.type == entry.key}">selected="selected"</c:if>><c:out value="${ entry.value }" /></option>
                    </c:forEach>
                </select>
                
            </td>
            <td>
                <input type="text" name="new_profile_header_${ index }" value="<c:out value="${ profile.header }"/>"/>
            </td>
            <td>
                <input type="text" name="new_profile_description_${ index }" value="<c:out value="${ profile.description }"/>"/>
            </td>
            <td>
                <input type="text" name="new_profile_content_${ index }" value="<c:out value="${ profile.content }"/>"/>
            </td>
            <td>
                <a href="javascript: void(0);" onclick="jQuery(this).parents('tr').remove();">
                    <img src="${pageContext.request.contextPath}/images/icons/delete.png" alt="Delete" class="vertmiddle"/>
                </a>
            </td>
        </c:when>
        <c:otherwise>
            <td class="drag_handle">
                <input type="hidden" value="0" class="sort_weight" name="profile_weight_${ profile.id }" value="${ profile.weight }"/>
            </td>
            <td>
                <input type="hidden" name="profile_pk" value="${ profile.id }"/>
                <select name="profile_type_${ profile.id }">
                    <c:forEach var="entry" items="<%=  SpeciesProfile.SPECIES_PROFILE_TYPE_VALUES %>">
                        <option value="${ entry.key }"
                            <c:if test="${ entry.key == profile.type }">  
                                selected="selected"
                            </c:if>
                        >
                            <c:out value="${ entry.value }" />
                        </option>
                    </c:forEach>
                </select>
            </td>
            <td>
                <input type="text" name="profile_header_${ profile.id }" value="<c:out value="${ profile.header }"/>"/>
            </td>
            <td>
                <input type="text" name="profile_description_${ profile.id }" value="<c:out value="${ profile.description }"/>"/>
            </td>
            <td>
                <input type="text" name="profile_content_${ profile.id }" value="<c:out value="${ profile.content }"/>"/>
            </td>
            <td>
                <a href="javascript: void(0);" onclick="jQuery(this).parents('tr').remove();">
                    <img src="${pageContext.request.contextPath}/images/icons/delete.png" alt="Delete" class="vertmiddle"/>
                </a>
            </td>
        </c:otherwise>
    </c:choose>
</tr>
