<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<tiles:importAttribute/>

<div class="wizard-steps">
    <c:forEach items="${stepContext.steps.stepLabels}" var="label" varStatus="status">
        <span id="${wizardName}_step${status.count}"><spring:message code="${label}"/></span>
    </c:forEach>
</div>
