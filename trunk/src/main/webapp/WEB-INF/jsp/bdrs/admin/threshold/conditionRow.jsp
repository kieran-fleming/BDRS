<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<tiles:useAttribute name="condition" classname="au.com.gaiaresources.bdrs.model.threshold.Condition" ignore="true"/>
<tiles:useAttribute name="index" ignore="true"/>

<tr class="conditionRow">
    <td class="condition">
	    <c:choose>
	        <c:when test="${ condition.id == null }">
	            <input type="hidden" name="new_condition" value="${ index }"/>
	        </c:when>
	        <c:otherwise>
	            <input type="hidden" name="condition_pk" value="${ condition.id }"/>
	        </c:otherwise>
        </c:choose>
       <c:forEach items="<%= condition.getPathDescriptorsForPath() %>" var="entry">
            <tiles:insertDefinition name="thresholdPathDescriptorRenderer">
                <tiles:putAttribute name="path_descriptor_list" value="${ entry.value }"/>
                <tiles:putAttribute name="selected_path_descriptor" value="${ entry.key }"/>
                <tiles:putAttribute name="condition" value="${ condition }"/>
            </tiles:insertDefinition>
        </c:forEach>
    </td>
    <td class="textcenter">
	    <a href="javascript: void(0);" onclick="jQuery(this).parents('.conditionRow').remove();">
	        <img src="${pageContext.request.contextPath}/images/icons/delete.png" alt="Delete" class="vertmiddle"/>
	    </a>
    </td>
</tr>