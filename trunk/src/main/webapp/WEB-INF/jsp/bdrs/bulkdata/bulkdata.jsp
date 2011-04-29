<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<h1>Bulk Data</h1>
<p>
    On this page you can contribute a spreadsheet full of records to a project.
    To do this:
    <ul>
        <li>download the template,</li>
        <li>fill it out, and</li>
        <li>then upload it.</li>
    </ul>
</p>


<h3>Download Template Spreadsheet</h3>
<div class="textright">
    <a id="downloadTemplateToggle" href="javascript: void(0);">Collapse</a>
</div>
<div id="downloadTemplateWrapper">
    <p>
        Download your template below for your project. There is help, a list of
        locations and a taxonomy checklist included in the downloaded template.
    </p>
    <form id="downloadTemplateForm" action="${pageContext.request.contextPath}/bulkdata/spreadsheetTemplate.htm" method="get">
        <table class="form_table">
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


<h3>Upload Spreadsheet</h3>
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
    });
</script>