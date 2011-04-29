<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<tiles:useAttribute name="label"/>
<tiles:useAttribute name="path"/>
<tiles:useAttribute name="type"/>
<tiles:useAttribute name="validValues" ignore="true"/>
<tiles:useAttribute name="validValuesLabelProperty" ignore="true"/>
<tiles:useAttribute name="validValuesValueProperty" ignore="true"/>
<tiles:useAttribute name="required" ignore="true"/>
<tiles:useAttribute name="onchange" ignore="true"/>
<div class="recordLabel">
<label for="${path}" class="recItemLabel"><c:out value="${label}"/></label></div>
<div class="recordInput">
<tiles:insertDefinition name="formInput">
    <tiles:putAttribute name="path" value="${path}"/>
    <tiles:putAttribute name="type" value="${type}"/>
    <tiles:putAttribute name="validValues" value="${validValues}"/>
    <tiles:putAttribute name="validValuesLabelProperty" value="${validValuesLabelProperty}"/>
    <tiles:putAttribute name="validValuesValueProperty" value="${validValuesValueProperty}"/>
    <tiles:putAttribute name="required" value="${required}"/>
    <tiles:putAttribute name="onchange" value="${onchange }"/>
</tiles:insertDefinition>
</div>
<form:errors path="${path}"/>
