<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<tiles:useAttribute name="path"/>
<tiles:useAttribute name="type"/>
<tiles:useAttribute name="validValues" ignore="true"/>
<tiles:useAttribute name="validValuesLabelProperty" ignore="true"/>
<tiles:useAttribute name="validValuesValueProperty" ignore="true"/>
<tiles:useAttribute name="required" ignore="true"/>
<tiles:useAttribute name="onchange" ignore="true"/>

    <c:choose> 
        <c:when test="${type == 'checkbox'}">
            <form:checkbox path="${path}" cssErrorClass="formerrorfield"/>
        </c:when>
        <c:when test="${type == 'select'}">
            <form:select path="${path}" cssClass="formFieldSelect">
            <c:if test="${!required}"><form:option value="" label=""/></c:if>
            <form:options items="${validValues}"/>
            </form:select>
        </c:when>
        <c:when test="${type == 'hidden' }">
            <form:hidden path="${path}"/>
        </c:when>
        <c:when test="${type == 'text'}">
            <form:input path="${path}" size="40" cssErrorClass="formerrorfield" onchange="${onchange}"/>
        </c:when>
        <c:when test="${type == 'textarea'}">
            <form:textarea path="${path}" cols="40" rows="5" cssErrorClass="formerrorfield"/>
        </c:when>
        <c:when test="${type == 'radios'}">
            <c:choose>
                <c:when test="${fn:length(validValuesLabelProperty) > 0 && fn:length(validValuesValueProperty) > 0}">
                    <form:radiobuttons path="${path}" items="${validValues}" 
                                       itemLabel="${validValuesLabelProperty}" itemValue="${validValuesValueProperty}" delimiter="<br/>"/>
                </c:when>
                <c:otherwise>
                    <form:radiobuttons path="${path}" items="${validValues}" delimiter="<br/>"/>
                </c:otherwise>
            </c:choose>
        </c:when>
        <c:when test="${type == 'checkboxes'}">
            <form:checkboxes items="${validValues}" path="${path}" delimiter="<br/>"></form:checkboxes>
        </c:when>
        <c:when test="${type == 'date'}">
            <form:input path="${path}" size="40"/>
            <img src="${pageContext.request.contextPath}/js/rico/images/calendaricon.gif" title="Calendar" id="${path}_cal"/>
        </c:when>
        <c:when test="${type == 'file'}">
            <input type="file" id="${path}" name="${path}" onchange="${onchange}"/>
        </c:when>
        <c:when test="${type == 'image'}">
            <input type="file" id="${path}" name="${path}" accept="image/gif,image/jpeg,image/png"  onchange="${onchange}"/>
        </c:when>
        <c:when test="${type == 'password'}">
            <form:password path="${path}" size="40" cssErrorClass="formerrorfield"  onchange="${onchange}"/>
        </c:when>
        <c:when test="${type == 'hour-dropdown'}">
            <form:select path="${path}">
                <form:option value=""></form:option>
                <form:option value="0">00</form:option>
                <form:option value="1">01</form:option>
                <form:option value="2">02</form:option>
                <form:option value="3">03</form:option>
                <form:option value="4">04</form:option>
                <form:option value="5">05</form:option>
                <form:option value="6">06</form:option>
                <form:option value="7">07</form:option>
                <form:option value="8">08</form:option>
                <form:option value="9">09</form:option>
                <form:option value="10">10</form:option>
                <form:option value="11">11</form:option>
                <form:option value="12">12</form:option>
                <form:option value="13">13</form:option>
                <form:option value="14">14</form:option>
                <form:option value="15">15</form:option>
                <form:option value="16">16</form:option>
                <form:option value="17">17</form:option>
                <form:option value="18">18</form:option>
                <form:option value="19">19</form:option>
                <form:option value="20">20</form:option>
                <form:option value="21">21</form:option>
                <form:option value="22">22</form:option>
                <form:option value="23">23</form:option>
            </form:select>
        </c:when>
        <c:when test="${type == 'minute-dropdown'}">
            <form:select path="${path}">
                <form:option value=""></form:option>
                <form:option value="0">00</form:option>
                <form:option value="5">05</form:option>
                <form:option value="10">10</form:option>
                <form:option value="15">15</form:option>
                <form:option value="20">20</form:option>
                <form:option value="25">25</form:option>
                <form:option value="30">30</form:option>
                <form:option value="35">35</form:option>
                <form:option value="40">40</form:option>
                <form:option value="45">45</form:option>
                <form:option value="50">50</form:option>
                <form:option value="55">55</form:option>
            </form:select>
        </c:when>
    </c:choose>