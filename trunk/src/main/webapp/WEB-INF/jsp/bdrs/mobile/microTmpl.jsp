<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>


<script id="itemsListTmpl" type="text/html">
<ul id="itemsList">{{=content}}</ul>
</script>

<script id="taxaLiTmpl" type="text/html">
<li id="{{=id}}" class="taxaLi">
	<a href="#servicetype=survey&servicename=surveySpeciesForTaxon&taxonId={{=id}}&surveyId=2" class="fullLink">
		<div class="listItemText">
			<img class="listItemImg" src="../{{=thumbNail}}" alt="" />
		</div>
		<div class="liText" style="float: right; font-size: large; font-weight: bolder; padding-right: 2ex;"> &rsaquo; </div>
		<div class="liText">{{=name}}</div>
	</a>
</li>
</script>

<script id="speciesSubsetTmpl" type="text/html">
<div class="species-subset">{{=content}}</div>
</script>

<script id="speciesLiTmpl" type="text/html">
<li class="speciesLi">
	<img class="liImg" src="../images/mv/species/110/359944.jpg" alt="{{=commonName}}"/>
	<div class="liText">{{=commonName}} <em>{{=scientificName}}</em></div>
	<div class="liBtns">
		<a href="#servicetype=record&servicename=formForSpecies&id={{=id}}">
			<div class="liIcon"></div>
		</a>						
		<a href="#servicetype=taxon&servicename=profileById&id={{=id}}">
			<div class="liIcon"></div>
		</a>
	</div>
</li>
</script>

<script id="noSpeciesMatchTmpl" type="text/html">
<div id="noSpeciesMatch" class="transpBoxPadding">
	<h2>No Species Matches</h2>
	<p>
		Unfortunately there were no species that matched your
		description. Click
		<a href="javascript: void(0)">here</a>
		if you would like to try to reidentify your species.
	</p>
</div>
</script>
            
<script id="recordsListContainerTmpl" type="text/html">
	<div id="record_list_container">{{=content}}</div>	
</script>
	  			  		
<script id="simpleMenuContainerTmpl" type="text/html">
	<div class="simpleMenu">{{=content}}</div>
</script>

<script id="simpleMenuItemTmpl" type="text/html">
	<span class="{{=menu_item_class}}">{{=menu_item_text}}</span>
</script>	  		
	  		


<script id="speciesProfileTmpl" type="text/html">
<h1>Species Profile</h1>
<div id="speciesInfoContainer">
	<div id="speciesNameTitle">
		<div id="speciesCommonName"><span id="commonName"></span></div>
		<div id="speciesScientificName"><span id="scientificName"></span></div>
	</div>
	<div id="speciesInfoLeft">
		{{=speciesInfoLeft}}
	</div>
	<div id="speciesInfoRight">
		{{=speciesInfoRight}}
	</div>
	<div id="speciesInfoBottom" /></div>
</div>
</script>

<script id="textProfileTmpl" type="text/html">
<div id="{{=header}}">
	<div class="speciesInfoItemHeader">{{=header_titlecase}}</div>
	<div>{{=content}}</div>
</div>
</script>

<script id="textOtherProfileTmpl" type="text/html">
<tr id="{{=header}}">
	<td>{{=header_titlecase}}:</td>
	<td>{{=content}}</td>
</tr>
</script>

<script id="climatewatchProfileTmpl" type="text/html">
<div id="{{=header}}">
	<a href="{{=content}}">
		<img src="../../images/mv/climatewatch.logo.png" alt="This species is a ClimateWatch indicator species. Click here to find out more" title="This species is a ClimateWatch indicator species. Click here to find out more"/>
	</a>
</div>
</script>

<script id="speciesInfoRightTmpl" type="text/html">
{{=DISTINCTIVE ? tmpl("textProfileTmpl",{"header": DISTINCTIVE.header, "header_titlecase":titleCaps(DISTINCTIVE.header.toLowerCase()), "content":DISTINCTIVE.content}) : ""}}
{{=IDENTIFYINGCHARACTERS ? tmpl("textProfileTmpl",{"header": IDENTIFYINGCHARACTERS.header, "header_titlecase":titleCaps(IDENTIFYINGCHARACTERS.header.toLowerCase()), "content":IDENTIFYINGCHARACTERS.content}) : ""}}
{{=HABITAT ? tmpl("textProfileTmpl",{"header": HABITAT.header, "header_titlecase":titleCaps(HABITAT.header.toLowerCase()), "content":HABITAT.content}) : ""}}
{{=BIOLOGY ? tmpl("textProfileTmpl",{"header": BIOLOGY.header, "header_titlecase":titleCaps(BIOLOGY.header.toLowerCase()), "content":BIOLOGY.content}) : ""}}
<div id="otherInformation">
	<div class="speciesInfoItemHeader">Other Information</div>
	<table>
		{{=DIET ? tmpl("textOtherProfileTmpl",{"header": DIET.header, "header_titlecase":titleCaps(DIET.header.toLowerCase()), "content":DIET.content}) : ""}}
		<tr>
			<td class="tableHeader">Taxonomy:</td>
			<td></td>
		</tr>
		{{=PHYLUM ? tmpl("textOtherProfileTmpl",{"header": PHYLUM.header, "header_titlecase":titleCaps(PHYLUM.header.toLowerCase()), "content":PHYLUM.content}) : ""}}
		{{=CLASS ? tmpl("textOtherProfileTmpl",{"header": CLASS.header, "header_titlecase":titleCaps(CLASS.header.toLowerCase()), "content":CLASS.content}) : ""}}
		{{=ORDER ? tmpl("textOtherProfileTmpl",{"header": ORDER.header, "header_titlecase":titleCaps(ORDER.header.toLowerCase()), "content":ORDER.content}) : ""}}
		{{=FAMILY ? tmpl("textOtherProfileTmpl",{"header": FAMILY.header, "header_titlecase":titleCaps(FAMILY.header.toLowerCase()), "content":FAMILY.content}) : ""}}
		{{=GENUS ? tmpl("textOtherProfileTmpl",{"header": GENUS.header, "header_titlecase":titleCaps(GENUS.header.toLowerCase()), "content":GENUS.content}) : ""}}
		{{=SPECIES ? tmpl("textOtherProfileTmpl",{"header": SPECIES.header, "header_titlecase":titleCaps(SPECIES.header.toLowerCase()), "content":SPECIES.content}) : ""}}
		<tr>
			<td class="tableHeader">Status:</td>
			<td></td>
		</tr>
		{{=status}}
	</table>
	{{=climatewatch}}
</div>
</script>

<script id="speciesInfoProfileImgTmpl" type="text/html">
<a rel="lightbox-mygallery" href="../../{{=profile_img_med}}" title="{{=header}}">
	<img class="lbox" src="../../{{=profile_img_med}}" id="speciesProfileImg" alt="{{=header}}"/>
</a>
</script>

<script id="distributionMapTmpl" type="text/html">
<a rel="lightbox" href="../../{{=map_thumb}}" title="Where is it found?">
	<img class="lbox distMap" src="../../{{=map_thumb}}" />
</a>
</script>

<script id="imageCreditTmpl" type="text/html">
<li class="creditText">Image:&nbsp;{{=credit}}</li>
</script>

<script id="statusTmpl" type="text/html">
<tr><td></td><td>{{=status}}</td></tr>
</script>

<script id="msgTmpl" type="text/html">
<p class="message">
	<span id="closeMsg">X</span>
	{{=msgs}}
</p>
</script>

<script id="speciesInfoLeftTmpl" type="text/html">
<div id="speciesImgContainer">
	{{=profileImage}}
	<ul class="imageCredits">
		{{=profile_img_credit}}
	</ul>
	<div class="clear">
		<a href="#mode={{=mode}}&id={{=id}}&tax_id={{=tax_id}}&page={{=rec_page}}" class="fullLink">
			<input id="makeObservationBtn" type="button" class="form_action" value="Make Observation" />
		</a>
	</div>
</div>
<div id="{{=DISTRIBUTION.header}}">
	<div class="speciesInfoItemHeader">{{=DISTRIBUTION.header_titlecase}}</div>
	{{=map_thumb}}
	<div>{{=DISTRIBUTION.content}}</div>
</div>
</script>

<script id="labelInputTmpl" type="text/html">
<div class="record_form_item">
	<label for="{{=labelfor}}">{{=labelname}}:</label>
	{{=content}}
</div>
</script>

<script id="recordsListItemTmpl" type="text/html">
<div class="record_list_item" id="record{{=recordid}}">
	<input type="checkbox" id="{{=recordid}}" class="record_checkbox"/>

	<a href="#servicetype=survey&servicename=attributesForSurvey&callback=renderRecordForm&surveyId={{=surveyId}}&recordId={{=recordid}}&onlineRecordId={{=onlineRecordId}}&mode=edit">	
		<div class="species_name">
			<div class="commonName">{{=commonname}}</div>
			<span class="scientificName">{{=scientificname}}</span>
		</div>
		<div class="date">{{=date}} </div>
	</a>
</div>
</script>

<script id="noRecordsTmpl" type="text/html">
<p style="padding: 1em;">
	You have no records at this stage. To start recording you have to select one of the four buttons on the
	<a href="home.html" style="color: blue; text-decoration: underline">home</a> screen.
</p>
</script>

<script id="recordUploadTmpl" type="text/html">
<div style="text-align:center;">
	<input id="uploadSubmit" type="button" style=" margin-top: 10px; margin-bottom: 10px; margin-left: 4px; margin-right: 4px" value="Upload" class="form_action"/>
</div>
</script>

<script id="breadcrumbLinkTmpl" type="text/html">
<a title="Go to the {{=text}} page" href="{{=link}}"><span class="breadcrumb">{{=text}}</span></a>&gt;          
</script>

<script id="breadcrumbTmpl" type="text/html">
{{=text}}
</script>

<script id="idToolTmpl" type="text/html">
<form id="featForm" style="padding:1.5em;">
	<div id="transpBox">
		{{=content}}
		<div style="text-align: center; clear: left; margin-top: 10px;">
			<input type="button" class="form_action" value="Clear Options" id="idToolFormReset"/>
			<input type="button" class="form_action" value="View Results" id="featuresSubmit"/>
		</div>
	</div>
</form>
</script>

<script id="idToolSectionTmpl" type="text/html">
<div class="idToolSection">
	<div class="idToolHeader">{{=header}}</div>
	{{=content}}
	<div style="clear:both;"></div>
</div>
</script>

<script id="idToolItemIMTmpl" type="text/html">
<div style="float: left; text-align: center; padding: 2px; ">
	<img src="../../{{=value}}" alt="{{=value}}"/><br/>
	<input type="radio" name="{{=name}}" value="{{=value}}" /> <br/>
</div>
</script>

<script id="idToolItemSTTmpl" type="text/html">
<div style="float: left; text-align: center; padding: 2px; ">
	<input type="radio" name="{{=name}}" value="{{=value}}"/>{{=value}}
</div>
</script>

<script id="uploadSuccessfulTmpl" type="text/html">
<div class="transpBoxPadding">
	<p>You have succesfully uploaded {{=savedRecordCount}} records.</p>
	<p>Return to the <span id="homeLinkText" style="color:blue; text-decoration: underline;">home</span> screen to continue making observations.</p>
</div>
</script>

<script id="uploadFailedTmpl" type="text/html">
<div id="transpBox" style="margin-bottom: 2em;">
	<div class="transpBoxPadding">
		<h1>{{=errorCode}} Error</h1>
		<p>
			A fatal error was encountered during the upload.<br/>
			Please correct the following errors before reattempting the upload.
		</p>
		<p>{{=errorDescription}}</p>
	</div>
</div>
{{=content}}
</script>

<script id="syncTmpl" type="text/html">
<a href="javascript:alert('{{=message}}')" title="{{=message}}" id="sync" class="{{=online}} {{=status}} {{=type}}"></a>
</script>

<script id="loadingTmpl" type="text/html">
<a href="javascript:alert('{{=message}}')" title="{{=message}}" id="sync" class="{{=status}}"></a>
</script>

<script id="helpTmpl" type="text/html">
</script>

<script id="homeTmpl" type="text/html">
<form action="" id="surveysform">
	<label for="surveyselect">Survey:</label>
	<div>
	<select id="surveyselect"  name="surveyselect">
		{{=content}}
	</select>
	</div>
	<div>
		<input type="button" class="form_action" value="add record" id="addrecord"/>
		<input type="button" class="form_action" value="review" id="review"/>
	</div>
</form>
</script>















<script id="indexTmpl" type="text/html">
<div id="content">
	{{=headerLarge}}
	{{=list}}
</div>
</script>

<script id="mainTmpl" type="text/html">
<div id="content">
	{{=headerSmall}}
	{{=pageNavigation}}
	{{=list}}
</div>
</script>

<script id="headerLargeTmpl" type="text/html">
<div id="headerLarge">
	<div id="logoLarge"></div>
	<div id="title">{{=title}}</div>
	<div id="subTitle">{{=subTitle}}</div>
</div>
</script>

<script id="headerSmallTmpl" type="text/html">
<div id="headerSmall">
	<div id="logoSmall"></div>
	<div id="title">{{=title}}</div>
	<div id="subTitle">{{=subTitle}}</div>
</div>
</script>

<script id="pageNavigationTmpl" type="text/html">
<div id="pageNavigation">
	<a href="{{=previousUrl}}">
		<img class="listItemIcon" src="{{=url}}" alt="{{=alt}}">
		<div id="pageNavigationText">
			<span id="previousPage">{{=previousPage}}</span>
			<span id="currentPage">{{=currentPage}}</span>
		</div>
	</a>
	<a href="{{=homeUrl}}">
		<img id="homeIcon" src="{{=homeIconUrl}}" alt="home">
	</a>
</div>
</script>

<script id="listMenuTmpl" type="text/html">
<div id="listMenu">{{=listMenuItems}}</div>
</script>

<script id="listMenuItemTmpl" type="text/html">
<div>
	<a href="{{=listMenuItemUrl}}" rel="address:{{=listMenuItemUrl}}">
		<img class="listMenuItemIcon" src="{{=url}}" alt="{{=alt}}">
		<br>
		<div class="listMenuItemName">{{=listMenuItemName}}</div>
	</a>
</div>
</script>

<script id="listSearchTmpl" type="text/html">
<div>
	<label for="searchList">{{=searchListLabel}}:</label>
	<br>
	<input id="searchList" type="text"/>
</div>
</script>

<script id="listTmpl" type="text/html">
<div class="list">{{=listItems}}</div>
</script>

<script id="listItemTmpl" type="text/html">
<div id="{{=id}}" class="listItem">
	<img class="listItemIcon" src="{{=url}}" alt="{{=alt}}">
	<div class="listItemText">{{=listItemText}}</div>
</div>
</script>

<script id="listRecordTmpl" type="text/html">
<div id="recordList">{{=recordListItems}}</div>
</script>

<script id="listRecordItemTmpl" type="text/html">
<div class="recordListItem">
	<input type="checkbox" id="record{{=recordId}}" class="recordCheckbox"/>
	<a href="#surveys/{{=surveyId}}/edit/{{=recordId}}>	
		<div class="species_name">
			<div class="commonName">{{=commonname}}</div>
			<div class="scientificName">{{=scientificname}}</div>
			<div class="date">{{=date}}</div>
		</div>
	</a>
</div>
</script>

<script id="formRecordTmpl" type="text/html">
<form id="recordForm">
	<div class="record_form_item">
		<label for="survey_species_search">Species name:</label>
		<input id="survey" name="survey" type="hidden" value=""/>
		<input id="record" name="record" type="hidden" value=""/>
		<input id="onlineRecordId" name="onlineRecordId" type="hidden" value=""/>
		<input id="selected_species" name="selected_species" type="hidden" value=""/>
		<input id="survey_species_search" type="text"  value="" class="ui-autocomplete-input input_transparant input validate(required)" autocomplete="off" role="textbox" aria-autocomplete="list" aria-haspopup="true"/>
		<div id="autocomplete_choices" class="autocomplete"></div>
		<div style="clear:both;"></div>
	</div>
	<div class="record_form_item">
		<label for="locationList">Location:</label>
		<input id="locationid" name="locationid" type="hidden" value=""/>
		<select id="locationList" name="locationList" class="selectClass">
			<option>
		</select>
		<span id="locMsg" style="line-height:25px; vertical-align:middle;"></span>
		<div style="clear:both;"></div>
	</div>
	<div class="record_form_item">
		<label for="latitude">Latitude:</label>
		<input id="latitude" name="locationLatitude" class="input_transparant validate(range(-90,90), number)" type="text" value=""/>
		<span id="latMsg"></span>
		<div style="clear:both;"></div>
	</div>
	<div class="record_form_item">
		<label for="longitude">Longitude:</label>
		<input id="longitude" name="locationLongitude" class="input_transparant validate(range(-180,180), number)" type="text" value="" autocomplete="off"/>
		<span id="longMsg"></span>
		<div style="clear:both;"></div>
	</div>
	<div class="record_form_item">
		<label for="time">Time:</label>
		<input id="time" name="time" class="input_transparant input validate(time)" type="text" size="23"/>
		<div style="clear:both;"></div>
	</div>                		
	<div class="record_form_item">
		<label for="when">Date:</label>
		<input id="when" name="when" class="input_transparant datepicker_historical input validate(required)" type="text" size="23"/>
		<div style="clear:both;"></div>
	</div>
	<div class="record_form_item">
		<label for="number">Number seen:</label>
		<select id="number" name="number" class="input_transparant input validate(required)">
			<option value=""></option>
			<option value="1">1</option>
			<option value="2">2</option>
			<option value="3">3</option>
			<option value="4">4</option>
			<option value="0">4+</option>
		</select>
		<div style="clear:both;"></div>
	</div>
	{{=content}}
	<div class="record_form_item">
		<label for="notes">Notes:</label>
		<textarea id="notes" name="notes"></textarea>
		<div style="clear:both;"></div>
	</div>
	<div class="record_form_item">
		<label></label>
		<input type="submit" id="save_or_update" value="save" />
		<div style="clear:both;"></div>
	</div>
</form>
</script>
           
<script id="inputSelectTmpl" type="text/html">
<select name="attribute{{=id}}" id="{{=id}}" class="input_transparant input {{=vRules}}">
	<option value=""></option>
	{{=content}}
</select>
<div style="clear:both;"></div>
</script>

<script id="inputSelectOptionTmpl" type="text/html">
<option value="{{=id}}">{{=option}}</option>
</script>

<script id="inputTextTmpl" type="text/html">
<input id="{{=id}}" name="attribute{{=id}}" type="text" class="input_transparant input {{=vRules}}" value="{{=value}}"/>
<div style="clear:both;"></div>
</script>

<script id="inputDateTmpl" type="text/html">
<input id="{{=id}}" name="attribute{{=id}}" type="text" value="{{=value}}" class="datepicker input_transparant input {{=vRules}}"/>
<div style="clear:both;"></div>
</script>

<script id="inputTextAreaTmpl" type="text/html">
<textarea id="{{=id}}" name="attribute{{=id}}" class="input_textarea {{=vRules}}"></textarea>
<div style="clear:both;"></div>
</script>

<script id="listHelpIdTmpl" type="text/html">
<div id="listHelpId">
	<form id="helpIdForm">
	</form>
</div>
</script>

<script id="listHelpIdItemTmpl" type="text/html">
<div class="listItemIdent">
	<div class="listItemIdentTitle">{{=listItemIdentTitle}}</div>
	<div class="listItemIdentDescription">{{=listItemIdentDescription}}</div>
	{{=listItemIdentOptions}}
</div>
</script>

<script id="listItemIdentOptionTmpl" type="text/html">
</script>

<script id="listItemIdentOptionAndImgTmpl" type="text/html">
</script>

<script id="dialogTmpl" type="text/html">
<div id="dialog" title="Basic dialog">
	<p id="dialog_content"></p>
</div>
</script>
