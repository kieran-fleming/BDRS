<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@page import="java.util.Date"%>

<tiles:useAttribute name="condition" classname="au.com.gaiaresources.bdrs.model.threshold.Condition" ignore="true"/>
<tiles:useAttribute name="index" ignore="true"/>
<tiles:useAttribute name="valueForKey" ignore="true"/>

<c:choose>
    <c:when test="<%= condition.isSimplePropertyType() %>">
        <c:set var="klass" value="<%= condition.getTargetClassForPath() %>"/>
    </c:when>
    <c:otherwise>
        <c:choose>
            <c:when test="${ valueForKey }">
                <c:set var="klass" value="<%= condition.getComplexTypeOperator().getKeyClass() %>" />
            </c:when>
            <c:otherwise>
                <c:set var="klass" value="<%= condition.getComplexTypeOperator().getValueClass() %>" />
            </c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>
<jsp:useBean id="klass" type="java.lang.Class" />

<c:choose>
    <c:when test="${ valueForKey }">
        <c:set var="keyValuePrefix" value="key" />
    </c:when>
    <c:otherwise>
        <c:set var="keyValuePrefix" value="value" />
    </c:otherwise>
</c:choose>

<c:choose>
    <c:when test="${ condition.id == null }">
        <c:choose>
            <c:when test="<%= String.class.equals(klass) %>">
                <input class="validate(required)" type="text" name="add_${ keyValuePrefix }_value_${ index }"/>
            </c:when>
            <c:when test="<%= Boolean.class.equals(klass) %>">
                <fieldset class="noborder">
                    <span class="radio">
                        <input class="validate(required)" id="add_true_${ index }" type="radio" name="add_${ keyValuePrefix }_value_${ index }" value="true" class="vertmiddle"/>
                        <label for="add_true_${ index }">Yes</label>
                    </span>
                    
                    <span class="radio">
                        <input class="validate(required)" id="add_false_${ index }" type="radio" name="add_${ keyValuePrefix }_value_${ index }" value="false" class="vertmiddle"/>
                        <label for="add_false_${ index }">No</label>
                    </span>
                </fieldset>
            </c:when>
            <c:when test="<%= Integer.class.equals(klass) %>">
                <input class="validate(required, integer)" type="text" name="add_${ keyValuePrefix }_value_${ index }"/>
            </c:when>
            <c:when test="<%= Long.class.equals(klass) %>">
                <input class="validate(required, integer)" type="text" name="add_${ keyValuePrefix }_value_${ index }"/>
            </c:when>
            <c:when test="<%= Date.class.equals(klass) %>">
                <input class="validate(required) datepicker" type="text" name="add_${ keyValuePrefix }_value_${ index }"/>
            </c:when>
        </c:choose>
    </c:when>
    <c:otherwise>
        <c:choose>
            <c:when test="<%= String.class.equals(klass) %>">
                <input class="validate(required)" type="text" name="${ keyValuePrefix }_value_${ condition.id }"
	                <c:choose>
					    <c:when test="${ valueForKey }">
					        value="<%= condition.stringKey() %>"
					    </c:when>
					    <c:otherwise>
					        value="<%= condition.stringValue() %>"
					    </c:otherwise>
					</c:choose> 
                />
            </c:when>
            <c:when test="<%= Boolean.class.equals(klass) %>">
                <fieldset class="noborder">
                    <span class="radio">
                        <input id="true_${ condition.id }" type="radio" name="${ keyValuePrefix }_value_${ condition.id }" value="true" class="vertmiddle"
                            <c:choose>
		                        <c:when test="${ valueForKey }">
		                            <c:if test="<%= Boolean.TRUE.equals(condition.booleanKey()) %>">
		                                checked="checked"
	                                </c:if>
		                        </c:when>
		                        <c:otherwise>
		                            <c:if test="<%= Boolean.TRUE.equals(condition.booleanValue()) %>">
                                        checked="checked"
                                    </c:if>
		                        </c:otherwise>
		                    </c:choose>
                        />
                        <label for="true_${ condition.id }">Yes</label>
                    </span>
                    
                    <span class="radio">
                        <input class="validate(required)" id="false_${ condition.id }" type="radio" name="${ keyValuePrefix }_value_${ condition.id }" value="false" class="vertmiddle"
                            <c:choose>
                                <c:when test="${ valueForKey }">
                                    <c:if test="<%= Boolean.FALSE.equals(condition.booleanKey()) %>">
                                        checked="checked"
                                    </c:if>
                                </c:when>
                                <c:otherwise>
                                    <c:if test="<%= Boolean.FALSE.equals(condition.booleanValue()) %>">
                                        checked="checked"
                                    </c:if>
                                </c:otherwise>
                            </c:choose>
                        />
                        <label for="false_${ condition.id }">No</label>
                    </span>
                </fieldset>
            </c:when>
            <c:when test="<%= Integer.class.equals(klass) %>">
                <input class="validate(required, integer)" type="text" name="${ keyValuePrefix }_value_${ condition.id }"
                    <c:choose>
                        <c:when test="${ valueForKey }">
                            value="<%= condition.intKey() %>"
                        </c:when>
                        <c:otherwise>
                            value="<%= condition.intValue() %>"
                        </c:otherwise>
                    </c:choose> 
                />
            </c:when>
            <c:when test="<%= Long.class.equals(klass) %>">
                <input class="validate(required, integer)" type="text" name="${ keyValuePrefix }_value_${ condition.id }"
                    <c:choose>
                        <c:when test="${ valueForKey }">
                            value="<%= condition.longKey() %>"
                        </c:when>
                        <c:otherwise>
                            value="<%= condition.longValue() %>"
                        </c:otherwise>
                    </c:choose> 
                />
            </c:when>
            <c:when test="<%= Date.class.equals(klass) %>">
                <input class="validate(required) datepicker" type="text" name="${ keyValuePrefix }_value_${ condition.id }"
                    <c:choose>
                        <c:when test="${ valueForKey }">
                            value="<fmt:formatDate pattern="dd MMM yyyy" value="<%= condition.dateKey() %>"/>"
                        </c:when>
                        <c:otherwise>
                            value="<fmt:formatDate pattern="dd MMM yyyy" value="<%= condition.dateValue() %>"/>"
                        </c:otherwise>
                    </c:choose> 
                />
            </c:when>
        </c:choose>
    </c:otherwise>
</c:choose>    
