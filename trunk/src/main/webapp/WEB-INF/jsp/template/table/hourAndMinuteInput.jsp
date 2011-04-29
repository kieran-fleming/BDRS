<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<tiles:useAttribute name="hoursProperty"/>
<tiles:useAttribute name="minutesProperty"/>

<tiles:insertDefinition name="formInput">
    <tiles:putAttribute name="path" value="${hoursProperty}"/>
    <tiles:putAttribute name="type" value="hour-dropdown"/>
</tiles:insertDefinition>

<span>&nbsp;</span>

<tiles:insertDefinition name="formInput">
    <tiles:putAttribute name="path" value="${minutesProperty}"/>
    <tiles:putAttribute name="type" value="minute-dropdown"/>
</tiles:insertDefinition>