<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<jsp:useBean id="context" scope="request" type="au.com.gaiaresources.bdrs.servlet.RequestContext"></jsp:useBean>
<a href="${pageContext.request.contextPath}/authenticated/redirect.htm">
    <div id="header" class="<sec:authorize ifAnyGranted="ROLE_ROOT">root</sec:authorize>">
        <div id="portalName">
            <%= context.getPortal() == null ? "Biological Data Recording System" : context.getPortal().getName() %>
        </div>
    </div>
</a>
