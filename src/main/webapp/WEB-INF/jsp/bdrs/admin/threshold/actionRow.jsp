<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<%@page import="au.com.gaiaresources.bdrs.model.threshold.ActionType"%>

<tiles:useAttribute name="threshold" classname="au.com.gaiaresources.bdrs.model.threshold.Threshold" ignore="true"/>
<tiles:useAttribute name="action" classname="au.com.gaiaresources.bdrs.model.threshold.Action" ignore="true"/>
<tiles:useAttribute name="index" ignore="true"/>

<tr>
	<c:choose>
	    <c:when test="${ action.id == null }">
	       <td>
	            <input type="hidden" name="new_action" value="${ index }"/>
		        <select id="add_action_actiontype_${ index }" name="add_action_actiontype_${ index }" onchange="bdrs.threshold.handlers.changeActionType('#add_action_actiontype_${ index }', '#add_action_cell_${ index }', ${ index }, null);">
		            <c:forEach items="<%= threshold.getPossibleActionTypes() %>" var="addActionType">
		               <jsp:useBean id="addActionType" type="au.com.gaiaresources.bdrs.model.threshold.ActionType" />
		               <option value="<%= addActionType.toString() %>"
		                   <c:if test="<%= addActionType.equals(action.getActionType()) %>">selected="selected"</c:if>
		               >
		                   <c:out value="<%= addActionType.getDisplayText() %>"/>
		               </option>
		            </c:forEach>
		        </select>
		    </td>
		    <td id="add_action_cell_${ index }">
		        <tiles:insertDefinition name="thresholdActionValue">
                    <tiles:putAttribute name="action" value="${ action }"/>
                    <tiles:putAttribute name="index" value="${ index }"/>
                </tiles:insertDefinition>
		    </td>
	    </c:when>
	    <c:otherwise>
	       <td>
	            <input type="hidden" name="action_pk" value="${ action.id }"/>
                <select id="action_actiontype_${ action.id }" name="action_actiontype_${ action.id }" onchange="bdrs.threshold.handlers.changeActionType('#action_actiontype_${ index }', '#action_cell_${ index }', null, ${ action.id });">
                    <c:forEach items="<%= threshold.getPossibleActionTypes() %>" var="actionType">
                       <jsp:useBean id="actionType" type="au.com.gaiaresources.bdrs.model.threshold.ActionType" />
                       <option value="<%= actionType.toString() %>"
                           <c:if test="<%= actionType.equals(action.getActionType()) %>">selected="selected"</c:if>
                       >
                           <c:out value="<%= actionType.getDisplayText() %>"/>
                       </option>
                    </c:forEach>
                </select>
            </td>
            <td id="action_cell_${ index }">
                <tiles:insertDefinition name="thresholdActionValue">
                    <tiles:putAttribute name="action" value="${ action }"/>
                    <tiles:putAttribute name="index" value="${ index }"/>
                </tiles:insertDefinition>
            </td>
	    </c:otherwise>
	</c:choose>
	<td class="textcenter">
        <a href="javascript: void(0);" onclick="jQuery(this).parents('tr').remove();">
            <img src="${pageContext.request.contextPath}/images/icons/delete.png" alt="Delete" class="vertmiddle"/>
        </a>
    </td>
</tr>

	

    
