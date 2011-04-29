<%@ page language="java" contentType="text/html"%>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<div id="transpBox" class="box">
	<p class="textcenter">
	    You are about to download the application to your device. The size off
	    the application is ${dataSize}MB. Your carrier might charge you for
	    the download!
	</p>
	<p>
		<c:out value="${groupDataString}"/>
	</p>
	<div id="actionPanel" class="textcenter">
		<input class="form_action" type="button" value="Download" onclick="window.document.location = '${pageContext.request.contextPath}/mobile/home.html'"/>
	    <input class="form_action" type="button" value="Cancel" onclick="window.document.location='home.htm'"/>
	</div>
	<div id="downloadProgress" class="hidden textcenter">
	    <img src="${pageContext.request.contextPath}/images/mobile/ajax-loader.gif" alt="downloading"/>
	    <div>
	        <p>
	            Please be patient.<br>
	            You will be automatically forwarded when downloading has finished.
	        </p>
	    </div>
	</div>
</div>
