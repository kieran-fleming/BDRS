<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<h1>Message User</h1>

<p>Message the owner of this record.</p>

<div class="map_wrapper left" id="map_wrapper">
    <div id="miniMap" class="messageUserMiniMap"></div>
    <div class="recordCount textright"></div>
</div>

<div class="input_container right">
	<div class="messageUserContainer">
		<form method="post">
			   <table>
			       <tr>
		                <th>
			       		      <label title="Text you would like to send to the recipient" for="text">Text to Send:</label>
		                </th>
						<td>
					         <textarea class="validate(required)" title="Text you would like to send to the recipient" id="text" name="text"></textarea>
						</td>
			       </tr>
				
				   <tr>
		                <th>
		                      <label "The email the recipient should use when responding to your inquiry" for="replyEmail">Reply Email:</label> 
		                </th>
		                <td>
		                     <input class="validate(required,email)" title="The email the recipient should use when responding to your inquiry" 
							 id="replyEmail" type="text" name="replyEmail" value="<c:out value="${replyEmail}" />" />
		                </td>
		           </tr>
				   
				   <tr>
				   	    <th>
				   	    	<label title="Sends a copy of the email to yourself" for="sendToSelf">Send Copy to Self:</label>
				   	    </th>
						<td>
							<input type="checkbox" title="Sends a copy of the email to yourself" name="sendToSelf" id="sendToSelf" />
						</td>
				   </tr>
			   </table>
	        
			   <div class="textright">
			        <td class="textright"><input name="submit" type="submit" value="Submit" class="form_action" />    
			   </div>
		</form>
	</div>
</div>

<div class="clear"></div>

<script type="text/javascript">
	$(function() {
		bdrs.map.initBaseMap('miniMap', {});
		var map = bdrs.map.baseMap;
		
		var layerOptions = {
            visible: true,
            includeClusterStrategy: true,
            upperZoomLimit: null,
            lowerZoomLimit: null
        };
        
		var layerArray = new Array();
		var layer = bdrs.map.addKmlLayer(map, "Record Layer", "${pageContext.request.contextPath}/bdrs/map/getRecord.htm?recordPk=${recordId}", layerOptions);
        layerArray.push(layer);
		
		layer.events.register('loadend', layer, function(event){
	        var extent = event.object.getDataExtent();
	        if (extent !== null) {
	            map.zoomToExtent(event.object.getDataExtent(), false);
	        }
	    });
		
		// Add select for KML stuff
        bdrs.map.addSelectHandler(map, layerArray);

		bdrs.map.centerMap(map);
	});
</script>
