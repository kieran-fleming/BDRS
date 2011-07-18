<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<div class="pageContent">
    <h1>Portal Selection</h1>
    <table class="datatable">
        <thead>
                <tr>
                    <th>Portal Name</th>
                    <th>Default Portal</th>
                    <th>Current Portal</th>
                </tr>
        </thead>
        <tbody>
            <c:forEach items="${portalList}" var="portal">
                <tr>
                    <td>
                        <a href="${pageContext.request.contextPath}/portal/${portal.id}/home.htm">
                            ${portal.name}
                        </a>
                    </td>
                    <td class="textcenter">
                        <c:if test="${portal.default}">
                            <img class="vertmiddle" src="${pageContext.request.contextPath}/images/icons/yes.png" alt="Yes"/>
                        </c:if>
                    </td>
                    <td class="textcenter">
                        <c:if test="${context.portal == portal}">
                            <img class="vertmiddle" src="${pageContext.request.contextPath}/images/icons/yes.png" alt="Yes"/>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>