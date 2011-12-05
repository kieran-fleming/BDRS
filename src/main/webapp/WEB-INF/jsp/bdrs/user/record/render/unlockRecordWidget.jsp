<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<tiles:useAttribute name="surveyId" />
<tiles:useAttribute name="recordId" />

<a title="unlock form for editing" href="${pageContext.request.contextPath}/bdrs/user/surveyRenderRedirect.htm?surveyId=${surveyId}&recordId=${recordId}&editForm=true"
>Unlock form for editing<img src="${pageContext.request.contextPath}/images/icons/lock.png"/>
</a>