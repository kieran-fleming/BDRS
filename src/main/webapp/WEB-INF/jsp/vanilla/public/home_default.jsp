<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>
<%@page import="au.com.gaiaresources.bdrs.model.metadata.Metadata"%>
<div class="pageContent">
    <h1 class="textcenter">Welcome to the Biological Data Recording System</h1>
    <div class="left_col">
        <div style="height: 37.5em;"></div>
        <h3>Supported By</h3>
        <a href="http://www.ala.org.au/">
            <img src="${pageContext.request.contextPath}/images/bdrs/atlasoflivingaust.png" alt="Atlas of Living Australia"/>
        </a>
    </div>
    <div class="center_col">
        <cw:getContent key="public/home" />
    </div>
    <div class="right_col">
        <div style="height: 33.5em;">
            <h3 class="nopad_top">
                <a href="http://www.birdsaustralia.com.au/conservation/our-projects.html">
                    Our Projects
                </a>
            </h3>
            <p>
                Click on an image of a project you are interested in to sign in and 
                start recording observations today! Or register with the site
                <a href="${pageContext.request.contextPath}/vanilla/usersignup.htm">here</a>.
            </p>
            <c:if test="${not empty publicSurveys }">
                <div id="survey_carousel">
                    <c:forEach items="${ publicSurveys }" var="survey">
                        <jsp:useBean id="survey" type="au.com.gaiaresources.bdrs.model.survey.Survey"/>
                        <c:set var="logo_md" value="<%= survey.getMetadataByKey(Metadata.SURVEY_LOGO) %>" scope="page"/>
                           <c:if test="${ logo_md != null }">
                               <jsp:useBean id="logo_md" type="au.com.gaiaresources.bdrs.model.metadata.Metadata" scope="page"/>
                               <img src="${pageContext.request.contextPath}/files/download.htm?<%= logo_md.getFileURL() %>" 
                                   alt="<b>${survey.name}</b><br/>${ survey.description }" title="${ survey.name }" 
                                   width="250" onclick="window.document.location='${pageContext.request.contextPath}/bdrs/user/surveyRenderRedirect.htm?surveyId=${survey.id}'"/>   
                           </c:if>
                    </c:forEach>
                </div>
            </c:if>
        </div>
        <div id="statistics">
            <h3>Latest Statistics</h3>
            <table>
                <tbody>
                   <tr>
                       <th>Number of users</th>
                       <td class="boldtext">${userCount}</td>
                   </tr>
                    <tr>
                        <th>Total number of records</th>
                        <td class="boldtext">${recordCount}</td>
                    </tr>
                    <tr>
                        <th>Number of species recorded</th>
                        <td class="boldtext">${uniqueSpeciesCount}</td>
                    </tr>
                </tbody>
            </table>
            <p>
                The last sighting was a ${latestRecord.species.commonName},  
                <span class="scientificName">${latestRecord.species.scientificName}</span>
                in the group ${latestRecord.species.taxonGroup.name}.
            </p>
        </div>
    </div>
</div>
<div style="clear: both;"></div>

<script type="text/javascript">
    jQuery(function() {
        jQuery("#survey_carousel").cjSimpleSlideShow({
            autoRun: true,
            centerImg: true,
            showCaptions: true
        });
        
        jQuery.get("${pageContext.request.contextPath}/speciesCount.htm", function(data) {
            jQuery(".speciesCount").text(data);
        });
    });
</script>
