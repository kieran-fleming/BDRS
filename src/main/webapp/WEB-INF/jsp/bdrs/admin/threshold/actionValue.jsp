<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<%@page import="au.com.gaiaresources.bdrs.model.threshold.ActionType"%>

<tiles:useAttribute name="action" classname="au.com.gaiaresources.bdrs.model.threshold.Action" ignore="true"/>
<tiles:useAttribute name="index" ignore="true"/>

<c:choose>
    <c:when test="${ action.id == null }">
        <c:choose>
		    <c:when test="<%= ActionType.EMAIL_NOTIFICATION.equals(action.getActionType()) %>">
		        <input class="validate(required, email)" type="text" name="add_action_value_${ index }"/>
		    </c:when>
		    <c:when test="<%= ActionType.HOLD_RECORD.equals(action.getActionType()) %>">
		        <input type="hidden" name="add_action_value_${ index }" value=""/>
		        <span>N/A</span>
		    </c:when>
		</c:choose>
    </c:when>
    <c:otherwise>
        <c:choose>
            <c:when test="<%= ActionType.EMAIL_NOTIFICATION.equals(action.getActionType()) %>">
                <input class="validate(required, email)" type="text" name="action_value_${ action.id }" value="<c:out value="${ action.value }"/>"/>
            </c:when>
            <c:when test="<%= ActionType.HOLD_RECORD.equals(action.getActionType()) %>">
                <input type="hidden" name="action_value_${ action.id }" value="<c:out value="${ action.value }"/>"/>
                <span>N/A</span>
            </c:when>
        </c:choose>
    </c:otherwise>
</c:choose>

