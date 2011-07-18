<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@page import="au.com.gaiaresources.bdrs.model.method.Taxonomic"%>
<tiles:useAttribute name="id" ignore="true" />
<tiles:useAttribute name="name" ignore="true" />
<tiles:useAttribute name="taxonomic" ignore="true" />

<tr>
	<td class="drag_handle">
	    <input type="hidden" value="0" class="sort_weight" name="add_weight_${index}"/>
	</td>
	<td>
	    <label>${name}</label>
	    <input type="hidden" value="${id}" name="childCensusMethod" />
	    <input type="hidden" value="<c:choose><c:when test="${ isTag == true }">true</c:when><c:otherwise>false</c:otherwise></c:choose>" name="add_tag_${index}"/>
	</td>
	<td>
	    <label><c:out value="<%= ((Taxonomic)taxonomic).getName() %>"/></label>
	</td>
	<td class="textcenter">
	    <a href="javascript: void(0);" onclick="jQuery(this).parents('tr').hide().find('select, input, textarea').attr('disabled', 'disabled').removeClass(); return false;">
	        <img src="${pageContext.request.contextPath}/images/icons/delete.png" alt="Delete" class="vertmiddle"/>
	    </a>
	</td>
</tr>