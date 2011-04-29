<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<tiles:useAttribute id="title" name="title" classname="java.lang.String"/>
<tiles:useAttribute id="bean" name="bean" classname="java.lang.Object"/>
<tiles:useAttribute id="titles" name="titles" classname="java.util.List"/>
<tiles:useAttribute id="properties" name="properties" classname="java.util.List"/>

<table class="datatable" cellspacing="0" cellpadding="1">
    <tr>
        <th>&nbsp;&nbsp;&nbsp;</th>
        <th colspan="2"><c:out value="${title}"/></th>
        <th>&nbsp;&nbsp;&nbsp;</th>
    </tr>
    
    <c:forEach var="prompt" items="${titles}" varStatus="status">
        <tr class="datatablerow${status.count % 2}">
            <td/>
            <td><c:out value="${prompt}"/></td>
            <cw:displayTableElement bean="${bean}" render="${properties[status.count - 1]}"/>
            <td/>
        </tr>
    </c:forEach>
</table>
