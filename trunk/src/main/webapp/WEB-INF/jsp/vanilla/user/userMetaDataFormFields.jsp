<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<tiles:useAttribute name="metaList" classname="java.util.List" ignore="true"/>

<!-- Add meta data items... -->
<c:forEach items="${ metaList }" var="meta">
<jsp:useBean id="meta" type="au.com.gaiaresources.bdrs.service.user.UserMetaData"/>
<tr>
    <td class="formlabel">${meta.displayName}:</td>
    <c:if test="${meta.type == 'String'}">
    <td><input name="${meta.key}" type="text" value="<c:out value='${meta.value}'/>" size="40" style="width:25em" class="validate(${meta.validation})"/></td>
    </c:if>
    <c:if test="${meta.type == 'Boolean'}">
    <td><input name="${meta.key}" type="checkbox" value="<%= au.com.gaiaresources.bdrs.service.user.UserMetaData.TRUE %>" 
    <c:if test="<%= meta.getValue().equals(au.com.gaiaresources.bdrs.service.user.UserMetaData.TRUE) %>">checked</c:if> /></td>
    </c:if>
</tr>
</c:forEach>