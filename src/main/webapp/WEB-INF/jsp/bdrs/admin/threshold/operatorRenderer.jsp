<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<%@page import="java.lang.Iterable"%>

<tiles:useAttribute name="condition" classname="au.com.gaiaresources.bdrs.model.threshold.Condition" ignore="true"/>
<tiles:useAttribute name="index" ignore="true"/>

<%--

The file is structured like this

if we are adding a condition,
    if the target property is a complex type
        perform complex type rendering
    else the target property is a simple type
        perform simple type rendering
else we are editing a condition
    if the target property is a complex type
        perform complex type rendering
    else the target property is a simple type
        perform simple type rendering
--%>

<c:choose>
    <c:when test="${ condition.id == null }">
        <input type="hidden" name="add_condition" value="${ index }"/>
        <input type="hidden" name="add_property_path_${ index }" value="${ condition.propertyPath }"/>
        <c:choose>
		    <c:when test="<%= Iterable.class.isAssignableFrom(condition.getTargetClassForPath()) %>">
		        <c:set var="add_complex_operator" value="<%= condition.getComplexTypeOperator() %>"/>
		        <jsp:useBean id="add_complex_operator" type="au.com.gaiaresources.bdrs.service.threshold.ComplexTypeOperator" />
		        <span>
			        <c:out value="<%= add_complex_operator.getKeyLabel() %>"/>
			        <select id="add_key_operator_${ index }" name="add_key_operator_${ index }">
                        <c:forEach items="<%= condition.getPossibleValueOperators() %>" var="add_iterable_key_operator">
                            <jsp:useBean id="add_iterable_key_operator" type="au.com.gaiaresources.bdrs.model.threshold.Operator" />
                            <option value="<%= add_iterable_key_operator.toString() %>"
                                <c:if test="${ condition.valueOperator == add_iterable_key_operator }">selected="selected"</c:if>
                            >
                                <c:out value="<%= add_iterable_key_operator.getDisplayText() %>"/>
                            </option>
                        </c:forEach>
                    </select>
                    <tiles:insertDefinition name="thresholdConditionValue">
	                    <tiles:putAttribute name="condition" value="${ condition }"/>
	                    <tiles:putAttribute name="index" value="${ index }"/>
	                    <tiles:putAttribute name="valueForKey" value="true"/>
                    </tiles:insertDefinition>
			        
			        <c:out value="<%= add_complex_operator.getValueLabel() %>"/>
			        <select id="add_value_operator_${ index }" name="add_value_operator_${ index }">
	                    <c:forEach items="<%= condition.getPossibleValueOperators() %>" var="add_iterable_value_operator">
	                        <jsp:useBean id="add_iterable_value_operator" type="au.com.gaiaresources.bdrs.model.threshold.Operator" />
	                        <option value="<%= add_iterable_value_operator.toString() %>"
	                            <c:if test="${ condition.valueOperator == add_iterable_value_operator }">selected="selected"</c:if>
	                        >
	                            <c:out value="<%= add_iterable_value_operator.getDisplayText() %>"/>
	                        </option>
	                    </c:forEach>
	                </select>
	                <tiles:insertDefinition name="thresholdConditionValue">
                        <tiles:putAttribute name="condition" value="${ condition }"/>
                        <tiles:putAttribute name="index" value="${ index }"/>
                        <tiles:putAttribute name="valueForKey" value="false"/>
                    </tiles:insertDefinition>
	                
		        </span>
		    </c:when>
		    <c:otherwise>
		        <select id="add_value_operator_${ index }" name="add_value_operator_${ index }">
		            <c:forEach items="<%= condition.getPossibleValueOperators() %>" var="add_value_operator">
		                <jsp:useBean id="add_value_operator" type="au.com.gaiaresources.bdrs.model.threshold.Operator" />
		                <option value="<%= add_value_operator.toString() %>"
		                    <c:if test="${ condition.valueOperator == add_value_operator }">selected="selected"</c:if>
		                >
		                    <c:out value="<%= add_value_operator.getDisplayText() %>"/>
		                </option>
		            </c:forEach>
		        </select>
		        
		        <tiles:insertDefinition name="thresholdConditionValue">
                    <tiles:putAttribute name="condition" value="${ condition }"/>
                    <tiles:putAttribute name="index" value="${ index }"/>
                    <tiles:putAttribute name="valueForKey" value="false"/>
                </tiles:insertDefinition>
                
		    </c:otherwise>
		</c:choose>
    </c:when>
    <c:otherwise>
        <!--<input type="hidden" name="condition_id" value="${ condition.id }"/>-->
        <input type="hidden" name="property_path_${ condition.id }" value="${ condition.propertyPath }"/>
        <c:choose>
            <c:when test="<%= Iterable.class.isAssignableFrom(condition.getTargetClassForPath()) %>">
                <c:set var="complex_operator" value="<%= condition.getComplexTypeOperator() %>"/>
                <jsp:useBean id="complex_operator" type="au.com.gaiaresources.bdrs.service.threshold.ComplexTypeOperator" />
                <span>
                    <c:out value="<%= complex_operator.getKeyLabel() %>"/>
                    <select id="key_operator_${ condition.id  }" name="key_operator_${ condition.id  }">
                        <c:forEach items="<%= condition.getPossibleValueOperators() %>" var="iterable_key_operator">
                            <jsp:useBean id="iterable_key_operator" type="au.com.gaiaresources.bdrs.model.threshold.Operator" />
                            <option value="<%= iterable_key_operator.toString() %>"
                                <c:if test="${ condition.valueOperator == iterable_key_operator }">selected="selected"</c:if>
                            >
                                <c:out value="<%= iterable_key_operator.getDisplayText() %>"/>
                            </option>
                        </c:forEach>
                    </select>
                    <tiles:insertDefinition name="thresholdConditionValue">
                        <tiles:putAttribute name="condition" value="${ condition }"/>
                        <tiles:putAttribute name="valueForKey" value="true"/>
                    </tiles:insertDefinition>
                    
                    <c:out value="<%= complex_operator.getValueLabel() %>"/>
                    <select id="value_operator_${ condition.id  }" name="value_operator_${ condition.id  }">
                        <c:forEach items="<%= condition.getPossibleValueOperators() %>" var="iterable_value_operator">
                            <jsp:useBean id="iterable_value_operator" type="au.com.gaiaresources.bdrs.model.threshold.Operator" />
                            <option value="<%= iterable_value_operator.toString() %>"
                                <c:if test="${ condition.valueOperator == iterable_value_operator }">selected="selected"</c:if>
                            >
                                <c:out value="<%= iterable_value_operator.getDisplayText() %>"/>
                            </option>
                        </c:forEach>
                    </select>
                    <tiles:insertDefinition name="thresholdConditionValue">
                        <tiles:putAttribute name="condition" value="${ condition }"/>
                        <tiles:putAttribute name="valueForKey" value="false"/>
                    </tiles:insertDefinition>
                    
                </span>
            </c:when>
            <c:otherwise>
                <select id="value_operator_${ condition.id  }" name="value_operator_${ condition.id  }">
                    <c:forEach items="<%= condition.getPossibleValueOperators() %>" var="value_operator">
                        <jsp:useBean id="value_operator" type="au.com.gaiaresources.bdrs.model.threshold.Operator" />
                        <option value="<%= value_operator.toString() %>"
                            <c:if test="${ condition.valueOperator == value_operator }">selected="selected"</c:if>
                        >
                            <c:out value="<%= value_operator.getDisplayText() %>"/>
                        </option>
                    </c:forEach>
                </select>
                
                <tiles:insertDefinition name="thresholdConditionValue">
                    <tiles:putAttribute name="condition" value="${ condition }"/>
                    <tiles:putAttribute name="valueForKey" value="false"/>
                </tiles:insertDefinition>
                
            </c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>
