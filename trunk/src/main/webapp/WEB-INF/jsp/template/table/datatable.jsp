<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<tiles:useAttribute id="beans" name="beans" classname="java.util.Collection"/>
<tiles:useAttribute id="titles" name="titles" classname="java.util.List"/>
<tiles:useAttribute id="properties" name="properties" classname="java.util.List"/>

<table class="datatable" cellspacing="0" cellpadding="1">
    <tr>
        <th>&nbsp;&nbsp;&nbsp;</th>
        <c:forEach items="${titles}" var="title">
            <th><c:out value="${title}"/></th>
        </c:forEach>
        <th>&nbsp;&nbsp;&nbsp;</th>
    </tr>
    <c:forEach items="${beans}" var="bean" varStatus="status">
        <tr class="datatablerow${status.count % 2}">
            <td/>
            <c:forEach items="${properties}" var="property" varStatus="count">
                <cw:displayTableElement bean="${bean}" render="${property}"/>
            </c:forEach>
            <td/>
        </tr>
    </c:forEach>
</table>