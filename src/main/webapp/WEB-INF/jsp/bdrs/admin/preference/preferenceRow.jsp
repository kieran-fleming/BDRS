<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<tiles:useAttribute name="pref" classname="au.com.gaiaresources.bdrs.model.preference.Preference" ignore="true"/>
<tiles:useAttribute name="index" ignore="true"/>

<tr>
    <c:choose>
	    <c:when test="${ pref.id == null }">
		    <td class="description_col">
		        <input type="hidden" name="add_preference" value="${ index }"/>
		        <input type="hidden" name="add_preference_category_${ index }" value="${ pref.preferenceCategory.id }"/>
		        <textarea class="validate(required)" name="add_preference_description_${ index }"><c:out value="${ pref.description }"/></textarea>
		    </td>
		    <td class="key_col">
		        <input class="validate(unique(.uniqueKey)) uniqueKey" type="text" name="add_preference_key_${ index }" value="<c:out value="${ pref.key }"/>"/>
		    </td>
		    <td class="value_col">
		        <input class="validate(required)" type="text" name="add_preference_value_${ index }" value="<c:out value="${ pref.value }"/>"/>
		    </td>
		    <td class="delete_col">
		        <a href="javascript: void(0);" onclick="jQuery(this).parents('tr').remove();">
		            <img src="${pageContext.request.contextPath}/images/icons/delete.png" alt="Delete" class="vertmiddle"/>
		        </a>
		    </td>
	    </c:when>
	    <c:otherwise>
	       <td class="description_col">
                <input type="hidden" name="preference_id" value="${ pref.id }"/>
				
				<c:choose>
					<c:when test="${pref.locked}">
						<div>
						  <cw:validateHtml html="${ pref.description }"/>
						</div>
					</c:when>
					<c:otherwise>
						<div>
                            <textarea class="validate(required)" name="preference_description_${ pref.id }"><c:out value="${ pref.description }"/></textarea>
						</div>
					</c:otherwise>
				</c:choose>
            </td>
            <td class="key_col">			
				<c:choose>
                    <c:when test="${pref.locked}">
                        <c:out value="${ pref.key }"/>
                    </c:when>
                    <c:otherwise>
                    	<div>
                            <input class="validate(required, unique(.uniqueKey)) uniqueKey" type="text" name="preference_key_${ pref.id }" value="<c:out value="${ pref.key }"/>"/>
						</div>
                    </c:otherwise>
                </c:choose>
            </td>
            <td class="value_col">
            	<div>
                    <input class="validate(required)" type="text" name="preference_value_${ pref.id }" value="<c:out value="${ pref.value }"/>"/>
				</div>
            </td>
            <td class="delete_col">
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