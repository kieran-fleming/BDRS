<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<tiles:useAttribute name="formField" classname="au.com.gaiaresources.bdrs.controller.attribute.formfield.TypedAttributeValueFormField"/>
<tiles:useAttribute name="errorMap" classname="java.util.Map" ignore="true"/>
<tiles:useAttribute name="valueMap" classname="java.util.Map" ignore="true"/>
<tiles:useAttribute name="formPrefix" ignore="true"/>

<%@page import="java.lang.Boolean"%>
<%@page import="au.com.gaiaresources.bdrs.model.taxa.AttributeValue"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.GregorianCalendar"%>

<c:if test="${ formPrefix == null }">
    <c:set var="formPrefix" value="${ formField.prefix }"></c:set>
</c:if>

<c:if test="<%= errorMap != null && errorMap.containsKey(formField.getPrefix()+\"attribute_\"+formField.getAttribute().getId()) %>">
    <p class="error">
        <c:out value="<%= errorMap.get(formField.getPrefix()+\"attribute_\"+formField.getAttribute().getId()) %>"/>
    </p>
</c:if>

<c:choose>
    <c:when test="${ formField.attribute.type == 'STRING_WITH_VALID_VALUES'}">
        <select name="${ formPrefix }attribute_${ formField.attribute.id }"
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
            id="${ formPrefix }attribute_${ formField.attribute.id }"
            name="${ formPrefix }attribute_${ formField.attribute.id }"
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
    <c:when test="${ formField.attribute.type == 'HTML'}">
       <%-- Build tags from the attribute options --%>
       <div class="htmlContent">
       <c:forEach var="optopen" items="<%= formField.getAttribute().getOptions() %>">
          <jsp:useBean id="optopen" type="au.com.gaiaresources.bdrs.model.taxa.AttributeOption"/>
          <c:choose>
              <c:when test="<%= \"bold|italic|underline\".contains(optopen.getValue()) %>">
                  <<%= optopen.getValue().substring(0,1) %>>
              </c:when>
              <c:when test="<%= \"left|right\".contains(optopen.getValue()) %>">
                  <p align="<%= optopen.getValue() %>">
              </c:when>
              <c:when test="<%= \"center|h1|h2|h3|h4|h5|h6\".contains(optopen.getValue()) %>">
                  <<%= optopen.getValue() %>>
              </c:when>
          </c:choose>
       </c:forEach>
       <cw:validateHtml html="${formField.attribute.description}"/>
       <c:forEach var="optclose" items="<%= formField.getAttribute().getOptions() %>">
          <jsp:useBean id="optclose" type="au.com.gaiaresources.bdrs.model.taxa.AttributeOption"/>
              <c:choose>
                  <c:when test="<%= \"bold|italic|underline\".contains(optclose.getValue()) %>">
                      </<%= optclose.getValue().substring(0,1) %>>
                  </c:when>
                  <c:when test="<%= \"left|right\".contains(optclose.getValue()) %>">
                      </p>
                  </c:when>
                  <c:when test="<%= \"center|h1|h2|h3|h4|h5|h6\".contains(optclose.getValue()) %>">
                      </<%= optclose.getValue() %>>
                  </c:when>
              </c:choose>
       </c:forEach>
       </div>
    </c:when>
    <c:when test="${ formField.attribute.type == 'HTML_COMMENT'}">
       <center>
           <p><b>
               <c:forEach var="optopen2" items="<%= formField.getAttribute().getOptions() %>">
               <jsp:useBean id="optopen2" type="au.com.gaiaresources.bdrs.model.taxa.AttributeOption"/>
               <c:choose>
                   <c:when test="<%= \"bold|italic|underline\".contains(optopen2.getValue()) %>">
                       <<%= optopen2.getValue().substring(0,1) %>>
                   </c:when>
                   <c:when test="<%= \"left|right\".contains(optopen2.getValue()) %>">
                       <p align="<%= optopen2.getValue() %>">
                   </c:when>
                   <c:when test="<%= \"center|h1|h2|h3|h4|h5|h6\".contains(optopen2.getValue()) %>">
                       <<%= optopen2.getValue() %>>
                   </c:when>
               </c:choose>
            </c:forEach>
            <cw:validateHtml html="${formField.attribute.description}"/>
            <c:forEach var="optclose2" items="<%= formField.getAttribute().getOptions() %>">
               <jsp:useBean id="optclose2" type="au.com.gaiaresources.bdrs.model.taxa.AttributeOption"/>
                   <c:choose>
                       <c:when test="<%= \"bold|italic|underline\".contains(optclose2.getValue()) %>">
                           </<%= optclose2.getValue().substring(0,1) %>>
                       </c:when>
                       <c:when test="<%= \"left|right\".contains(optclose2.getValue()) %>">
                           </p>
                       </c:when>
                       <c:when test="<%= \"center|h1|h2|h3|h4|h5|h6\".contains(optclose2.getValue()) %>">
                           </<%= optclose2.getValue() %>>
                       </c:when>
                   </c:choose>
            </c:forEach>
           </b></p>
       </center>
    </c:when>
    <c:when test="${ formField.attribute.type == 'HTML_HORIZONTAL_RULE'}">
       <hr>
    </c:when>
    <c:when test="${ formField.attribute.type == 'STRING_AUTOCOMPLETE'}">
        <input type="text"
            id="${ formPrefix }attribute_${ formField.attribute.id }" 
            name="${ formPrefix }attribute_${ formField.attribute.id }" 
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
            id="${ formPrefix }attribute_${ formField.attribute.id }"
            name="${ formPrefix }attribute_${ formField.attribute.id }"
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
            id="${ formPrefix }attribute_${ formField.attribute.id }"
            name="${ formPrefix }attribute_${ formField.attribute.id }"
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
    <c:when test="${formField.attribute.type == 'BARCODE' || formField.attribute.type == 'REGEX'}">
        <c:set var="regex" value=""></c:set>
        <c:forEach var="opt" items="${formField.attribute.options}">
                   <c:choose>
                       <c:when test="${regex == ''}">
                           <c:set var="regex" value="${opt}"></c:set>
                       </c:when>
                       <c:otherwise>
                           <c:set var="regex" value="${regex}\\\\u002C${opt}"></c:set>
                       </c:otherwise>
                   </c:choose>
            </c:forEach>
        <c:set var="escapedRegex"><cw:regexEscaper regex="${regex}"/></c:set>
        
        <input type="text"
            id="${ formPrefix }attribute_${ formField.attribute.id }"
            name="${ formPrefix }attribute_${ formField.attribute.id }"
            <c:choose>
                <c:when test="<%= valueMap != null && valueMap.containsKey(formField.getPrefix()+\"attribute_\"+formField.getAttribute().getId()) %>">
                   value="<c:out value="<%= valueMap.get(formField.getPrefix()+\"attribute_\"+formField.getAttribute().getId()) %>"/>"
                </c:when>
                <c:when test="${ formField.attributeValue != null && formField.attributeValue.stringValue != null}">
                   value="<c:out value="<%= formField.getAttributeValue().getStringValue() %>"/>"
                </c:when>
            </c:choose>
            <c:choose>
                <c:when test="${ formField.attribute.required }">
                    class="validate(regExp(${escapedRegex}, ${regex}))"
                </c:when>
                <c:otherwise>
                     class="validate(regExpOrBlank(${escapedRegex}, ${regex}))"
                </c:otherwise>
            </c:choose>
        />
        
    </c:when>
    
    <c:when test="${ formField.attribute.type == 'DECIMAL'}">
        <input type="text"
            id="${ formPrefix }attribute_${ formField.attribute.id }"
            name="${ formPrefix }attribute_${ formField.attribute.id }"
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
        <textarea id="${ formPrefix }attribute_${ formField.attribute.id }"
            name="${ formPrefix }attribute_${ formField.attribute.id }"
            onkeypress = "return (jQuery(this).val().length <= 255)"
			<c:choose>
				<c:when test="${ formField.attribute.required }"> class="validate(required,maxlength(8191)" </c:when>
				<c:otherwise> class="validate(maxlength(8191))" </c:otherwise>
			</c:choose>
            
        ><c:choose><c:when test="<%= valueMap != null && valueMap.containsKey(formField.getPrefix()+\"attribute_\"+formField.getAttribute().getId()) %>"><c:out value="<%= valueMap.get(formField.getPrefix()+\"attribute_\"+formField.getAttribute().getId()) %>"/></c:when><c:when test="${ formField.attributeValue != null}"><c:out value="${ formField.attributeValue.stringValue}"/></c:when></c:choose></textarea>
    </c:when>
    <c:when test="${ formField.attribute.type == 'DATE'}">
        <input type="text"
            id="${ formPrefix }attribute_${ formField.attribute.id }"
            name="${ formPrefix }attribute_${ formField.attribute.id }"
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
    <c:when test="${ formField.attribute.type == 'TIME'}">
        <c:set var="cal" value="<%= new GregorianCalendar() %>"/>
        <jsp:useBean id="cal" type="java.util.Calendar"/>
        <div id="timeSelector" class="timeSelector">
            <% 
                Boolean selectTime = false;    
                if(formField.getAttribute() != null && formField.getAttributeValue() != null) {
                    String[] hourMin = formField.getAttributeValue().getStringValue().split(":");
                    if (hourMin.length == 2) {
                        selectTime = true;                
                        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hourMin[0]));
                        cal.set(Calendar.MINUTE, Integer.parseInt(hourMin[1]));
                    }
                }
            %>
            <select 
                id="${ formPrefix }attribute_time_hour_${ formField.attribute.id }"
                name="${ formPrefix }attribute_time_hour_${ formField.attribute.id }"
                class="hourSelector <c:if test="${ formField.attribute.required }">validate(required)</c:if>">
                <option value=""></option>
                <c:forEach var="hr" items="<%= new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23} %>">
                    <jsp:useBean id="hr" type="java.lang.Integer"/>
                    <option value="${ hr }"
                        <c:if test="<%= cal.get(Calendar.HOUR_OF_DAY) == hr && selectTime %>">
                            selected="selected"
                        </c:if>
                    >
                        <c:out value="<%= String.format(\"%02d\", hr) %>"/>
                    </option>
                </c:forEach>
            </select>
            
            <select 
                id="${ formPrefix }attribute_time_minute_${ formField.attribute.id }"
                name="${ formPrefix }attribute_time_minute_${ formField.attribute.id }"
                class="minuteSelector <c:if test="${ formField.attribute.required }">validate(required)</c:if>">
                <option value=""></option>
                <c:forEach var="min" items="<%= new int[]{0,5,10,15,20,25,30,35,40,45,50,55} %>">
                    <jsp:useBean id="min" type="java.lang.Integer"/>
                    <option value="${ min }"
                        <c:if test="<%= cal.get(Calendar.MINUTE) >= min && min+5 > cal.get(Calendar.MINUTE) && selectTime %>">
                            selected="selected"
                        </c:if>
                    >
                        <c:out value="<%= String.format(\"%02d\", min) %>"/>
                    </option>
                </c:forEach>
            </select>
        </div>
    </c:when>
    
    <c:when test="${ formField.attribute.type == 'SINGLE_CHECKBOX'}">
        <%-- No need to check if mandatory --%>
        <input type="checkbox" 
            id="${ formPrefix }attribute_${ formField.attribute.id }"
            class="vertmiddle singleCheckbox"
            name="${ formPrefix }attribute_${ formField.attribute.id }"
            value="true"
            <c:choose>
                <c:when test="<%= valueMap != null && valueMap.containsKey(formField.getPrefix()+\"attribute_\"+formField.getAttribute().getId()) %>">
                    <c:if test="<%= Boolean.parseBoolean(valueMap.get(formField.getPrefix()+\"attribute_\"+formField.getAttribute().getId()).toString()) %>">
                        checked="checked"
                    </c:if>
                </c:when>
                <c:when test="${ formField.attributeValue != null}">
                    <c:if test="${ formField.attributeValue.booleanValue}">
                        checked="checked"
                    </c:if>
                </c:when>
            </c:choose>
        />
    </c:when>
    
    <c:when test="${ formField.attribute.type == 'MULTI_CHECKBOX'}">
        <div class="multiCheckboxOpts">
        <c:forEach var="multiCbOpt" items="${ formField.attribute.options }" varStatus="multiCbStatus">
            <jsp:useBean id="multiCbOpt" type="au.com.gaiaresources.bdrs.model.taxa.AttributeOption"/>
            <jsp:useBean id="multiCbStatus" type="javax.servlet.jsp.jstl.core.LoopTagStatus"/>
            <div>
                <input type="checkbox" 
                    id="${ formPrefix }attribute_${ formField.attribute.id }_${multiCbStatus.index}"
                    class="vertmiddle multiCheckbox"
                    name="${ formPrefix }attribute_${ formField.attribute.id }"
                    value='<c:out value="${ multiCbOpt.value }"/>'
                    <c:choose>
                        <c:when test="<%= valueMap != null && valueMap.containsKey(formField.getPrefix()+\"attribute_\"+formField.getAttribute().getId()) && valueMap.get(formField.getPrefix()+\"attribute_\"+formField.getAttribute().getId()).toString().contains(multiCbOpt.getValue()) %>">
                            checked="checked"
                        </c:when>
                        <c:when test="${ formField.attributeValue != null}">
                            <c:if test="<%= formField.getAttributeValue().hasMultiCheckboxValue(multiCbOpt.getValue()) %>">
                                checked="checked"
                            </c:if>
                        </c:when>
                    </c:choose>
                    onchange="var inp=jQuery('#${ formPrefix }attribute_${ formField.attribute.id }'); inp.val(jQuery('input[name=${ formPrefix }attribute_${ formField.attribute.id }]:checked').serialize()); inp.blur();"
                />
                <label for="${ formPrefix }attribute_${ formField.attribute.id }_${multiCbStatus.index}" class="multiCheckboxLabel">
                    <c:out value="${ multiCbOpt.value }"/>
                </label>
            </div>
        </c:forEach>
        </div>
        <div style="line-height: 0em;">
            <%-- Value for this input is populate via javascript after the checkboxes --%>
            <input type="text"
                id="${ formPrefix }attribute_${ formField.attribute.id }" 
                <c:if test="${ formField.attribute.required }">
                    class="validate(required)"
                </c:if>
                style="visibility: hidden;height: 0em;"
            />
        </div>
        <script type="text/javascript">
            <%-- 
              The following snippet sets the initial value for the hidden input.
              This was not done using JSP because JSP tends to insert whitespace
              into the value. 
              
              It is ok if the browser does not have javascript enabled because 
              the hidden input is only present to support ketchup validation.
            --%>
            jQuery(function() {
                var hidden = jQuery('#${ formPrefix }attribute_${ formField.attribute.id }');
                var checkedInputs = jQuery('input[name=${ formPrefix }attribute_${ formField.attribute.id }]:checked'); 
                hidden.val(checkedInputs.serialize());
            });
        </script>
    </c:when>
    <c:when test="${ formField.attribute.type == 'MULTI_SELECT'}">
        <select multiple="multiple"
                id="${ formPrefix }attribute_${ formField.attribute.id }"
                name="${ formPrefix }attribute_${ formField.attribute.id }"
                <c:if test="${ formField.attribute.required }">
                    class="validate(required)"
                </c:if>
            >
            <c:forEach var="multiSelectOpt" items="${ formField.attribute.options }" varStatus="multiSelectStatus">
                <jsp:useBean id="multiSelectOpt" type="au.com.gaiaresources.bdrs.model.taxa.AttributeOption"/>
                <jsp:useBean id="multiSelectStatus" type="javax.servlet.jsp.jstl.core.LoopTagStatus"/>                
                <option value="<c:out value="${ multiSelectOpt.value }"/>"
                    <c:choose>
                        <c:when test="<%= valueMap != null && valueMap.containsKey(formField.getPrefix()+\"attribute_\"+formField.getAttribute().getId()) && valueMap.get(formField.getPrefix()+\"attribute_\"+formField.getAttribute().getId()).toString().contains(multiSelectOpt.getValue()) %>">
                            selected="selected"
                        </c:when>
                        <c:when test="${ formField.attributeValue != null}">
                            <c:if test="<%= formField.getAttributeValue().hasMultiCheckboxValue(multiSelectOpt.getValue()) %>">
                                selected="selected"
                            </c:if>
                        </c:when>
                    </c:choose>
                >
                    <c:out value="${ multiSelectOpt.value }"/>
                </option>
            </c:forEach>
        </select>
    </c:when>
    <c:when test="${ formField.attribute.type == 'IMAGE'}">
        <c:if test="${ formField.attributeValue != null && formField.attributeValue.stringValue != null }">
            <div id="${ formPrefix }attribute_img_${ formField.attribute.id }">
                <a href="${pageContext.request.contextPath}/files/download.htm?<%= formField.getAttributeValue().getFileURL() %>">
                    <img width="250"
                        src="${pageContext.request.contextPath}/files/download.htm?<%= formField.getAttributeValue().getFileURL() %>"
                        alt="Missing Image"/>
                </a>
            </div>
        </c:if>
        <div style="line-height: 0em;">
            <input type="text"
                id="${ formPrefix }attribute_${ formField.attribute.id }"
                name="${ formPrefix }attribute_${ formField.attribute.id }"
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
            id="${ formPrefix }attribute_file_${ formField.attribute.id }"
            name="${ formPrefix }attribute_file_${ formField.attribute.id }"
            class="image_file"
            onchange="bdrs.util.file.imageFileUploadChangeHandler(this);jQuery('#${ formPrefix }attribute_${ formField.attribute.id }').val(jQuery(this).val());"
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
                    class: "clearLink"
                });
                elem.text('Clear');
                elem.click(function() {
                    jQuery('#${ formPrefix }attribute_${ formField.attribute.id }, #${ formPrefix }attribute_file_${ formField.attribute.id }').attr('value','');
                    jQuery('#${ formPrefix }attribute_img_${ formField.attribute.id }').remove();
                });
                jQuery('[name=${ formPrefix }attribute_file_${ formField.attribute.id }]').after(elem);
            });
        </script>
    </c:when>
    <c:when test="${ formField.attribute.type == 'FILE' }">
        <c:if test="${ formField.attributeValue != null && formField.attributeValue.stringValue != null }">
            <div id="${ formPrefix }attribute_data_${ formField.attribute.id }">
                <a href="${pageContext.request.contextPath}/files/download.htm?<%= formField.getAttributeValue().getFileURL() %>">
                    <c:out value="${ formField.attributeValue.stringValue }"/>
                </a>
            </div>
        </c:if>
        <div style="line-height: 0em;">
            <input type="text"
                id="${ formPrefix }attribute_${ formField.attribute.id }"
                name="${ formPrefix }attribute_${ formField.attribute.id }"
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
            id="${ formPrefix }attribute_file_${ formField.attribute.id }"
            name="${ formPrefix }attribute_file_${ formField.attribute.id }"
            class="data_file"
            onchange="jQuery('#${ formPrefix }attribute_${ formField.attribute.id }').val(jQuery(this).val());"
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
                    class: "clearLink"
                });
                elem.text('Clear');
                elem.click(function() {
                    jQuery('#${ formPrefix }attribute_${ formField.attribute.id }, #${ formPrefix }attribute_file_${ formField.attribute.id }').attr('value','');
                    jQuery('#${ formPrefix }attribute_data_${ formField.attribute.id }').remove();
                });
                jQuery('[name=${ formPrefix }attribute_file_${ formField.attribute.id }]').after(elem);
            });
        </script>
    </c:when>
</c:choose>
