<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@page import="au.com.gaiaresources.bdrs.model.taxa.AttributeType"%>
<%@page import="au.com.gaiaresources.bdrs.model.taxa.AttributeScope"%>
<%@page import="au.com.gaiaresources.bdrs.controller.attribute.AttributeInstanceFormField"%>

<tiles:useAttribute name="formField" classname="au.com.gaiaresources.bdrs.controller.attribute.AttributeFormField" ignore="true"/>
<tiles:useAttribute name="showScope" ignore="true"/>
<tiles:useAttribute name="isTag" ignore="true"/>
<tiles:useAttribute name="index" ignore="true"/>

<tr>
	<c:choose>
	    <c:when test="<%= formField.isAttributeField() %>">
	        <c:choose>
			    <c:when test="${ formField.attribute == null }">
		            <td class="drag_handle">
		                <input type="hidden" value="0" class="sort_weight" name="add_weight_${index}"/>
		            </td>
	                <td>
	                    <input type="text" name="add_description_${index}"/>
	                </td>		            
		            <td>
		                <input type="hidden" value="${index}" name="add_attribute"/>
		                <input type="hidden" value="<c:choose><c:when test="${ isTag == true }">true</c:when><c:otherwise>false</c:otherwise></c:choose>" name="add_tag_${index}"/>
		                <input type="text" name="add_name_${index}" class="uniqueName validate(uniqueOrBlank(.uniqueName))"/>
		            </td>
		            <td>
		                <select name="add_typeCode_${index}" onchange="bdrs.attribute.enableOptionInput('<%= AttributeType.STRING_WITH_VALID_VALUES.getCode() %>' == jQuery(this).val(), '[name=add_option_${index}]');">
		                    <c:forEach items="<%=AttributeType.values()%>" var="add_type">
		                        <jsp:useBean id="add_type" type="au.com.gaiaresources.bdrs.model.taxa.AttributeType" />
		                        <option value="<%= add_type.getCode() %>">
		                            <c:out value="<%= add_type.getName() %>"/>
		                        </option>
		                    </c:forEach>
		                </select>
		            </td>
		            <td class="textcenter">
		                <input type="checkbox" name="add_required_${index}"/>
		            </td>
		            <c:if test="${ showScope == true }">
			            <td class="textcenter">
			                <select name="add_scope_${index}">
			                    <c:forEach items="<%= AttributeScope.values() %>" var="add_scope">
			                        <jsp:useBean id="add_scope" type="au.com.gaiaresources.bdrs.model.taxa.AttributeScope" />
			                        <option value="<%= add_scope.toString() %>">
			                            <c:out value="<%= add_scope.getName() %>"/>
			                        </option>
			                    </c:forEach>
			                </select>
			            </td>
		            </c:if>
		            <td>
		                <input type="text" name="add_option_${index}" disabled="disabled"/>
		            </td>
		            <td class="textcenter">
		                <a href="javascript: void(0);" onclick="jQuery(this).parents('tr').hide().find('select, input, textarea').attr('disabled', 'disabled').removeClass(); return false;">
		                    <img src="${pageContext.request.contextPath}/images/icons/delete.png" alt="Delete" class="vertmiddle"/>
		                </a>
		            </td>
			    </c:when>
			    <c:otherwise>
		            <td class="drag_handle">
		                <input type="hidden" class="sort_weight" value="${formField.weight}" name="${formField.weightName}"/>
		            </td>
		            <td>
		                <input type="text" name="description_${formField.attribute.id}"
		                    value="<c:out value="${formField.attribute.description}"/>"
		                />
		            </td>
	                <td>
	                    <input type="hidden" value="${formField.attribute.id}" name="attribute"/>
	                    <input type="hidden" value="${formField.attribute.tag}" name="tag_${formField.attribute.id}"/>
	                    <input type="text" name="name_${formField.attribute.id}" class="uniqueName validate(uniqueOrBlank(.uniqueName))"
	                        value="<c:out value="${formField.attribute.name}"/>"
	                    />
	                </td>
		            <td>
		                <select name="typeCode_${formField.attribute.id}" onchange="bdrs.attribute.enableOptionInput('<%= AttributeType.STRING_WITH_VALID_VALUES.getCode() %>' == jQuery(this).val(), '[name=option_${formField.attribute.id}]');">
		                    <c:forEach items="<%=AttributeType.values()%>" var="type">
		                        <jsp:useBean id="type" type="au.com.gaiaresources.bdrs.model.taxa.AttributeType" />
		                        <option value="<%= type.getCode() %>"
		                            <c:if test="<%= ((AttributeInstanceFormField)formField).getAttribute() != null && type.getCode().equals(((AttributeInstanceFormField)formField).getAttribute().getTypeCode()) %>">
		                                selected="selected"
		                            </c:if>
		                        >
		                            <c:out value="<%= type.getName() %>"/>
		                        </option>
		                    </c:forEach>
		                </select>
		            </td>
		            <td class="textcenter">
		                <input type="checkbox" name="required_${formField.attribute.id}"
		                    <c:if test="${formField.attribute.required}">
		                        checked="checked"
		                    </c:if>
		                />
		            </td>
		            <c:if test="${ showScope == true }">
			            <td class="textcenter">
			                <select name="scope_${formField.attribute.id}">
			                    <c:forEach items="<%= AttributeScope.values() %>" var="attr_scope">
			                        <jsp:useBean id="attr_scope" type="au.com.gaiaresources.bdrs.model.taxa.AttributeScope" />
			                        <option value="<%= attr_scope.toString() %>"
			                            <c:if test="<%= (((AttributeInstanceFormField)formField).getAttribute() != null && attr_scope.equals(((AttributeInstanceFormField)formField).getAttribute().getScope())) || (AttributeScope.RECORD.equals(attr_scope) && ((AttributeInstanceFormField)formField).getAttribute().getScope() == null) %>">
			                                selected="selected"
			                            </c:if>
			                        >
			                            <c:out value="<%= attr_scope.getName() %>"/>
			                        </option>
			                    </c:forEach>
			                </select>
			            </td>
		            </c:if>
		            <td>
		                <input type="text" name="option_${formField.attribute.id}"
		                    value="<c:forEach items="${formField.attribute.options}" var="opt"><c:out value="${opt.value}"/>, </c:forEach>"
		                    <c:if test="<%= !AttributeType.STRING_WITH_VALID_VALUES.getCode().equals(((AttributeInstanceFormField)formField).getAttribute().getTypeCode()) %>">
		                        disabled="disabled"
		                    </c:if>
		                />
		            </td>
		            <td class="textcenter">
		                <a href="javascript: void(0);" onclick="jQuery(this).parents('tr').hide().find('select, input, textarea').attr('disabled', 'disabled').removeClass(); return false;">
		                    <img src="${pageContext.request.contextPath}/images/icons/delete.png" alt="Delete" class="vertmiddle"/>
		                </a>
		            </td>
			    </c:otherwise>
			</c:choose>
	    </c:when>
	    <c:when test="<%= formField.isPropertyField() %>">
	        <td class="drag_handle">
	            <input type="hidden" value="${ formField.weight }" class="sort_weight" name="${formField.weightName}"/>
	        </td>
	        <td>
	            <c:choose>
	                <c:when test="${ 'species' == formField.propertyName }">
	                    Species                                        
	                </c:when>
	                <c:when test="${ 'location' == formField.propertyName }">
	                    Location                                       
	                </c:when>
	                <c:when test="${ 'point' == formField.propertyName }">
	                    Position                                       
	                </c:when>
	                <c:when test="${ 'when' == formField.propertyName }">
	                    Date                                       
	                </c:when>
	                <c:when test="${ 'time' == formField.propertyName }">
	                    Time                                        
	                </c:when>
	                <c:when test="${ 'notes' == formField.propertyName }">
	                    Additional Comments                                        
	                </c:when>
	                <c:when test="${ 'number' == formField.propertyName }">
	                    Number                            
	                </c:when>
	            </c:choose>
	        </td>
	        <td>N/A</td>
	        <td>N/A</td>
	        <td class="textcenter">Yes</td>
	        <td>Survey</td>
	        <td></td>
	        <td class="textcenter">DwC</td>    
	    </c:when>
	</c:choose>
</tr>