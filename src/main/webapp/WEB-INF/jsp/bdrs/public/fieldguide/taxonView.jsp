<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<%@page import="au.com.gaiaresources.bdrs.model.taxa.AttributeType"%>

<h1>
    <span>
        <c:out value="${ taxon.commonName }"/>
    </span>
    <span class="scientificName">
        <c:out value="${ taxon.scientificName }"/>
    </span>
</h1>

<h3 class="scientificClassificationHeader">Scientific Classification</h3>
<table class="scientificClassification">
    <tbody>
        <tiles:insertDefinition name="fieldGuideTaxonRankRow">
            <tiles:putAttribute name="taxon" value="${ taxon }"/>
        </tiles:insertDefinition>
    </tbody>
</table>

<h3 class="field_guide_header">Identification</h3>
<table>
    <tbody>
        <c:forEach items="${ taxon.attributes }" var="taxonAttr">
            <jsp:useBean id="taxonAttr" type="au.com.gaiaresources.bdrs.model.taxa.IndicatorSpeciesAttribute"/>
            <c:if test="${ taxonAttr.attribute.tag }">
                <tr>
                    <th class="textright">
                        <c:out value="${ taxonAttr.attribute.description }"/>
                    </th>
                    <td>
                        <c:choose>
                            <c:when test="<%= AttributeType.IMAGE.equals(taxonAttr.getAttribute().getType()) %>">
                                <a href="${pageContext.request.contextPath}/files/download.htm?<%= taxonAttr.getFileURL() %>">
                                    <img class="max_size_img" src="${pageContext.request.contextPath}/files/download.htm?<%= taxonAttr.getFileURL() %>" alt="<c:out value="${ taxonAttr.stringValue }"/>"/>
                                </a>
                            </c:when>
                            <c:when test="<%= AttributeType.FILE.equals(taxonAttr.getAttribute().getType()) %>">
                                <a href="${pageContext.request.contextPath}/files/download.htm?<%= taxonAttr.getFileURL() %>">
                                    <c:out value="${ taxonAttr.stringValue }"/>
                                </a>
                            </c:when>
                            <c:otherwise>
                                <c:out value="${ taxonAttr.stringValue }"/>
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </c:if>
        </c:forEach>
    </tbody>
</table>

<c:forEach items="${ taxon.infoItems }" var="profile">
    <jsp:useBean id="profile" type="au.com.gaiaresources.bdrs.model.taxa.SpeciesProfile"/>
    <c:if test="${ not empty profile.content }">
	    <div class="fieldguide_profile_item">
	        <h3 class="field_guide_header">
	            <c:out value="${ profile.description }"/>
	        </h3>
	        <c:choose>
	            <c:when test="<%= profile.isImgType() %>">
                    <a class="left" href="${pageContext.request.contextPath}/files/downloadByUUID.htm?uuid=${ profile.content }">
                        <img class="max_size_img" src="${pageContext.request.contextPath}/files/downloadByUUID.htm?uuid=${ profile.content }"/>
                    </a>
                
                    <cw:getManagedFile uuid="${ profile.content }" var="managedFile"/>
                    <div class="right textright">
                        <div>
                            Credit:&nbsp;
                            <c:out value="${ managedFile.credit }"/>
                        </div>
                        <div>
                            Permission:&nbsp;
                            <c:out value="${ managedFile.license }"/>
                        </div>
                    </div>
                    <div class="clear"></div>
	            </c:when>
	            <c:when test="<%= profile.isAudioType() %>">
	                <audio src="${pageContext.request.contextPath}/files/downloadByUUID.htm?uuid=${ profile.content }" 
	                    controls="controls" preload>
	                    <c:out value="${ profile.content }"/>
	                </audio>
	                <cw:getManagedFile uuid="${ profile.content }" var="managedFile"/>
                    <div class="right textright">
                        <div>
                            Credit:&nbsp;
                            <c:out value="${ managedFile.credit }"/>
                        </div>
                        <div>
                            Permission:&nbsp;
                            <c:out value="${ managedFile.license }"/>
                        </div>
                    </div>
                    <div class="clear"></div>
	            </c:when>
	            <c:otherwise>
	                    <p><c:out value="${ profile.content }"/></p>
	            </c:otherwise>
	        </c:choose>
	    </div>
    </c:if>
</c:forEach>

<script type="text/javascript">
    html5media.configureFlowplayer = function (tag, element, config){
        if(tag === 'audio') {
            config.clip.type = 'audio';
        }
        config.plugins.controls.all = false;
        config.plugins.controls.play = true;
        config.plugins.controls.scrubber = true;
        config.plugins.controls.volume = true;
    }
</script>