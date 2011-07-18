<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<jsp:useBean id="context" scope="request" type="au.com.gaiaresources.bdrs.servlet.RequestContext"></jsp:useBean>

<div id="horiz-menu" class="suckerfish">
    <ul class="menutop" id="nav">
        <sec:authorize ifAnyGranted="ROLE_ADMIN,ROLE_USER">
            <li><a href="${pageContext.request.contextPath}/authenticated/redirect.htm">Home</a></li>
            <li><a href="#">Contribute</a>
                <ul id="contributeMenu">
                    <li><a href="${pageContext.request.contextPath}/bulkdata/bulkdata.htm">Bulk Data</a></li>
                </ul>
            </li>
            <li><a href="#">Review</a>
                <ul id="reviewMenu">
                    <!-- species=Calyptorhynchus+latirostris -->
                    <!-- species=Brown -->
                    <!--<li><a href="${pageContext.request.contextPath}/map/recordTracker.htm?species=Calyptorhynchus+latirostris&user=${context.user.id}&group=0&survey=1&taxon_group=0&limit=300&layer_name=My+Sightings&page=usersightings">My Sightings</a></li>-->
                    <li><a href="${pageContext.request.contextPath}/map/mySightings.htm">My Sightings</a></li>
                    <li><a href="${pageContext.request.contextPath}/review/sightings/advancedReview.htm">Advanced Review</a></li>
                    
                    <!-- Leaving this as an example of how to use the recordTracker 
                    <li><a href="${pageContext.request.contextPath}/map/recordTracker.htm?species=Calyptorhynchus+latirostris&user=0&group=0&survey=1&taxon_group=0&limit=300&layer_name=Last+300+Sightings&page=recentsightings">Last 300</a></li>
                    <li><a href="${pageContext.request.contextPath}/map/recordTracker.htm?species=Calyptorhynchus+latirostris&user=0&group=0&survey=1&taxon_group=0&limit=5000&layer_name=By+Date&show_date=true&page=datesightings">By Date</a></li>
                     -->
                    <!-- Example of how to add a sub menu -->
                    <!--li><a href="#" class="daddy">Download</a>
                    	<ul>
                        	<li><a href="#">Climbing perches</a></li>
                        	<li><a href="#">Labyrinthfishes</a></li>
                        	<li><a href="#">Kissing gouramis</a></li>
                        	<li><a href="#">Pike-heads</a></li>
                        	<li><a href="#">Giant gouramis</a></li>
                    	</ul>
                	</li-->
                </ul>
            </li>
            <sec:authorize ifAnyGranted="ROLE_ADMIN,ROLE_ROOT">
                <li><a href="#">Admin</a>
                    <ul>
                        <li><a href="${pageContext.request.contextPath}/bdrs/admin/survey/listing.htm">Project</a></li>
                        <sec:authorize ifAnyGranted="ROLE_ROOT">
                            <li><a href="${pageContext.request.contextPath}/bdrs/root/portal/listing.htm">Portal</a></li>
                            <li><a href="${pageContext.request.contextPath}/bdrs/admin/testdata/dashboard.htm">Create Test Data</a></li>
                        </sec:authorize>
                        <li><a href="${pageContext.request.contextPath}/bdrs/admin/preference/preference.htm">Preferences</a></li>
                        <li><a href="${pageContext.request.contextPath}/bdrs/admin/threshold/listing.htm">Threshold</a></li>
                        <li><a href="${pageContext.request.contextPath}/bdrs/admin/taxonomy/listing.htm">Taxonomy</a></li>
                        <li><a href="${pageContext.request.contextPath}/bdrs/admin/taxongroup/listing.htm">Taxon Groups</a></li>
                        <li><a href="${pageContext.request.contextPath}/bdrs/admin/censusMethod/listing.htm">Census Methods</a></li>
                        <li><a href="${pageContext.request.contextPath}/bdrs/admin/map/listing.htm">Maps</a></li>
                        <li><a href="${pageContext.request.contextPath}/bdrs/admin/mapLayer/listing.htm">Map Layers</a></li>
                        <li><a href="${pageContext.request.contextPath}/admin/userSearch.htm">Manage Users</a></li>
                        <li><a href="${pageContext.request.contextPath}/bdrs/admin/group/listing.htm">Manage User Groups</a></li>
                        <li><a href="${pageContext.request.contextPath}/admin/editContent.htm">Edit Content</a></li>
                        <li><a href="${pageContext.request.contextPath}/bdrs/user/managedfile/listing.htm">Media Upload</a></li>
						<li><a href="${pageContext.request.contextPath}/bdrs/admin/gallery/listing.htm">Galleries</a></li>
                    	<li><a href="${pageContext.request.contextPath}/bdrs/public/embedded/widgetBuilder.htm">Embedded Widgets</a></li>
                    </ul>
                </li>
            </sec:authorize>
            <li><a href="#">Profile</a>
                <ul>
                	<li><a href="${pageContext.request.contextPath}/user/profile.htm">My Profile</a></li>
                    <li><a href="${pageContext.request.contextPath}/bdrs/location/editUserLocations.htm">Locations</a></li>
                </ul>
            </li>
            <!--<li><a href="${pageContext.request.contextPath}/logout">Sign Out</a></li>-->
        </sec:authorize>

        <!--  i.e. If not signed in.... -->
        <sec:authorize ifNotGranted="ROLE_ADMIN,ROLE_USER">
            <li><a href="${pageContext.request.contextPath}/home.htm">Home</a></li>
            <li><a href="${pageContext.request.contextPath}/fieldguide/groups.htm">Field Guide</a></li>
            <!--  note there is no 'my sightings' map page when you are not signed in -->
            <li><a href="${pageContext.request.contextPath}/home.htm?signin=true">Sign In</a></li>
            <li><a href="#">Review</a>
                <ul id="reviewMenu">
                </ul>
            </li>
        </sec:authorize>

        <li><a href="${pageContext.request.contextPath}/about.htm">About</a></li>
        <li><a href="${pageContext.request.contextPath}/help.htm">Help</a></li>
        <li><a href="${pageContext.request.contextPath}/mobileSession.htm">Mobile</a></li>
    </ul>
</div>


<script type="text/javascript">

    jQuery(function() {
        bdrs.menu.initHover();
	
	    <sec:authorize ifAnyGranted="ROLE_ADMIN,ROLE_USER">
	    
	    bdrs.menu.populateSurveyItems('${context.user.registrationKey}', "#contributeMenu");
	    
	    </sec:authorize>

	    bdrs.menu.populateMapMenu("#reviewMenu");
    });
</script>
