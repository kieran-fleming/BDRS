<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<tiles:useAttribute name="portalEntryPoint" classname="au.com.gaiaresources.bdrs.model.portal.PortalEntryPoint" ignore="true"/>
<tiles:useAttribute name="index" ignore="true"/>

<tr>
	<c:choose>
	    <c:when test="${ portalEntryPoint.id == null }">
            <td>
                <input type="hidden" name="add_portalEntryPoint" value="${ index }"/>
                <input class="validate(required) fillwidth" type="text" name="add_entryPoint_pattern_${ index }" value="<c:out value="${ portalEntryPoint.pattern }"/>"/>
            </td>
            <td>
                <input class="fillwidth" type="text" name="add_entryPoint_redirect_${ index }" value="<c:out value="${ portalEntryPoint.redirect }"/>"/>
            </td>
	    </c:when>
	    <c:otherwise>
			<td>
			    <input type="hidden" name="portalEntryPoint_id" value="${ portalEntryPoint.id }"/>
			    <input class="validate(required) fillwidth" type="text" name="entryPoint_pattern_${ portalEntryPoint.id }" value="<c:out value="${ portalEntryPoint.pattern }"/>"/>
			</td>
			<td>
			    <input class="fillwidth" type="text" name="entryPoint_redirect_${ portalEntryPoint.id }" value="<c:out value="${ portalEntryPoint.redirect }"/>"/>
			</td>
	    </c:otherwise>
	</c:choose>
	<td class="textcenter">
        <a href="javascript: void(0);" onclick="jQuery(this).parents('tr').hide().find('select, input, textarea').attr('disabled', 'disabled').removeClass(); return false;">
            <img src="${pageContext.request.contextPath}/images/icons/delete.png" alt="Delete" class="vertmiddle"/>
        </a>
    </td>
</tr>
