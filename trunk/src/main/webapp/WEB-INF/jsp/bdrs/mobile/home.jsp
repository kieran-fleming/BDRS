<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


<script type="text/javascript">
/**
 * Adding event handler to DOM elements.
 * Scroll page up to hide url bar.
 */
jQuery(document).ready(function() {

	// Need to create database connection for problem with reloading home.htm
	createConnection();
	
    jQuery.address.change(function(event) {
         if(event.pathNames.length > 0){
         	bdrs(event.pathNames.pop(), event.parameterNames);
         }else{
        	 bdrs('home', event.parameterNames);
         }
    });
    
   	//window.scrollTo(0, 1);

	if(window.openDatabase){
		ping();
		//going to set regkey cookie for ios application mode
		GR159DB.transaction(function(transaction) {
        	transaction.executeSql('SELECT registrationkey FROM userdefinition;', [], function(transaction, results){
                var row = results.rows.item(0);
                document.cookie="regkey=" + row.registrationkey;
            },errorHandler);
		});
	}
	
});
</script>
<div style="clear:both;"></div>
<div id="onlineContainer">

	<c:choose>
		<c:when test="${hometype=='basic'}">
			<!--show basic home
				<form action="" id="surveysform">
					<dl>
						<dt>
							<label for="surveyselect">Survey:</label>
						</dt>
						<dt>
							<select id="surveyselect"  name="surveyselect" class="selectClass">
						 			<c:forEach items="${surveys}" var="asurvey">
						 				<option value="${asurvey.id}">${asurvey.name}</option>
						 			</c:forEach>
						 		</select>
						</dt>
						<dt>
							<input type="submit" class="form_action" value="add record" id="addrecord"/>
	                    	<input type="submit" class="form_action" value="review" id="review"/>
						</dt>
					</dl>
		 		</form>-->
		</c:when>
		<c:otherwise>

			<!--show education home-->
			<div id="homeBtnContainer">
				<div class="buttonrow">
					<c:forEach var="afeature" items="${features}">
						<div class="buttonbox">
							<a href="${afeature.btnurl}">
								<div class="button">
									<div id="icon${afeature.btnid}"></div>
								</div>
							</a>
							<p class="ico_txt"><label id="label${afeature.btnid}">${afeature.btnlabelname}</label></p>
						</div>
					</c:forEach>
				</div>
			</div>
		</c:otherwise>
	</c:choose>
</div>