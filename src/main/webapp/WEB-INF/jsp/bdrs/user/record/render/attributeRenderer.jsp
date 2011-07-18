<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<tiles:useAttribute name="formField" classname="au.com.gaiaresources.bdrs.controller.attribute.formfield.TypedAttributeValueFormField"/>
<tiles:useAttribute name="errorMap" classname="java.util.Map" ignore="true"/>
<tiles:useAttribute name="valueMap" classname="java.util.Map" ignore="true"/>

<%@page import="au.com.gaiaresources.bdrs.model.taxa.AttributeValue"%>

<c:if test="<%= errorMap != null && errorMap.containsKey(formField.getPrefix()+\"attribute_\"+formField.getAttribute().getId()) %>">
    <p class="error">
        <c:out value="<%= errorMap.get(formField.getPrefix()+\"attribute_\"+formField.getAttribute().getId()) %>"/>
    </p>
</c:if>

<c:choose>
    <c:when test="${ formField.attribute.type == 'STRING_WITH_VALID_VALUES'}">
        <select name="${ formField.prefix }attribute_${ formField.attribute.id }"
            <c:if test="${ formField.attribute.required }">
                class="validate(required)"
            </c:if>
        >
            <c:if test="${ not formField.attribute.required }">
                <option></option>
            </c:if>
            <c:forEach var="attrOpt" items="${ formField.attribute.options }">
                <jsp:useBean id="attrOpt" type="au.com.gaiaresources.bdrs.model.taxa.AttributeOption"/>
                <option
                    <c:choose>
			            <c:when test="<%= valueMap != null && attrOpt.getValue().equals(valueMap.get(formField.getPrefix()+\"attribute_\"+formField.getAttribute().getId())) %>">
			                selected="selected"
			            </c:when>
			            <c:when test="${ formField.attributeValue.stringValue == attrOpt.value }">
			                selected="selected"
			            </c:when>
			        </c:choose>
                >
                    <c:out value="${ attrOpt.value }"/>
                </option>
            </c:forEach>
        </select>
    </c:when>
    <c:when test="${ formField.attribute.type == 'STRING' }">
        <input type="text"
            id="${ formField.prefix }attribute_${ formField.attribute.id }"
            name="${ formField.prefix }attribute_${ formField.attribute.id }"
            maxlength="255"
            <c:choose>
	            <c:when test="<%= valueMap != null && valueMap.containsKey(formField.getPrefix()+\"attribute_\"+formField.getAttribute().getId()) %>">
	               value="<c:out value="<%= valueMap.get(formField.getPrefix()+\"attribute_\"+formField.getAttribute().getId()) %>"/>"
	            </c:when>
	            <c:when test="${ formField.attributeValue != null}">
    	            value="<c:out value="${ formField.attributeValue.stringValue}"/>"
	            </c:when>
	        </c:choose>
            <c:if test="${ formField.attribute.required }">
            	class="validate(required)"
            </c:if>
        />
    </c:when>
    <c:when test="${ formField.attribute.type == 'STRING_AUTOCOMPLETE'}">
        <input type="text"
            id="${ formField.prefix }attribute_${ formField.attribute.id }"
            name="${ formField.prefix }attribute_${ formField.attribute.id }"
            maxlength="255"
            <c:choose>
                <c:when test="<%= valueMap != null && valueMap.containsKey(formField.getPrefix()+\"attribute_\"+formField.getAttribute().getId()) %>">
                   value="<c:out value="<%= valueMap.get(formField.getPrefix()+\"attribute_\"+formField.getAttribute().getId()) %>"/>"
                </c:when>
                <c:when test="${ formField.attributeValue != null}">
                    value="<c:out value="${ formField.attributeValue.stringValue}"/>"
                </c:when>
            </c:choose>
            <c:choose>
            	<c:when test="${ formField.attribute.required }">
                	class="validate(required) acomplete"
                </c:when>
                <c:otherwise>
                	class="acomplete"
                </c:otherwise>
            </c:choose>
        />
    </c:when>
    <c:when test="${ formField.attribute.type == 'INTEGER'}">
        <input type="text"
            id="${ formField.prefix }attribute_${ formField.attribute.id }"
            name="${ formField.prefix }attribute_${ formField.attribute.id }"
            <c:choose>
			    <c:when test="<%= valueMap != null && valueMap.containsKey(formField.getPrefix()+\"attribute_\"+formField.getAttribute().getId()) %>">
			       value="<c:out value="<%= valueMap.get(formField.getPrefix()+\"attribute_\"+formField.getAttribute().getId()) %>"/>"
			    </c:when>
			    <c:when test="${ formField.attributeValue != null && formField.attributeValue.numericValue != null}">
			       value="<c:out value="<%= formField.getAttributeValue().getNumericValue().intValue() %>"/>"
			    </c:when>
			</c:choose>
            <c:choose>
                <c:when test="${ formField.attribute.required }">
                    class="validate(integer)"
                </c:when>
                <c:otherwise>
                    class="validate(integerOrBlank)"
                </c:otherwise>
            </c:choose>
        />
    </c:when>
     <c:when test="${ formField.attribute.type == 'INTEGER_WITH_RANGE'}">
        <input type="text"
            id="${ formField.prefix }attribute_${ formField.attribute.id }"
            name="${ formField.prefix }attribute_${ formField.attribute.id }"
            <c:choose>
			    <c:when test="<%= valueMap != null && valueMap.containsKey(formField.getPrefix()+\"attribute_\"+formField.getAttribute().getId()) %>">
			       value="<c:out value="<%= valueMap.get(formField.getPrefix()+\"attribute_\"+formField.getAttribute().getId()) %>"/>"
			    </c:when>
			    <c:when test="${ formField.attributeValue != null && formField.attributeValue.numericValue != null}">
			       value="<c:out value="<%= formField.getAttributeValue().getNumericValue().intValue() %>"/>"
			    </c:when>
			</c:choose>
            <c:choose>
                <c:when test="${ formField.attribute.required }">
                    class="validate(range(<c:out value="${formField.attribute.options[0]}"/>,<c:out value="${formField.attribute.options[1]}"/>), number)"
                </c:when>
                <c:otherwise>
                     class="validate(rangeOrBlank(<c:out value="${formField.attribute.options[0]}"/>,<c:out value="${formField.attribute.options[1]}"/>), numberOrBlank)"
                </c:otherwise>
            </c:choose>
        />
        
    </c:when>
    <c:when test="${ formField.attribute.type == 'DECIMAL'}">
        <input type="text"
            id="${ formField.prefix }attribute_${ formField.attribute.id }"
            name="${ formField.prefix }attribute_${ formField.attribute.id }"
            <c:choose>
                <c:when test="<%= valueMap != null && valueMap.containsKey(formField.getPrefix()+\"attribute_\"+formField.getAttribute().getId()) %>">
                   value="<c:out value="<%= valueMap.get(formField.getPrefix()+\"attribute_\"+formField.getAttribute().getId()) %>"/>"
                </c:when>
                <c:when test="${ formField.attributeValue != null && formField.attributeValue.numericValue != null}">
                   value="<c:out value="<%= formField.getAttributeValue().getNumericValue().doubleValue() %>"/>"
                </c:when>
            </c:choose>
            <c:choose>
                <c:when test="${ formField.attribute.required }">
                    class="validate(number)"
                </c:when>
                <c:otherwise>
                    class="validate(numberOrBlank)"
                </c:otherwise>
            </c:choose>
        />
    </c:when>
    <c:when test="${ formField.attribute.type == 'TEXT'}">
        <textarea id="${ formField.prefix }attribute_${ formField.attribute.id }"
            name="${ formField.prefix }attribute_${ formField.attribute.id }"
            onkeypress = "return (jQuery(this).val().length <= 255)"
            <c:if test="${ formField.attribute.required }">
                class="validate(required)"
            </c:if>
        ><c:choose><c:when test="<%= valueMap != null && valueMap.containsKey(formField.getPrefix()+\"attribute_\"+formField.getAttribute().getId()) %>"><c:out value="<%= valueMap.get(formField.getPrefix()+\"attribute_\"+formField.getAttribute().getId()) %>"/></c:when><c:when test="${ formField.attributeValue != null}"><c:out value="${ formField.attributeValue.stringValue}"/></c:when></c:choose></textarea>
    </c:when>
    <c:when test="${ formField.attribute.type == 'DATE'}">
        <input type="text"
            id="${ formField.prefix }attribute_${ formField.attribute.id }"
            name="${ formField.prefix }attribute_${ formField.attribute.id }"
            class="datepicker <c:if test="${ formField.attribute.required }">validate(required)</c:if>"
            <c:choose>
                <c:when test="<%= valueMap != null && valueMap.containsKey(formField.getPrefix()+\"attribute_\"+formField.getAttribute().getId()) %>">
                    value="<c:out value="<%= valueMap.get(formField.getPrefix()+\"attribute_\"+formField.getAttribute().getId()) %>"/>"
                </c:when>
                <c:when test="${ formField.attributeValue != null }">
                    value="<fmt:formatDate pattern="dd MMM yyyy" value="${ formField.attributeValue.dateValue }"/>"
                </c:when>
            </c:choose>
        />
    </c:when>
    <c:when test="${ formField.attribute.type == 'IMAGE'}">
        <c:if test="${ formField.attributeValue != null && formField.attributeValue.stringValue != null }">
            <div id="${ formField.prefix }attribute_img_${ formField.attribute.id }">
                <a href="${pageContext.request.contextPath}/files/download.htm?<%= formField.getAttributeValue().getFileURL() %>">
                    <img width="250"
                        src="${pageContext.request.contextPath}/files/download.htm?<%= formField.getAttributeValue().getFileURL() %>"
                        alt="Missing Image"/>
                </a>
            </div>
        </c:if>
        <div style="line-height: 0em;">
            <input type="text"
                id="${ formField.prefix }attribute_${ formField.attribute.id }"
                name="${ formField.prefix }attribute_${ formField.attribute.id }"
                style="visibility: hidden;height: 0em;"
                <c:if test="${ formField.attribute.required }">
                    class="validate(required)"
                </c:if>
                <c:if test="${ formField.attributeValue != null}">
                    value="<c:out value="${ formField.attributeValue.stringValue}"/>"
                </c:if>
            />
        </div>
        <input type="file"
            accept="image/gif,image/jpeg,image/png"
            id="${ formField.prefix }attribute_file_${ formField.attribute.id }"
            name="${ formField.prefix }attribute_file_${ formField.attribute.id }"
            class="image_file"
            onchange="bdrs.util.file.imageFileUploadChangeHandler(this);jQuery('#${ formField.prefix }attribute_${ formField.attribute.id }').val(jQuery(this).val());"
        />
        <script type="text/javascript">
            /**
             * This script creates a link that clears the image from the record
             * attribute. The link should only appear if javascript is enabled 
             * on the browser so we create an insert the script using javascript.
             * No script... no link.
             */
            jQuery(function() {
                var elem = jQuery("<a></a>");
                elem.attr({
                    href: "javascript: void(0)",
                });
                elem.text('Clear');
                elem.click(function() {
                    jQuery('#${ formField.prefix }attribute_${ formField.attribute.id }, #${ formField.prefix }attribute_file_${ formField.attribute.id }').attr('value','');
                    jQuery('#${ formField.prefix }attribute_img_${ formField.attribute.id }').remove();
                });
                jQuery('[name=${ formField.prefix }attribute_file_${ formField.attribute.id }]').after(elem);
            });
        </script>
    </c:when>
    <c:when test="${ formField.attribute.type == 'FILE' }">
        <c:if test="${ formField.attributeValue != null && formField.attributeValue.stringValue != null }">
            <div id="${ formField.prefix }attribute_data_${ formField.attribute.id }">
                <a href="${pageContext.request.contextPath}/files/download.htm?<%= formField.getAttributeValue().getFileURL() %>">
                    <c:out value="${ formField.attributeValue.stringValue }"/>
                </a>
            </div>
        </c:if>
        <div style="line-height: 0em;">
            <input type="text"
                id="${ formField.prefix }attribute_${ formField.attribute.id }"
                name="${ formField.prefix }attribute_${ formField.attribute.id }"
                style="visibility: hidden;height: 0em;border:none;"
                <c:if test="${ formField.attribute.required }">
                    class="validate(required)"
                </c:if>
                <c:if test="${ formField.attributeValue != null }">
                    value="<c:out value="${ formField.attributeValue.stringValue }"/>"
                </c:if>
            />
        </div>
        <input type="file"
            id="${ formField.prefix }attribute_file_${ formField.attribute.id }"
            name="${ formField.prefix }attribute_file_${ formField.attribute.id }"
            class="data_file"
            onchange="jQuery('#${ formField.prefix }attribute_${ formField.attribute.id }').val(jQuery(this).val());"
        />
        <script type="text/javascript">
            /**
             * This script creates a link that clears the image from the record
             * attribute. The link should only appear if javascript is enabled 
             * on the browser so we create an insert the script using javascript.
             * No script... no link.
             */
            jQuery(function() {
                var elem = jQuery("<a></a>");
                elem.attr({
                    href: "javascript: void(0)",
                });
                elem.text('Clear');
                elem.click(function() {
                    jQuery('#${ formField.prefix }attribute_${ formField.attribute.id }, #${ formField.prefix }attribute_file_${ formField.attribute.id }').attr('value','');
                    jQuery('#${ formField.prefix }attribute_data_${ formField.attribute.id }').remove();
                });
                jQuery('[name=${ formField.prefix }attribute_file_${ formField.attribute.id }]').after(elem);
            });
        </script>
    </c:when>
</c:choose>
