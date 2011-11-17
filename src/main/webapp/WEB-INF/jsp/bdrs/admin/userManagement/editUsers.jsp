<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<jsp:useBean id="context" scope="request" type="au.com.gaiaresources.bdrs.servlet.RequestContext"></jsp:useBean>

<h1>Edit Users</h1>

<cw:getContent key="admin/manageUsers" />

<tiles:insertDefinition name="adminUserSearch">
	<tiles:putAttribute name="approveUsers" value="false"/>
</tiles:insertDefinition>