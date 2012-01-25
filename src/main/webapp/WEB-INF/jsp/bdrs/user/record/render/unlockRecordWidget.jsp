<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<tiles:useAttribute name="surveyId" />
<tiles:useAttribute name="recordId" />

<a title="unlock form for editing" href="${pageContext.request.contextPath}/bdrs/user/surveyRenderRedirect.htm?surveyId=${surveyId}&recordId=${recordId}&editForm=true
<%-- for forms with multiple tabs which is currently only the tracker form --%>
<c:if test="${!empty selectedTab}">
	&selectedTab=${selectedTab}
</c:if>
"
>Unlock form for editing<img src="${pageContext.request.contextPath}/images/icons/lock.png"/>
</a>