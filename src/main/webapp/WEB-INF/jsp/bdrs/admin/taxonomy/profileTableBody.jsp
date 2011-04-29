<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<tiles:useAttribute name="taxonProfileList" classname="java.util.List" ignore="true"/>
<tiles:useAttribute name="newProfileIndex" classname="java.lang.Integer" ignore="true"/>

<c:forEach var="profile" items="${ taxonProfileList }" varStatus="profileRowCounter">

    <jsp:useBean id="profile" type="au.com.gaiaresources.bdrs.model.taxa.SpeciesProfile"/>
    
    <tiles:insertDefinition name="taxonProfileRow">
        <tiles:putAttribute name="profile" value="${ profile }"/>
        <tiles:putAttribute name="index" value="${ profileRowCounter.count + newProfileIndex }"/>
    </tiles:insertDefinition>
    
</c:forEach>