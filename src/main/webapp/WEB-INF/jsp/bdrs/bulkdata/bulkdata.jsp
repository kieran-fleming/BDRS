<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<h1>Bulk Data</h1>
<p>
    While you can upload datum one by one through the individual recording forms, 
	you can also contribute a bulk amount of data through this Bulk Upload page.  
	There are some restrictions to this:
</p>

<ul>
	<li>You have to download a template in either ESRI Shapefile or Microsoft Excel format</li>
	<li>You then fill out the details in that template, and</li>
	<li>You then upload the data through the Upload method at the bottom of the form.</li>
</ul>

<p>
	Please note: all co-ordinates must be in the Geographic (WGS84) Latitude &ndash; Longitude projection, 
	and if you are using Excel you must have decimal degrees.  If you change the format of the 
	template before loading it, it will not pass the upload method.
</p>

<h3>Step 1: Select File Format</h3>
<div>
	<table class="bulkDataTableForm">
        <tbody>
            <tr>
                <th>
                    Spreadsheet
                </th>
                <td>
                    <input id="bulkDataUploadType" type="radio" name="bulkDataUploadType" value="spreadsheet" checked="checked" />
                </td>
            </tr>
            <tr>
                <th>Shapefile</th>
                <td>
                    <input type="radio" name="bulkDataUploadType" value="shapefile" />
                </td>
            </tr>
        </tbody>
    </table>
</div>

<div id="spreadsheetSection">
	<p>
	    <ul>
            <li>Download the template,</li>
            <li>fill it out, and</li>
            <li>then upload it.</li>
        </ul>
	</p>
	
	<h3>Step 2: Download Template Spreadsheet</h3>
	<div class="textright">
	    <a id="downloadTemplateToggle" href="javascript: void(0);">Collapse</a>
	</div>
	<div id="downloadTemplateWrapper">
	    <p>
	        Download your template below for your project. There is help, a list of
	        locations and a taxonomy checklist included in the downloaded template.
	    </p>
	    <form id="downloadTemplateForm" action="${pageContext.request.contextPath}/bulkdata/spreadsheetTemplate.htm" method="get">
	        <table class="form_table bulkDataTableForm">
	            <tbody>
	                <tr>
	                    <th>
	                        <label class="strong" for="surveyTemplateSelect">Project:</label>
	                    </th>
	                    <td>
	                        <select id="surveyTemplateSelect" name="surveyPk">
	                            <c:forEach items="${surveyList}" var="survey">
	                                <option value="${survey.id}"><c:out value="${survey.name}"/></option>
	                            </c:forEach>
	                        </select>
	                    </td>
	                </tr>
	                <tr>
	                    <th>Download Checklist:</th>
	                    <td>
	                        <a href="javascript: void(0)" onclick="window.document.location='${pageContext.request.contextPath}/webservice/survey/checklist.htm?format=csv&surveyId='+jQuery('#surveyTemplateSelect').val();">
	                            CSV
	                        </a>
	                        &nbsp;|&nbsp;
	                        <a href="javascript: void(0)" onclick="window.document.location='${pageContext.request.contextPath}/webservice/survey/checklist.htm?format=zip&surveyId='+jQuery('#surveyTemplateSelect').val();">
	                            Zip
	                        </a>
	                    </td>
	                </tr>
	            </tbody>
	        </table>
	        <div class="textright">
	            <input type="submit" value="Download Template" class="form_action"/>
	        </div>
	    </form>
	</div>
	
	<h3>Step 3: Upload Spreadsheet</h3>
	<div class="textright">
	    <a id="uploadSpreadsheetToggle" href="javascript: void(0);">Collapse</a>
	</div>
	<div id="uploadSpreadsheetWrapper">
	    <tiles:insertDefinition name="importForm">
	        <tiles:putAttribute name="showHeader" value="false"/>
	        <tiles:putAttribute name="header" value=""/>
	        <tiles:putAttribute name="showHelp" value="true"/>
	        <tiles:putAttribute name="extraContent" value=""/>
	    </tiles:insertDefinition>
	</div>
</div>

<div id="shapefileSection">
    <p>
        <ul>
            <li>Download the template and unzip it. It is also recomended you download the species checklist.</li>
			<li>Load the .SHP in your preferred GIS program.</li>
			<li>Create your records.</li>
			<li>Rezip everything.</li>
			<li>Upload the zip file.</li>
        </ul>
    </p>
	
	<h3>Step 2: Download Template Shapefile</h3>

	<form id="downloadShapefileTemplateForm" action="${pageContext.request.contextPath}/bulkdata/shapefileTemplate.htm" method="get">
            <table class="form_table bulkDataTableForm">
                <tbody>
                	<tr>
                        <th>
                            <label class="strong" for="shapefileType">Feature Type:</label>
                        </th>
                        <td>
                            <select id="shapefileType" name="shapefileType">
                                <option value="POINT">Point</option>
								<option value="MULTI_POLYGON">Multi-Polygon</option>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <th>
                            <label class="strong" for="surveyShapefileTemplateSelect">Project:</label>
                        </th>
                        <td>
                            <select id="surveyShapefileTemplateSelect" name="surveyPk">
                                <c:forEach items="${surveyList}" var="survey">
                                    <option value="${survey.id}"><c:out value="${survey.name}"/></option>
                                </c:forEach>
                            </select>
                        </td>
                    </tr>
					<tr>
                        <th>
                            <label class="strong" for="censusMethodShapefileTemplateSelect">Census Method:</label>
                        </th>
                        <td>
                            <select id="censusMethodShapefileTemplateSelect" name="censusMethodPk">
                                <option value="0">No census method</option>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <th>Download Checklist:</th>
                        <td>
                            <a href="javascript: void(0)" onclick="window.document.location='${pageContext.request.contextPath}/webservice/survey/checklist.htm?format=csv&surveyId='+jQuery('#surveyShapefileTemplateSelect').val();">
                                CSV
                            </a>
                            &nbsp;|&nbsp;
                            <a href="javascript: void(0)" onclick="window.document.location='${pageContext.request.contextPath}/webservice/survey/checklist.htm?format=zip&surveyId='+jQuery('#surveyShapefileTemplateSelect').val();">
                                Zip
                            </a>
                        </td>
                    </tr>
                </tbody>
            </table>
            <div class="textright">
                <input type="submit" value="Download Template" class="form_action"/>
            </div>
        </form>
	
	<h3>Step 3: Upload Shapefile</h3>
    <div>
    	<form id="uploadShapefileForm" action="${pageContext.request.contextPath}/bulkdata/uploadShapefile.htm" method="post" enctype="multipart/form-data">
		    <table class="form_table bulkDataTableForm">
		        <tbody>
		            <tr>
		                <th>
		                    <label class="strong" for="shapefileInput">Upload file:</label>
		                </th>
		                <td>
		                    <input id="shapefileInput" name="shapefile" type="file"/>
		                </td>
		            </tr>
		        </tbody>
		    </table>
		    <div class="textright">
		        <input type="submit" value="Upload shapefile" class="form_action"/>
		    </div>
		</form>
    </div>
</div>

<script type="text/javascript">
    jQuery(function() {
        // Download Template Expand/Collapse
        jQuery("#downloadTemplateToggle").click(function() {
            jQuery("#downloadTemplateWrapper").slideToggle(function() {
                var canSee = jQuery("#downloadTemplateWrapper").css('display') === 'none';
                jQuery("#downloadTemplateToggle").text(canSee ? "Expand" : "Collapse");
            });
        });

        // Upload Spreadsheet Expand/Collapse
        jQuery("#uploadSpreadsheetToggle").click(function() {
            jQuery("#uploadSpreadsheetWrapper").slideToggle(function() {
                var canSee = jQuery("#uploadSpreadsheetWrapper").css('display') === 'none';
                jQuery("#uploadSpreadsheetToggle").text(canSee ? "Expand" : "Collapse");
            });
        });
		
		$("input[name=bulkDataUploadType]").change(function(){
			var value = $("input[@name='option_layout']:checked").val();
			if (value == "spreadsheet") {
				jQuery("#spreadsheetSection").show();
				jQuery("#shapefileSection").hide();
			} else if (value == "shapefile") {
				jQuery("#spreadsheetSection").hide();
                jQuery("#shapefileSection").show();
			} else {
				throw 'unexpected value on change event: ' + value;
			}
		});
		
		var createCensusMethodOptionItems = function(data) {
			var selectNode = jQuery("#censusMethodShapefileTemplateSelect");
			selectNode.empty();
			for(var j=0; j<data.length; ++j) {
                var censusMethod = data[j];
                var cmOption = jQuery("<option>" + censusMethod.name + "</option>");
				cmOption.attr("value", censusMethod.id);
                selectNode.append(cmOption);
            }
		}
		
		jQuery("#surveyShapefileTemplateSelect").change(function() {
			var surveyId = jQuery("#surveyShapefileTemplateSelect").val();
			// populate census method items...
            jQuery.getJSON(bdrs.contextPath + "/bdrs/user/censusMethod/getSurveyCensusMethods.htm", {surveyId:surveyId}, createCensusMethodOptionItems);
		});
		
		// trigger change events to initialise UI
		$("#bulkDataUploadType").change();
		$("#surveyShapefileTemplateSelect").change();
    });
</script>