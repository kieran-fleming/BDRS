<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<%@page import="au.com.gaiaresources.bdrs.model.method.Taxonomic"%>

<jsp:useBean id="censusMethod" scope="request" type="au.com.gaiaresources.bdrs.model.method.CensusMethod"/> 

<c:choose>
    <c:when test="${ censusMethod.id == null }">
        <h1>Add Census Method</h1>
    </c:when>
    <c:otherwise>
        <h1>Edit Census Method</h1>
    </c:otherwise>
</c:choose>

<cw:getContent key="admin/censusMethodEdit" />

<form method="POST">
    <input type="hidden" name="group_pk" value="${censusMethod.id}" />
    <table class="form_table">
        <tr>
            <th>Census Method Name:</th>
            <td><input class="validate(required, maxlength(200))" type="text" style="width:40em" name="censusMethodName" value="<c:out value="${censusMethod.name}" />" size="40"  autocomplete="off"></td>
        </tr>
        <tr>
            <th>Description:</th>
            <td>
            	<textarea rows="4" class="validate(maxlength(1023))" name="description"><c:out value="${censusMethod.description}"/></textarea>
            </td>
        </tr>
        <tr>
            <th>Type:</th>
            <td>
                <input type="text" name="type" value="<c:out value="${censusMethod.type}"/>"/> 
            </td>
        </tr>
        <tr>
            <th>Taxonomic:</th>
            <td>
            	<select name="taxonomic">
                    <c:forEach var="taxonomicChoice" items="<%= Taxonomic.values() %>">
                        <jsp:useBean id="taxonomicChoice" type="au.com.gaiaresources.bdrs.model.method.Taxonomic"/>
                        <option value="<%= taxonomicChoice.toString() %>"
                            
                            <c:if test="<%= taxonomicChoice.equals(censusMethod.getTaxonomic()) %>">
                                selected="selected"
                            </c:if>
                        >
                            <%= taxonomicChoice.getName() %>
                        </option>
                    </c:forEach>
                </select>
            </td>
        </tr>
		<tr>
			<th title="The record will be a single point on the map">Enter record as a point:</th>
			<td title="The record will be a single point on the map">
				<input type="checkbox" value="true" name="drawPoint" <c:if test="${censusMethod.drawPointEnabled}">checked="checked"</c:if> />
			</td>
		</tr>
		<tr>
            <th title="The record will be a line on the map">Enter record as a line:</th>
            <td title="The record will be a line on the map">
            	<input type="checkbox" value="true" name="drawLine" <c:if test="${censusMethod.drawLineEnabled}">checked="checked"</c:if> />
		    </td>
        </tr>
		<tr>
            <th title="The record will be a polygon on the map">Enter record as a polygon:</th>
            <td title="The record will be a polygon on the map">
            	<input type="checkbox" value="true" name="drawPolygon" <c:if test="${censusMethod.drawPolygonEnabled}">checked="checked"</c:if> />
			</td>
        </tr>
    </table>
    
    <h3>Census Method Attributes</h3>
    
    <div id="censusMethodAttributeContainer">
	    <div class="textright buttonpanel">
	        <a id="maximiseLink" class="text-left" href="javascript:bdrs.util.maximise('#maximiseLink', '#censusMethodAttributeContainer', 'Enlarge Table', 'Shrink Table')">Enlarge Table</a>
	        <input type="button" class="form_action" value="Add Another Field" onclick="bdrs.attribute.addAttributeRow('#attribute_input_table', false, false)"/>
	    </div>
	    <table id="attribute_input_table" class="datatable attribute_input_table">
	        <thead>
	            <tr>
	                <th>&nbsp;</th>
	                <th>Description on the Form</th>
	                <th>Name in the Database</th>
	                <th>Field Type</th>
	                <th>Mandatory</th>
	                <th>Options (separated by comma)</th>
	                <th>Delete</th>
	            </tr>
	        </thead>
	        <tbody>
	            <c:forEach items="${ attributeFormFieldList }" var="formField">
	                <tiles:insertDefinition name="attributeRow">
	                    <tiles:putAttribute name="formField" value="${ formField }"/>
	                    <tiles:putAttribute name="showScope" value="false"/>
	                </tiles:insertDefinition>
	            </c:forEach>    
	        </tbody>
	    </table>	
    </div>

    <h3>Sub Census Methods</h3>

    <div id="subCensusMethodContainer">
        <div class="textright buttonpanel">
            <a id="maximiseLink" class="text-left" href="javascript:bdrs.util.maximise('#maximiseLink', '#subCensusMethodContainer', 'Enlarge Table', 'Shrink Table')">Enlarge Table</a>
            <input id="addCensusMethodBtn" class="form_action" type="button" value="Add Census Method" />
        </div>
        <table id="censusMethod_input_table" class="datatable attribute_input_table">
            <thead>
                <tr>
                    <th>&nbsp;</th>
                    <th>Census Method Name</th>
                    <th>Taxonomic</th>
                    <th>Delete</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${ censusMethod.censusMethods }" var="cm">
					<tiles:insertDefinition name="censusMethodEditRow">
						<tiles:putAttribute name="id" value="${cm.id}"/>
						<tiles:putAttribute name="name" value="${cm.name}"/>
						<tiles:putAttribute name="taxonomic" value="${cm.taxonomic}" />
					</tiles:insertDefinition>
                </c:forEach>
            </tbody>
        </table>
    </div>

    <div class="buttonpanel textright">
        <input type="submit" class="form_action" type="button" value="Save" />
     </div>
</form>

<!--  DIALOGS GO HERE  ! -->
<div id="addCensusMethodDialog" title="Add Sub Census Methods">
    <tiles:insertDefinition name="censusMethodGrid">
           <tiles:putAttribute name="widgetId" value="addCensusMethodGrid"/>
           <tiles:putAttribute name="multiselect" value="false"/>
           <tiles:putAttribute name="scrollbars" value="true" />
    </tiles:insertDefinition>
</div>

<script type="text/javascript">
    $(function() {
        bdrs.dnd.attachTableDnD('#attribute_input_table');
        bdrs.dnd.attachTableDnD('#censusMethod_input_table');
        
        $( "#addCensusMethodDialog" ).dialog({
            width: 'auto',
            modal: true,
            autoOpen: false,
            zIndex: bdrs.MODAL_DIALOG_Z_INDEX,
            buttons: {
                "Ok": function() {
                    var selected = addCensusMethodGrid_GridHelper.getSelected();
                    if (selected) {
                        // single select so...
                        bdrs.censusMethod.addCensusMethodRow("#censusMethod_input_table", selected);
                    }   
                    $( this ).dialog( "close" );
                },
                Cancel: function() {
                    $( this ).dialog( "close" );
                }
            }
        });
        
        $( "#addCensusMethodBtn" )
            .click(function() {
                addCensusMethodGrid_GridHelper.reload();
                $( "#addCensusMethodDialog" ).dialog( "open" );
        });
    });
    
</script>

