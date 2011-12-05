<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec"%>

<h1>Reports</h1>

<cw:getContent key="user/report/listing"/>

<sec:authorize ifAnyGranted="ROLE_ADMIN,ROLE_ROOT">
	<div class="buttonpanel sepBottom">
	    <form method="POST" action="${pageContext.request.contextPath}/report/add.htm" enctype="multipart/form-data">
	        <input id="add_report_file" name="report_file" type="file" style="visibility:hidden"/>
		    <input id="add_report_button" class="form_action right" type="button" value="Add Report"/>
	    </form>
	    <div class="clear"></div>
	</div>
</sec:authorize>

<c:forEach items="${ reports }" var="report">
    <div class="report_item sepBottomDotted">
        <a href="${pageContext.request.contextPath}/report/${ report.id }/render.htm">
            <img class="left" src="${pageContext.request.contextPath}/files/download.htm?className=au.com.gaiaresources.bdrs.model.report.Report&id=${ report.id }&fileName=${ report.iconFilename }" alt="Icon of ${ report.name }"/>
        </a>
        <div class="left report_descriptor">
            <h2 class="left clear report_name">
                <a href="${pageContext.request.contextPath}/report/${ report.id }/render.htm">
                    <c:out value="${ report.name }"></c:out>
                </a>
            </h2>
            <p class="left clear report_description"><c:out value="${ report.description }"></c:out></p>
        </div>
        <sec:authorize ifAnyGranted="ROLE_ADMIN,ROLE_ROOT">
	        <div class="right">
	            <form method="POST" action="${pageContext.request.contextPath}/report/delete.htm">
	                <input type="hidden" name="reportId" value="${ report.id }"/>
	                <a href="javascript:void(0);" class="delete delete_report">Delete</a>
	            </form>
	        </div>
        </sec:authorize>
        <div class="clear"></div>
    </div>
</c:forEach>




<script type="text/javascript">
    jQuery(window).load(function() {
        bdrs.report.listing.init();
    });
</script>