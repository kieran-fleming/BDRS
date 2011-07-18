<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<jsp:useBean id="gallery" type="au.com.gaiaresources.bdrs.model.showcase.Gallery" scope="request" />
<div id="gallery">
    <jsp:useBean id="mfMap" type="java.util.Map<String, au.com.gaiaresources.bdrs.model.file.ManagedFile>" scope="request"/>
    <c:forEach var="uuid" items="${gallery.fileUUIDS}">
    	<jsp:useBean id="uuid" type="java.lang.String" />
		<a rel="lightbox" href="${pageContext.request.contextPath}/bdrs/public/gallery/fullImg.htm?uuid=${uuid}">
        <img src="${pageContext.request.contextPath}/bdrs/public/gallery/slideshowImg.htm?uuid=${uuid}" width="${slideshowWidth}" height="${slideshowHeight}" 
		alt="
		<p>
		  <c:out value="<%= mfMap.get(uuid).getDescription() %>" />
		</p>
		<p>
		  <c:if test="<%= org.springframework.util.StringUtils.hasLength(mfMap.get(uuid).getCredit()) %>">
		  	Credit:&nbsp;
            <c:out value="<%= mfMap.get(uuid).getCredit() %>" />
		  </c:if>
		</p>
		<p>
		  <c:if test="<%= org.springframework.util.StringUtils.hasLength(mfMap.get(uuid).getLicense()) %>">
		  	License:&nbsp;
			<c:out value="<%= mfMap.get(uuid).getLicense() %>" />
		  </c:if>
		</p>
		" />  <!-- end of the image tag -->
		</a>
    </c:forEach>
</div>

<script>
	$(function() {
		$("#gallery").css("height", "${height}");
		$("#gallery").cjSimpleSlideShow({
            autoRun: true,
            allowPause: true,
            showCaptions: true,
            centerImg: true
        });
		
		var elementsToStyle = $("#gallery .cj_slideshow_wrapper .cj_slideshow_slide .cj_slideshow_caption");
		elementsToStyle.css("background-color", "${backgroundColor}");
		elementsToStyle.css("font-weight", "bold");
		elementsToStyle.css("color", "${textColor}");
		elementsToStyle.css("position", "static");
    });   
</script>