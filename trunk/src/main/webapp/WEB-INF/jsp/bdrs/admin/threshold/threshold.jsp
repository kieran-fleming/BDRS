<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<jsp:useBean id="threshold" type="au.com.gaiaresources.bdrs.model.threshold.Threshold" scope="request"/>

<h1>Threshold</h1>
<cw:getContent key="admin/thresholdEdit" />

<form method="post" action="${pageContext.request.contextPath}/bdrs/admin/threshold/edit.htm">
	<p class="storyline">
	    For all
	    <c:choose>
	       <c:when test="${ threshold.id == null }">
	           <select id="class_name" name="class_name"> 
	               <c:forEach items="${ displayNameMap }" var="entry">
			           <option value="${ entry.key }">
			               <c:out value="${ entry.value }"/>
			           </option>
	               </c:forEach>
	           </select>
	       </c:when>
	       <c:otherwise>
	           <input id="threshold_id" type="hidden" name="threshold_id" value="${ threshold.id }"/>
	           <input id="class_name" type="hidden" name="class_name" value="${ threshold.className }"/> 
	           <c:out value="${ displayNameMap[threshold.className] }"/>
	       </c:otherwise>
       </c:choose>
	    
	    where:
	</p>
	
	<div id="conditionWrapper">
		<div class="textright buttonpanel">
	        <a id="maximiseLink" class="text-left" href="javascript:bdrs.util.maximise('#maximiseLink', '#conditionWrapper', 'Enlarge Table', 'Shrink Table')">Enlarge Table</a>
	        <input type="button" class="form_action" value="Add Condition" onclick="bdrs.threshold.addCondition('#condition_index', '#class_name', '#conditionContainer')"/>
	        <input id="condition_index" type="hidden" name="condition_index" value="0"/>
	    </div>
		<table class="datatable">
		    <thead>
		        <tr>
		            <th>Rules</th>
		            <th>Delete</th>
		        </tr>
		    </thead>
		    <tbody id="conditionContainer">
			    <c:forEach items="${ threshold.conditions }" var="condition">
			        <tiles:insertDefinition name="thresholdConditionRow">
			            <tiles:putAttribute name="threshold" value="${ threshold }"/>
			            <tiles:putAttribute name="condition" value="${ condition }"/>
			        </tiles:insertDefinition>
			    </c:forEach>
		    </tbody>
		</table>
	</div>
	
	<p class="storyline">the system shall perform the following actions:</p>
	
	<div id="actionContainer">
	    <div class="textright buttonpanel">
	        <a id="maximiseLink" class="text-left" href="javascript:bdrs.util.maximise('#maximiseLink', '#actionContainer', 'Enlarge Table', 'Shrink Table')">Enlarge Table</a>
	        <input type="button" class="form_action" value="Add Action" onclick="bdrs.threshold.addAction('#action_index', '#class_name', '#actionTable')"/>
	        <input id="action_index" type="hidden" name="action_index" value="0"/>
	    </div>
	    <table id="actionTable" class="datatable">
	        <thead>
	            <tr>
	                <th>Action</th>
	                <th>Input</th>
	                <th>Delete</th>
	            </tr>
	        </thead>
	        <tbody>
	            <c:forEach items="${ threshold.actions }" var="action">
                    <tiles:insertDefinition name="thresholdActionRow">
                        <tiles:putAttribute name="threshold" value="${ threshold }"/>
                        <tiles:putAttribute name="action" value="${ action }"/>
                    </tiles:insertDefinition>
                </c:forEach>
	        </tbody>
	    </table>
	</div>
	
	<p>
	   <input id="enabled" type="checkbox" name="enabled" class="vertmiddle" value="true" <c:if test="${ threshold.enabled }">checked="checked"</c:if>/>
	   <label for="enabled">Enable this threshold</label>
	</p>

    <div class="textright buttonpanel">
        <input type="submit" class="form_action" value="Save"/>
    </div>
</form>

<c:choose>
	<c:when test="${ threshold.id == null }">
		<script type="text/javascript">
		    jQuery(function() {
		        var className = jQuery("#class_name");
		        var changeParams = {
		            conditionContainerSelector : '#conditionContainer'
		        };
		        className.bind('change', changeParams, bdrs.threshold.handlers.changeClass);
		        className.trigger("change");
		    }); 
		</script>
	</c:when>
	<c:otherwise>
	   <script type="text/javascript">
            jQuery(function() {
                jQuery(".condition").each(function(index, elem) {
                    jQuery(elem).find(".pathSelect:last").trigger("change");
                });
            }); 
        </script>
	</c:otherwise>
</c:choose>