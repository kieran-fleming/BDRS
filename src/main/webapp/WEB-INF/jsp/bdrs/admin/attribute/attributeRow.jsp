<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@page import="au.com.gaiaresources.bdrs.model.taxa.AttributeType"%>
<%@page import="au.com.gaiaresources.bdrs.model.taxa.AttributeScope"%>
<%@page import="au.com.gaiaresources.bdrs.controller.attribute.AttributeInstanceFormField"%>
<%@page import="au.com.gaiaresources.bdrs.model.survey.Survey"%>
<%@page import="au.com.gaiaresources.bdrs.model.survey.SurveyFormRendererType" %>

<tiles:useAttribute name="formField" classname="au.com.gaiaresources.bdrs.controller.attribute.AttributeFormField" ignore="true"/>
<tiles:useAttribute name="survey" classname="au.com.gaiaresources.bdrs.model.survey.Survey" ignore="true"/>
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
                        <span class="table_input_container">
                            <input type="text" name="add_description_${index}" id="add_description_${index}"/>
                        </span>
                    </td>
                    <td>
                        <span class="table_input_container">
                            <input type="hidden" value="${index}" name="add_attribute"/>
                            <input type="hidden" value="<c:choose><c:when test="${ isTag == true }">true</c:when><c:otherwise>false</c:otherwise></c:choose>" name="add_tag_${index}"/>
                            <input type="text" name="add_name_${index}" id="add_name_${index}" class="uniqueName validate(uniqueAndRequired(.uniqueName))"/>
                        </span>
                    </td>
                    <td>
                        <span class="table_input_container">
                            <select class="attrTypeSelect" name="add_typeCode_${index}">
                                <c:forEach items="<%=AttributeType.values()%>" var="add_type">
                                    <jsp:useBean id="add_type" type="au.com.gaiaresources.bdrs.model.taxa.AttributeType" />
                                    <option value="<%= add_type.getCode() %>">
                                        <c:out value="<%= add_type.getName() %>"/>
                                    </option>
                                </c:forEach>
                            </select>
                        </span>
                    </td>
                    <td class="textcenter">
                        <input id="add_required_${index}" 
                              type="checkbox" 
                              name="add_required_${index}"/>
                    </td>
                    <c:if test="${ showScope == true }">
                        <td class="textcenter">
                            <span class="table_input_container">
                                <select class="attrScopeSelect" name="add_scope_${index}" >
                                    <c:forEach items="<%= AttributeScope.values() %>" var="add_scope">
                                        <jsp:useBean id="add_scope" type="au.com.gaiaresources.bdrs.model.taxa.AttributeScope" />
                                        <option value="<%= add_scope.toString() %>">
                                            <c:out value="<%= add_scope.getName() %>"/>
                                        </option>
                                    </c:forEach>
                                </select>
                            </span>
                        </td>
                    </c:if>
                    <td>
                        <span class="table_input_container">
                            <input type="text" name="add_option_${index}" disabled="disabled"/>
                        </span>
                    </td>
                    <td class="textcenter">
                        <a href="javascript: void(0);" onclick="jQuery(this).parents('tr').hide().find('select, input, textarea').attr('disabled', 'disabled').removeClass(); return false;">
                            <img src="${pageContext.request.contextPath}/images/icons/delete.png" alt="Delete" class="vertmiddle"/>
                        </a>
                    </td>
                    <script type="text/javascript">
                        jQuery(function() {
                            var func = bdrs.attribute.getRowTypeChangedFunc();
                            var eventData = {index: ${index}, bNewRow: true};
                            jQuery("[name=add_typeCode_"+${index}+"]").change(eventData, func);
                            jQuery("[name=add_scope_"+${index}+"]").change(eventData, rowScopeChanged);
                        });
                    </script>
                </c:when>
                <c:otherwise>
                    <td class="drag_handle">
                        <input type="hidden" class="sort_weight" value="${formField.weight}" name="${formField.weightName}"/>
                    </td>
                    <td>
                        <span class="table_input_container">
                            <input type="text" name="description_${formField.attribute.id}"
                                value="<c:out value="${formField.attribute.description}"/>"
                                <c:if test="<%= ((AttributeInstanceFormField)formField).getAttribute() != null && AttributeType.HTML_HORIZONTAL_RULE.getCode().equals(((AttributeInstanceFormField)formField).getAttribute().getTypeCode()) %>">
                                    disabled="disabled"
                                </c:if>
                                <c:if test="<%= ((AttributeInstanceFormField)formField).getAttribute() != null && AttributeType.HTML.getCode().equals(((AttributeInstanceFormField)formField).getAttribute().getTypeCode()) %>">
                                    onfocus="bdrs.attribute.showHtmlEditor($('#htmlEditorDialog'), $('#markItUp')[0], this)"
                                </c:if>
                            />
                        </span>
                    </td>
                    <td>
                        <span class="table_input_container">
                            <input type="hidden" value="${formField.attribute.id}" name="attribute"/>
                            <input type="hidden" value="${formField.attribute.tag}" name="tag_${formField.attribute.id}"/>
                            <input type="text" name="name_${formField.attribute.id}" class="uniqueName validate(uniqueAndRequired(.uniqueName))"
                                value="<c:out value="${formField.attribute.name}"/>"
                            />
                        </span>
                    </td>
                    <td>
                        <span class="table_input_container">
                            <select class="attrTypeSelect" name="typeCode_${formField.attribute.id}" >
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
                        </span>
                    </td>
                    <td class="textcenter">
                        <span class="table_input_container">
                            <input id="required_${formField.attribute.id}" type="checkbox" name="required_${formField.attribute.id}"
                                <c:if test="${formField.attribute.required}">
                                    checked="checked"
                                </c:if>
                                <c:if test="<%= ((AttributeInstanceFormField)formField).getAttribute() != null && 
                                        (AttributeType.SINGLE_CHECKBOX.getCode().equals(((AttributeInstanceFormField)formField).getAttribute().getTypeCode()) || 
                                         AttributeType.HTML.getCode().equals(((AttributeInstanceFormField)formField).getAttribute().getTypeCode()) || 
                                         AttributeType.HTML_COMMENT.getCode().equals(((AttributeInstanceFormField)formField).getAttribute().getTypeCode()) || 
                                         AttributeType.HTML_HORIZONTAL_RULE.getCode().equals(((AttributeInstanceFormField)formField).getAttribute().getTypeCode())) %>">
                                    disabled="disabled"
                                </c:if>
                            />
                        </span>
                    </td>
                    <c:if test="${ showScope == true }">
                        <td class="textcenter">
                            <span class="table_input_container">
                                <select class="attrScopeSelect" name="scope_${formField.attribute.id}" >
                                    <c:forEach items="<%= AttributeScope.values() %>" var="attr_scope">
                                        <jsp:useBean id="attr_scope" type="au.com.gaiaresources.bdrs.model.taxa.AttributeScope" />
                                        <option value="<%= attr_scope.toString() %>"
                                            <c:if test="<%= (((AttributeInstanceFormField)formField).getAttribute() != null && 
                                                    attr_scope.equals(((AttributeInstanceFormField)formField).getAttribute().getScope())) || 
                                                    (AttributeScope.RECORD.equals(attr_scope) && ((AttributeInstanceFormField)formField).getAttribute().getScope() == null) %>">
                                                selected="selected"
                                            </c:if>
                                        >
                                            <c:out value="<%= attr_scope.getName() %>"/>
                                        </option>
                                    </c:forEach>
                                </select>
                            </span>
                        </td>
                    </c:if>
                    <td>
                        <span class="table_input_container">
                            <input type="text" name="option_${formField.attribute.id}"
                                value="<c:out value="${formField.attribute.optionString}"/>"
                                <c:if test="<%= !(AttributeType.STRING_WITH_VALID_VALUES.getCode().equals(((AttributeInstanceFormField)formField).getAttribute().getTypeCode()) 
                                    || AttributeType.INTEGER_WITH_RANGE.getCode().equals(((AttributeInstanceFormField)formField).getAttribute().getTypeCode())
                                    || AttributeType.BARCODE.getCode().equals(((AttributeInstanceFormField)formField).getAttribute().getTypeCode())
                                    || AttributeType.REGEX.getCode().equals(((AttributeInstanceFormField)formField).getAttribute().getTypeCode())
                                    || AttributeType.MULTI_CHECKBOX.getCode().equals(((AttributeInstanceFormField)formField).getAttribute().getTypeCode())
                                    || AttributeType.MULTI_SELECT.getCode().equals(((AttributeInstanceFormField)formField).getAttribute().getTypeCode())
                                    || AttributeType.HTML.getCode().equals(((AttributeInstanceFormField)formField).getAttribute().getTypeCode())
                                    || AttributeType.HTML_COMMENT.getCode().equals(((AttributeInstanceFormField)formField).getAttribute().getTypeCode())
                                    )%>">
                                    disabled="disabled"
                                </c:if>
                            />
                        </span>
                    </td>
                    <td class="textcenter">
                        <a href="javascript: void(0);" onclick="jQuery(this).parents('tr').hide().find('select, input, textarea').attr('disabled', 'disabled').removeClass(); return false;">
                            <img src="${pageContext.request.contextPath}/images/icons/delete.png" alt="Delete" class="vertmiddle"/>
                        </a>
                    </td>
                    
                    <script type="text/javascript">
                        jQuery(function() {
                            var func = bdrs.attribute.getRowTypeChangedFunc();
                            var eventData = {index: ${formField.attribute.id}, bNewRow: false};
                            jQuery("[name=typeCode_"+${formField.attribute.id}+"]").change(eventData, func);
                            jQuery("[name=scope_"+${formField.attribute.id}+"]").change(eventData, rowScopeChanged);
                        });
                    </script>
                </c:otherwise>
            </c:choose>
        </c:when>
        <c:when test="<%= formField.isPropertyField() %>">
            <td class="drag_handle">
                <span class="table_input_container">
                    <input type="hidden" value="${ formField.weight }" class="sort_weight" name="RECORD.${formField.propertyName}.WEIGHT" id="RECORD.${formField.propertyName}.WEIGHT"/>
                </span>
            </td>
            <td>
                <span class="table_input_container">
                    <input type="text"  value="${ formField.description }" name="RECORD.${formField.propertyName}.DESCRIPTION" id="RECORD.${formField.propertyName}.DESCRIPTION"/>
                </span>
               </td>
               <td>
                   <span class="table_input_container">
                    <input type="hidden" value="${formField.propertyName}" name="property"/>
                    <input type="text" name="RECORD.${formField.propertyName}.NAME" value="<c:out value="${formField.propertyName}"/>" disabled="disabled" />
                </span>
            </td>
            <td>DwC</td>
            <td class="textcenter"> 
                <span class="table_input_container">
                    <input id="RECORD.${formField.propertyName}.REQUIRED" type="checkbox" name="RECORD.${formField.propertyName}.REQUIRED"  value="true" <c:if test="${formField.required}"> checked="checked"</c:if><c:if test="${formField.propertyName == 'when'}">
                    disabled="disabled"
                    </c:if> />
                </span>
            </td>
            <td>
                <span class="table_input_container">
                     <select name="RECORD.${formField.propertyName}.SCOPE" disabled="disabled">
                          <option value="${formField.scope}" selected="selected">
                              <c:choose>
                                  <c:when test="${formField.scope == 'RECORD'}">
                                  Record
                                  </c:when>
                                  <c:otherwise>
                                  Survey
                                  </c:otherwise>
                              </c:choose>
                          </option>
                     </select>
                </span>
             </td>
            <td></td>
            <td class="textcenter">
                <input type="checkbox" name="RECORD.${formField.propertyName}.HIDDEN" id="RECORD.${formField.propertyName}.HIDDEN" value="true" 
                    <c:choose>
                         <c:when test="${formField.hidden}"> checked="checked"</c:when>
                         <c:when test="<%= survey.getFormRendererType().equals(SurveyFormRendererType.SINGLE_SITE_ALL_TAXA) %>">
                             <c:if test="${ formField.propertyName == 'SPECIES' }"> title="You cannot hide this field on single site all species surveys" disabled="disabled"</c:if>
                         </c:when>
                     </c:choose>
                 />
            </td>    
        </c:when>
    </c:choose>
</tr>