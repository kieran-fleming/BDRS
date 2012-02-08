<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<tiles:useAttribute name="formField" classname="au.com.gaiaresources.bdrs.controller.attribute.formfield.TypedAttributeValueFormField"/>
<tiles:useAttribute name="errorMap" classname="java.util.Map" ignore="true"/>
<tiles:useAttribute name="valueMap" classname="java.util.Map" ignore="true"/>
<tiles:useAttribute name="formPrefix" ignore="true"/>
<tiles:useAttribute name="editEnabled" ignore="true"/>

<%@page import="java.lang.Boolean"%>
<%@page import="au.com.gaiaresources.bdrs.model.taxa.AttributeValue"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.GregorianCalendar"%>

<c:set var="inputName" value="<%= formField.getPrefix()+\"attribute_\"+formField.getAttribute().getId() %>" />
<c:set var="fileInputName" value="<%= formField.getPrefix()+\"attribute_file_\"+formField.getAttribute().getId() %>" />
<c:set var="sectionName" value="<%= formField.getPrefix()+\"attribute_data_\"+formField.getAttribute().getId() %>" />

<%-- we use inputName in scriptlets so we need to 'usebean' the variable... --%>
<jsp:useBean id="inputName" type="java.lang.String"/>

<c:if test="${ formPrefix == null }">
    <c:set var="formPrefix" value="${ formField.prefix }"></c:set>
</c:if>

<c:if test="<%= errorMap != null && errorMap.containsKey(inputName) %>">
    <p class="error">
        <c:out value="<%= errorMap.get(inputName) %>"/>
    </p>
</c:if>

<%-- put the global 'edit form' bool into a local variable... --%>
<c:set var="fieldEditable" value="${editEnabled}"></c:set>

<c:choose>
    <c:when test="<%= valueMap != null && valueMap.containsKey(inputName) %>">
        <c:set var="fieldValue" value="<%= valueMap.get(inputName) %>"></c:set>
    </c:when>
    <c:when test="${ formField.attributeValue != null}">
        <c:set var="fieldValue" value="<%= formField.getAttributeValue().toString() %>"></c:set>
    </c:when>
</c:choose>

<c:choose>
    <c:when test="${ formField.attribute.type == 'STRING_WITH_VALID_VALUES'}">
		<c:choose>
            <c:when test="${fieldEditable}">
                <select name="${inputName}"
		            <c:if test="${ formField.attribute.required }">
		                class="validate(required)"
		            </c:if>
		        >
		            <c:if test="${ not formField.attribute.required }">
		                <option></option>
		            </c:if>
		            <c:forEach var="attrOpt" items="${ formField.attribute.options }">
		                <jsp:useBean id="attrOpt" type="au.com.gaiaresources.bdrs.model.taxa.AttributeOption"/>
		                <option <c:if test="${attrOpt.value == fieldValue}">selected="selected"</c:if> >
		                    <c:out value="${ attrOpt.value }"/>
		                </option>
		            </c:forEach>
		        </select>
            </c:when>
            <c:otherwise>
                <span><c:out value="${ fieldValue }"/></span>
            </c:otherwise>
        </c:choose>
    </c:when>
    <c:when test="${ formField.attribute.type == 'STRING' }">
		<c:choose>
			<c:when test="${fieldEditable}">
				<input type="text"
		            id="${inputName}"
		            name="${inputName}"
		            maxlength="255"
		            value="<c:out value="${ fieldValue }"/>"
		            <c:if test="${ formField.attribute.required }">
		                class="validate(required)"
		            </c:if>
		        />
			</c:when>
			<c:otherwise>
				<span><c:out value="${ fieldValue }"/></span>
			</c:otherwise>
		</c:choose>
    </c:when>
    <c:when test="${ formField.attribute.type == 'HTML' || formField.attribute.type == 'HTML_NO_VALIDATION'}">
       <%-- Build tags from the attribute options --%>
       <c:if test="${ formField.attribute.type == 'HTML'}">
       <div class="htmlContent">
       </c:if>
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
       <c:choose>
       <c:when test="${ formField.attribute.type == 'HTML'}">
           <cw:validateHtml html="${formField.attribute.description}"/>
       </c:when>
       <c:when test="${ formField.attribute.type == 'HTML_NO_VALIDATION'}">
           ${formField.attribute.description}
       </c:when>
       </c:choose>
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
       <c:if test="${ formField.attribute.type == 'HTML'}">
       </div>
       </c:if>
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
		<c:choose>
		    <c:when test="${fieldEditable}">
		        <input type="text"
		            id="${inputName}" 
		            name="${inputName}" 
		            maxlength="255" 
		            value="<c:out value="${ fieldValue }"/>"
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
		    <c:otherwise>
		        <span><c:out value="${ fieldValue }"/></span>
		    </c:otherwise>
		</c:choose>
    </c:when>
    <c:when test="${ formField.attribute.type == 'INTEGER'}">
		<c:choose>
		    <c:when test="${fieldEditable}">
		        <input type="text"
		            id="${inputName}"
		            name="${inputName}"
		            value="<c:out value="${ fieldValue }"/>"
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
		    <c:otherwise>
		        <span><c:out value="${ fieldValue }"/></span>
		    </c:otherwise>
		</c:choose>
    </c:when>
     <c:when test="${ formField.attribute.type == 'INTEGER_WITH_RANGE'}">
		<c:choose>
		    <c:when test="${fieldEditable}">
		        <input type="text" id="${inputName}" name="${inputName}" value="<c:out value="${ fieldValue }"/>" 
		            <c:choose>
		                <c:when test="${ formField.attribute.required }">
		                      <c:choose>
		                          <c:when test="<%= formField.getAttribute().getOptions().size() >= 2 %>">
		                              class="validate(range(<c:out value="${formField.attribute.options[0]}"/>,<c:out value="${formField.attribute.options[1]}"/>), integer)"
		                          </c:when>
		                          <c:otherwise>
		                              class="validate(integer)"
		                          </c:otherwise>
		                      </c:choose>
		                      
		                </c:when>
		                <c:otherwise>
		                     <c:choose>
		                          <c:when test="<%= formField.getAttribute().getOptions().size() >= 2 %>">
		                              class="validate(rangeOrBlank(<c:out value="${formField.attribute.options[0]}"/>,<c:out value="${formField.attribute.options[1]}"/>), integerOrBlank)"
		                          </c:when>
		                          <c:otherwise>
		                              class="validate(integerOrBlank)"
		                          </c:otherwise>
		                      </c:choose>
		                </c:otherwise>
		            </c:choose>
		        />
		    </c:when>
		    <c:otherwise>
		        <span><c:out value="${ fieldValue }"/></span>
		    </c:otherwise>
		</c:choose>
    </c:when>
    <c:when test="${formField.attribute.type == 'BARCODE' || formField.attribute.type == 'REGEX'}">
		<c:choose>
		    <c:when test="${fieldEditable}">
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
			            id="${inputName}"
			            name="${inputName}"
			            value="<c:out value="${ fieldValue }"/>"
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
		    <c:otherwise>
		        <span><c:out value="${ fieldValue }"/></span>
		    </c:otherwise>
		</c:choose>
    </c:when>
    <c:when test="${ formField.attribute.type == 'DECIMAL'}">
		<c:choose>
		    <c:when test="${fieldEditable}">
		        <input type="text"
		            id="${inputName}"
		            name="${inputName}"
		            value="<c:out value="${ fieldValue }"/>"
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
		    <c:otherwise>
		        <span><c:out value="${ fieldValue }"/></span>
		    </c:otherwise>
		</c:choose>
    </c:when>
    <c:when test="${ formField.attribute.type == 'TEXT'}">
		<c:choose>
		    <c:when test="${fieldEditable}">
		        <textarea id="${inputName}"
            name="${inputName}"
            onkeypress = "return (jQuery(this).val().length <= 255)"
            <c:choose>
                <c:when test="${ formField.attribute.required }"> class="validate(required,maxlength(8191)" </c:when>
                <c:otherwise> class="validate(maxlength(8191))" </c:otherwise>
            </c:choose>
            ><c:out value="${ fieldValue }"/></textarea>
		    </c:when>
		    <c:otherwise>
		        <span><c:out value="${ fieldValue }"/></span>
		    </c:otherwise>
		</c:choose>
    </c:when>
    <c:when test="${ formField.attribute.type == 'DATE'}">
		<c:choose>
		    <c:when test="${fieldEditable}">
		        <input type="text"
		            id="${inputName}"
		            name="${inputName}"
		            class="datepicker <c:if test="${ formField.attribute.required }">validate(required)</c:if>"
		            value="<c:out value="${ fieldValue }"/>"
		        />
		    </c:when>
		    <c:otherwise>
		        <span><c:out value="${ fieldValue }"/></span>
		    </c:otherwise>
		</c:choose>
    </c:when>
    <c:when test="${ formField.attribute.type == 'TIME'}">
		<c:choose>
		    <c:when test="${fieldEditable}">
			    <input type="text" name="${ inputName }" 
                class="timepicker
                <c:choose>
                    <c:when test="${ formField.attribute.required }">
                        validate(time)
                    </c:when>
                    <c:otherwise>
                        validate(timeOrBlank)
                    </c:otherwise>
                </c:choose>
                "  <%-- end of the class attribute --%>
	            value="<c:out value="${ fieldValue }"/>"
	             />  <%-- end of the time input field --%>
		    </c:when>
		    <c:otherwise>
		        <span><c:out value="${ fieldValue }"/></span>
		    </c:otherwise>
		</c:choose>
    </c:when>
    <c:when test="${ formField.attribute.type == 'SINGLE_CHECKBOX'}">
    	<c:choose>
		    <c:when test="${fieldEditable}">
		        <%-- No need to check if mandatory --%>
		        <input type="checkbox" 
		            id="${inputName}"
		            class="vertmiddle singleCheckbox"
		            name="${inputName}"
		            value="true"
		            <c:choose>
		                <c:when test="<%= valueMap != null && valueMap.containsKey(inputName) %>">
		                    <c:if test="<%= Boolean.parseBoolean(valueMap.get(inputName).toString()) %>">
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
		    <c:otherwise>
				<c:choose>
				    <c:when test="${fieldValue == 'true'}">
				        <span><img src="${pageContext.request.contextPath}/images/vanilla/icon_tick_green.png" /></span>
				    </c:when>
				    <c:otherwise>
				        <span><img src="${pageContext.request.contextPath}/images/vanilla/icon_cross_red.png" /></span>
				    </c:otherwise>
				</c:choose>
		    </c:otherwise>
		</c:choose>
    </c:when>
    <c:when test="${ formField.attribute.type == 'MULTI_CHECKBOX'}">
		<c:choose>
		    <c:when test="${fieldEditable}">
				<div class="multiCheckboxOpts">
		        <c:forEach var="multiCbOpt" items="${ formField.attribute.options }" varStatus="multiCbStatus">
					<jsp:useBean id="multiCbOpt" type="au.com.gaiaresources.bdrs.model.taxa.AttributeOption"/>
                    <jsp:useBean id="multiCbStatus" type="javax.servlet.jsp.jstl.core.LoopTagStatus"/>  
		            <div>
		                <input type="checkbox" 
		                    id="${inputName}_${multiCbStatus.index}"
		                    class="vertmiddle multiCheckbox"
		                    name="${inputName}"
		                    value='<c:out value="${ multiCbOpt.value }"/>'
		                    <c:choose>
		                        <c:when test="<%= valueMap != null && valueMap.containsKey(inputName) && valueMap.get(inputName).toString().contains(multiCbOpt.getValue()) %>">
		                            checked="checked"
		                        </c:when>
		                        <c:when test="${ formField.attributeValue != null}">
		                            <c:if test="<%= formField.getAttributeValue().hasMultiCheckboxValue(multiCbOpt.getValue()) %>">
		                                checked="checked"
		                            </c:if>
		                        </c:when>
		                    </c:choose>
		                    onchange="var inp=jQuery('#${inputName}'); inp.val(jQuery('input[name=${inputName}]:checked').serialize()); inp.blur();"
		                />
		                <label for="${inputName}_${multiCbStatus.index}" class="multiCheckboxLabel">
		                    <c:out value="${ multiCbOpt.value }"/>
		                </label>
		            </div>
		        </c:forEach>
		        </div>
		        <div style="line-height: 0em;">
		            <%-- Value for this input is populate via javascript after the checkboxes --%>
		            <input type="text"
		                id="${inputName}" 
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
		                var hidden = jQuery('#${inputName}');
		                var checkedInputs = jQuery('input[name=${inputName}]:checked'); 
		                hidden.val(checkedInputs.serialize());
		            });
		        </script>
		    </c:when>
		    <c:otherwise>
				<%-- need to rename multi* items here or the jsp parser thinks we are defining the same name twice --%>
                <div class="multiCheckboxOpts">
                <c:forEach var="multiCbOpt2" items="${ formField.attribute.options }" varStatus="multiCbStatus2">
					<jsp:useBean id="multiCbOpt2" type="au.com.gaiaresources.bdrs.model.taxa.AttributeOption"/>
                    <jsp:useBean id="multiCbStatus2" type="javax.servlet.jsp.jstl.core.LoopTagStatus"/>
					<c:set var="multiOptValue" value="<%= multiCbOpt2.getValue() %>" />
                    <div>
                    	<c:choose>
							<c:when test="${cw:hasCsvValue(fieldValue, multiOptValue)}">
                                <span><img src="${pageContext.request.contextPath}/images/vanilla/icon_tick_green.png" /></span>                                            
                            </c:when>
                            <c:otherwise>
                                <span><img src="${pageContext.request.contextPath}/images/vanilla/icon_cross_red.png" /></span>
                            </c:otherwise>
                        </c:choose>
						
                        <label for="${inputName}_${multiCbStatus2.index}" class="multiCheckboxLabel">
                            <c:out value="${ multiCbOpt2.value }"/>
                        </label>
                    </div>
                </c:forEach>
                </div>
		    </c:otherwise>
		</c:choose>
    </c:when>
    <c:when test="${ formField.attribute.type == 'MULTI_SELECT'}">
		<c:choose>
		    <c:when test="${fieldEditable}">
				<select multiple="multiple"
		                id="${inputName}"
		                name="${inputName}"
		                <c:if test="${ formField.attribute.required }">
		                    class="validate(required)"
		                </c:if>
		            >
		            <c:forEach var="multiSelectOpt" items="${ formField.attribute.options }" varStatus="multiSelectStatus">
		                <jsp:useBean id="multiSelectOpt" type="au.com.gaiaresources.bdrs.model.taxa.AttributeOption"/>
		                <jsp:useBean id="multiSelectStatus" type="javax.servlet.jsp.jstl.core.LoopTagStatus"/>                
		                <option value="<c:out value="${ multiSelectOpt.value }"/>"
		                    <c:choose>
		                        <c:when test="<%= valueMap != null && valueMap.containsKey(inputName) && valueMap.get(inputName).toString().contains(multiSelectOpt.getValue()) %>">
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
		    <c:otherwise>
				<c:forEach var="multiSelectOpt2" items="${ formField.attribute.options }" varStatus="multiSelectStatus2">
					<div>
	                    <jsp:useBean id="multiSelectOpt2" type="au.com.gaiaresources.bdrs.model.taxa.AttributeOption"/>
	                    <jsp:useBean id="multiSelectStatus2" type="javax.servlet.jsp.jstl.core.LoopTagStatus"/>
						<c:set var="multiOptValue" value="<%= multiSelectOpt2.getValue() %>" />             
						<c:choose>
	                        <c:when test="${cw:hasCsvValue(fieldValue, multiOptValue)}">
	                            <span><img src="${pageContext.request.contextPath}/images/vanilla/icon_tick_green.png" /></span>                                            
	                        </c:when>
	                        <c:otherwise>
	                            <span><img src="${pageContext.request.contextPath}/images/vanilla/icon_cross_red.png" /></span>
	                        </c:otherwise>
	                    </c:choose>
	                    <c:out value="${ multiSelectOpt2.value }"/>
					</div>
                </c:forEach>
		    </c:otherwise>
		</c:choose>
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
		<c:choose>
		    <c:when test="${fieldEditable}">
		        <div style="line-height: 0em;">
		            <input type="text"
		                id="${inputName}"
		                name="${inputName}"
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
		            id="${fileInputName}"
		            name="${fileInputName}"
		            class="image_file"
		            onchange="bdrs.util.file.imageFileUploadChangeHandler(this);jQuery('#${inputName}').val(jQuery(this).val());"
		        />
				<a href="javascript:void(0)" class="clearLink" onclick="jQuery('#${inputName}, #${fileInputName}').attr('value',''); jQuery('#${ formPrefix }attribute_img_${ formField.attribute.id }').remove();">Clear</a>
		    </c:when>
		</c:choose>
    </c:when>
    <c:when test="${ formField.attribute.type == 'FILE' }">
    	
		<c:if test="${ formField.attributeValue != null && formField.attributeValue.stringValue != null }">
            <div id="${sectionName}">
                <a href="${pageContext.request.contextPath}/files/download.htm?<%= formField.getAttributeValue().getFileURL() %>">
                    <c:out value="${ formField.attributeValue.stringValue }"/>
                </a>
            </div>
        </c:if>
		<c:choose>
		    <c:when test="${fieldEditable}">
		        
		        <div style="line-height: 0em;">
		            <input type="text"
		                id="${inputName}"
		                name="${inputName}"
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
		            id="${fileInputName}"
		            name="${fileInputName}"
		            class="data_file"
		            onchange="jQuery('#${inputName}').val(jQuery(this).val());"
		        />
				<a href="javascript:void(0)" class="clearLink" onclick="jQuery('#${inputName}, #${fileInputName}').attr('value',''); jQuery('#${sectionName}').remove();" >Clear</a>
		    </c:when>
		    <c:otherwise>
		        <%-- do nothing --%>
		    </c:otherwise>
		</c:choose>
    </c:when>
</c:choose>
