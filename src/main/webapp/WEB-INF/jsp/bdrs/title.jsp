<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<tiles:useAttribute name="title" ignore="true"/>

<c:choose>
    <c:when test="${not empty pageTitle}">
        <h1 class="pageTitle">${pageTitle}</h1>
    </c:when>
    <c:otherwise>
        <h1 class="pageTitle">${title}</h1>
    </c:otherwise>
</c:choose>
