<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ page import="java.util.Date" %>

<h1>My Sightings</h1>

<div>
    <a id="search_criteria_expand_collapse" class="right criteria_link" href="javascript:void(0);" 
        title="Expand or collapse the search criteria">Collapse</a>
    <input id="permalink_input" class="right" type="text" style="display:none;"/> 
    <a id="permalink_link" class="right criteria_link" href="javascript:void(0);" 
        title="Direct link to this page (including criteria)">Permalink</a>
    <h3>Search Criteria</h3>
</div>
<form id="search_criteria_form">
    <div id="search_critieria_container" class="input_container clear align_radio align_checkbox">
        <input id="highlighted_record_id" type="hidden" name="record_id" value="${ record_id }"/>
        <input id="page_number" type="hidden" name="page_number" value="${ page_number }"/>
	    <table id="sightings_criteria" class="form_table">
	        <tbody>
	            <%-- Section One --%>
	            <tr>
	                <th class="interrogative" colspan="2">Which</th>
	                <th class="interrogative" colspan="2">How Many</th>
	            </tr>
	            <tr>
	                <%-- Which --%>
	                <th>
	                    <label for="survey_id">Survey</label>
	                </th>
	                <td>
	                    <select id="survey_id" name="survey_id">
	                       <option value="0">All Surveys</option>
			                <c:forEach var="survey" items="${ survey_list }">
			                    <option value="${ survey.id }"
			                        <c:if test="${ survey == selected_survey }">
			                            selected="selected"
			                        </c:if> 
			                    >
			                        <c:out value="${ survey.name }"/>
			                    </option>
			                </c:forEach>
			            </select>
	                </td>
	                
	                <%-- How many --%>
	                <th>
	                    <label for="limit">Limit</label>
	                </th>
	                <td>
	                    <input id="limit" class="validate(positiveInteger)" type="text" name="limit" value="${ limit }" placeholder="e.g. 300"/>
	                    <input id="show_all" type="checkbox" name="limit" value="0"/>
	                    <label for="show_all">Show all</label> 
	                    <em id="limit_warning" style="display:none;">Warning: Large datasets may take a long time</em>
	                </td>
	            </tr>
	            
	            <%-- Section Two --%>
	            <tr>
	                <th class="interrogative" colspan="2">What</th>
	                <th class="interrogative" colspan="2">When</th>
	            </tr>
	            <tr>
	                <%-- What Row 1 --%>
	                <th>
	                    <label for="taxon_group_id">Species Group</label>
	                </th>
	                <td>
	                    <select id="taxon_group_id" name="taxon_group_id">
                            <option value="0">All Groups</option>
	                        <c:forEach var="group" items="${ group_list }">
	                            <option value="${ group.id }"
	                               <c:if test="${ group.id == taxon_group_id }">
	                                   selected="selected"
	                               </c:if>
	                            >
	                                <c:out value="${ group.name }"/>
	                            </option>
	                        </c:forEach>
	                    </select>
	                </td>
	                <%-- When Row 1 --%>
	                <th>
	                    <label for="start_date">Start</label>
	                </th>
	                <td>   
	                    <input id="start_date" type="text" name="start_date" class="validate(date) datepicker_historical"
	                        placeholder="e.g. <fmt:formatDate pattern="dd MMM yyyy" value="${ selected_survey.startDate }"/>"
	                        <%-- start_date may be null but the formatter defaults to blank --%>
	                        value="<fmt:formatDate pattern="dd MMM yyyy" value="${ start_date }"/>"
	                    />
	                </td>
	            </tr>
	            <tr>
	                <%-- What Row 2 --%>
	                <th>
	                    <label for="taxon_id">Species Search</label>
	                </th>
	                <td>
	                    <input id="taxon_id" type="text" placeholder="e.g. Kangaroo" name="taxon_search"
	                        title="Enter the common or scientific name to search only for that species, otherwise leave blank for all species"
	                        value="<c:out value="${ taxon_search }"/>"/>
	                </td>
	                
	                <%-- When Row 2 --%>
	                <th>
	                    <label for="end_date">End</label>
	                </th>
	                <td>
	                    <input id="end_date" type="text" class="validate(date) datepicker_historical" name="end_date" 
	                        placeholder="e.g. <fmt:formatDate pattern="dd MMM yyyy" value="<%= new Date() %>"/>"
	                        <%-- end_date may be null but the formatter defaults to blank --%>
	                        value="<fmt:formatDate pattern="dd MMM yyyy" value="${ end_date }"/>"
	                    />
	                </td>
	            </tr>
	            
	            <tr>
	                <th class="interrogative" colspan="2">Who</th>
	                <th class="interrogative" colspan="2">Order</th>
	            </tr>
	            <tr>
	                <th>
	                    <label for="my_records">Owner</label>
	                </th>
	                <td>
	                    <fieldset>
	                        <div>
	                            <input id="my_records" type="radio" name="user_records_only" checked="checked" value="true"/>
	                            <label for="my_records">Only for me</label>
	                        </div>
	                        
	                        <sec:authorize ifAnyGranted="ROLE_ADMIN, ROLE_ROOT">
	                            <div>
	                                <input type="radio" id="all_records" name="user_records_only" value="false"
	                                    <c:if test="${ not user_records_only }">
	                                       checked="checked"
	                                    </c:if>
	                                />
	                                <label for="all_records">All public records</label>
	                            </div>
	                        </sec:authorize>
	                    </fieldset>
	                </td>
	                <th>
                        <label for="my_records">Attribute</label>
                    </th>
                    <td>
                        <div>
	                        <select id="sort_by" name="sort_by">
				                <option value="record.when"
				                    <c:if test="${ 'record.when' == sort_by }">
				                        selected="selected"
				                    </c:if>
				                >
				                    date
				                </option>
				                <option value="species.scientificName"
				                    <c:if test="${ 'species.scientificName' == sort_by }">
				                        selected="selected"
				                    </c:if>
				                >
				                    scientific name
				                </option>
				                <option value="species.commonName"
				                    <c:if test="${ 'species.commonName' == sort_by }">
				                        selected="selected"
				                    </c:if>
				                >
				                    common name
				                </option>
				                <option value="location.name"
				                    <c:if test="${ 'location.name' == sort_by }">
				                        selected="selected"
				                    </c:if>
				                >
				                    location
				                </option>
				                <option value="censusMethod.type"
				                    <c:if test="${ 'censusMethod.type' == sort_by }">
				                        selected="selected"
				                    </c:if>
				                >
				                    type
				                </option>
				                <option value="record.user"
				                    <c:if test="${ 'record.user' == sort_by }">
				                        selected="selected"
				                    </c:if>
				                >
				                    user
				                </option>
				            </select>
                        </div>
                        <div class="order_type">
                            <fieldset>
                                <input id="ascending" type="radio" name="sort_order" value="ASCENDING"
                                    <c:if test="${ 'ASCENDING' == sort_order }">
                                        checked="checked"
                                    </c:if>
                                />
                                <label for="ascending">Ascending</label>
                                <input id="descending" type="radio" name="sort_order" value="DESCENDING"
                                    <c:if test="${ 'DESCENDING' == sort_order }">
                                        checked="checked"
                                    </c:if>
                                />
                                <label for="descending">Descending</label>  
                            </fieldset>
                        </div>
                    </td>
	            </tr>
	        </tbody>
	    </table>
	    <input id="selected_tab" type="hidden" name="selected_tab" value="${ selected_tab }"/>
	    <div class="buttonpanel textright">
	       <input id="search_criteria" class="form_action" type="button" value="Search"/>
	    </div>
    </div>

	<div id="sightings_tabs" class="controlPanel">
	    <a id="map_tab_handle" class="tab_handle" href="javascript:void(0);">
	        <div 
	            <c:choose>
		            <c:when test="${ \"map\" == selected_tab }">
		                class="displayTab left displayTabSelected"
		            </c:when>
		            <c:otherwise>
		                class="displayTab left"
		            </c:otherwise>
		        </c:choose>
	        >
	            Map
	        </div>
	    </a>
	    <a id="table_tab_handle" class="tab_handle" href="javascript:void(0);">
	        <div 
	            <c:choose>
	                <c:when test="${ \"table\" == selected_tab }">
	                    class="displayTab left displayTabSelected"
	                </c:when>
	                <c:otherwise>
	                    class="displayTab left"
	                </c:otherwise>
	            </c:choose>
	        >
	            Table
	        </div>
	    </a>
	    <a id="download_tab_handle" class="tab_handle" href="javascript:void(0);">
	        <div
	            <c:choose>
	                <c:when test="${ \"download\" == selected_tab }">
	                    class="displayTab left displayTabSelected"
	                </c:when>
	                <c:otherwise>
	                    class="displayTab left"
	                </c:otherwise>
	            </c:choose>
	        >
	            Download
	        </div>
	    </a>
	    <span id="loading" class="right" style="display:none">
	        <img class="vertmiddle" src="${pageContext.request.contextPath}/images/icons/ajax-loader.gif" alt="This is a loading spinner" title="Data Loading"/>
	        <span>Loading</span>
	    </span>
	    <div class="clear"></div>
	</div>
	<div id="sightings_tab_content">
	    <%--
	        This is the Map Tab 
	    --%>
	    <div id="map_tab"
	        <c:choose>
		        <c:when test="${ \"map\" == selected_tab }">
		            style="display:block"
		        </c:when>
		        <c:otherwise>
		            style="display:none"
		        </c:otherwise>
	        </c:choose>
	    >
	        <div class="map_wrapper" id="map_wrapper">
			    <div id="record_map" class="defaultmap review_map"></div>
			    <div id="geocode" class="geocode"></div>
			    <div class="recordCount textright"></div>
			</div>
			
			<div class="textright recordCountPanel"></div>
	    </div>
	    
	    <%--
	        This is the Table Tab 
	    --%>
	    <div id="table_tab"
	        <c:choose>
	            <c:when test="${ \"table\" == selected_tab }">
	                style="display:block"
	            </c:when>
	            <c:otherwise>
	                style="display:none"
	            </c:otherwise>
	        </c:choose>
	    >
	        <table id="record_table" class="datatable">
	            <thead>
	                <tr>
	                    <th>Date</th>
	                    <th>Common&nbsp;Name</th>
	                    <th>Scientific&nbsp;Name</th>
	                    <th>Latitude</th>
	                    <th>Longitude</th>
	                    <th>Number</th>
	                    <th>Notes</th>
	                    <th colspan="2">Action</th>
	                </tr>
	            </thead>
	        </table>
	        
	        <div class="textright recordCountPanel"></div>
	        <div id="page_count" class="textcenter"></div>
	    </div>
	    
	    <%--
	        This is the Download Tab 
	    --%>
	    <div id="download_tab"
	        <c:choose>
	            <c:when test="${ \"download\" == selected_tab }">
	                style="display:block"
	            </c:when>
	            <c:otherwise>
	                style="display:none"
	            </c:otherwise>
	        </c:choose>
	    >
	        <p>
	            <h3>Click to Download</h3>
	            Please choose the desired formats and click the button below to download the current search results as a zip file.
	        </p>
	        <div class="align_radio">
		        <fieldset>
			        <div>
			            <input id="kml" type="checkbox" name="download_format" value="KML"
			                <c:if test="${ download_kml_selected }">
			                    checked="checked"
		                    </c:if>
			            />
			            <label for="kml">
			                Keyhole Markup Language (KML) &mdash; Can be used for <a href="http://earth.google.com" target="_target">Google Earth</a>
			            </label> 
			        </div>
			        <div>
			            <input id="shp" type="checkbox" name="download_format" value="SHAPEFILE" 
			                <c:if test="${ download_shp_selected }">
                                checked="checked"
                            </c:if>
                        />
			            <label for="shp">
			                Shapefile (SHP) &mdash; Can be used in <a href="http://en.wikipedia.org/wiki/Comparison_of_geographic_information_systems_software" target="_target">GIS applications</a>.
			            </label> 
			        </div>
			        <div>
			            <input id="xls" type="checkbox" name="download_format" value="XLS"
			                <c:if test="${ download_xls_selected }">
                                checked="checked"
                            </c:if>
			            />
			            <label for="xls">
			                Spreadsheet (XLS) &mdash; Can be used for <a href="http://www.libreoffice.org/features/calc/">Spreadsheet Applications</a>
			            </label> 
			        </div>
		        </fieldset>
	        </div>
	        <div class="buttonpanel textright">
	            <input id="download_button" type="button" class="form_action" value="Download"/>
	        </div>
	    </div>
	</div>
</form>

<script type="text/javascript">
    jQuery(window).load(function() {
        bdrs.review.mysightings.init(${ user.portal.id });
    });
</script>