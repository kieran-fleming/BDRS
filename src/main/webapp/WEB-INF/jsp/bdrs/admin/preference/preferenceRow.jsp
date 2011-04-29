<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<tiles:useAttribute name="pref" classname="au.com.gaiaresources.bdrs.model.preference.Preference" ignore="true"/>
<tiles:useAttribute name="index" ignore="true"/>

<tr>
    <c:choose>
	    <c:when test="${ pref.id == null }">
		    <td>
		        <input type="hidden" name="add_preference" value="${ index }"/>
		        <input type="hidden" name="add_preference_category_${ index }" value="${ pref.preferenceCategory.id }"/>
		        <textarea class="validate(required)" name="add_preference_description_${ index }"><c:out value="${ pref.description }"/></textarea>
		    </td>
		    <td>
		        <input class="validate(unique(.uniqueKey)) uniqueKey" type="text" name="add_preference_key_${ index }" value="<c:out value="${ pref.key }"/>"/>
		    </td>
		    <td>
		        <input class="validate(required)" type="text" name="add_preference_value_${ index }" value="<c:out value="${ pref.value }"/>"/>
		    </td>
		    <td>
		        <a href="javascript: void(0);" onclick="jQuery(this).parents('tr').remove();">
		            <img src="${pageContext.request.contextPath}/images/icons/delete.png" alt="Delete" class="vertmiddle"/>
		        </a>
		    </td>
	    </c:when>
	    <c:otherwise>
	       <td>
                <input type="hidden" name="preference_id" value="${ pref.id }"/>
                <textarea class="validate(required)" name="preference_description_${ pref.id }"><c:out value="${ pref.description }"/></textarea>
            </td>
            <td>
                <c:choose>
                    <c:when test="${ pref.portal == null }">
                        <input class="validate(unique(.uniqueKey)) uniqueKey" type="hidden" name="preference_key_${ pref.id }" value="<c:out value="${ pref.key }"/>"/>
                        <c:out value="${ pref.key }"/>
                    </c:when>
                    <c:otherwise>
                        <input class="validate(unique(.uniqueKey)) uniqueKey" type="text" name="preference_key_${ pref.id }" value="<c:out value="${ pref.key }"/>"/>
                    </c:otherwise>
                </c:choose>
            </td>
            <td>
                <input class="validate(required)" type="text" name="preference_value_${ pref.id }" value="<c:out value="${ pref.value }"/>"/>
            </td>
            <td>
                <c:choose>
	                <c:when test="${ pref.isRequired }">
	                   <span>N/A</span>
	                </c:when>
	                <c:otherwise>
	                   <a href="javascript: void(0);" onclick="jQuery(this).parents('tr').remove();">
	                       <img src="${pageContext.request.contextPath}/images/icons/delete.png" alt="Delete" class="vertmiddle"/>
	                   </a>
	                </c:otherwise>
                </c:choose>
            </td>
	    </c:otherwise>
    </c:choose>
</tr>