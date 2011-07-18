<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<tiles:useAttribute name="widgetId" />
<tiles:useAttribute name="multiselect" />
<tiles:useAttribute name="scrollbars" ignore="true" />
<tiles:useAttribute name="baseQueryString" ignore="true" />
<tiles:useAttribute name="editUrl" ignore="true" />
<tiles:useAttribute name="showActions" ignore="true" />
<tiles:useAttribute name="deleteUrl" ignore="true" />

<!--
<h4>Search Map Layers</h4>
    <form id="${widgetId}SearchForm">
        <table>
            <tr>
                <td class="formlabel">Map Name:</td>
                <td><input type="text" style="width:15em" name="name" value="<c:out value="" />" size="40"  autocomplete="off" /></td>
            </tr>
            <tr>
                <td class="formlabel">Map Description:</td>
                <td><input type="text" style="width:15em" name="description" value="<c:out value="" />" size="40"  autocomplete="off" /></td>
            </tr>
        </table>
    </form>
<div class="buttonpanel textright">
    <button id="${widgetId}Filter" class="form_action">Filter</button>
</div>
-->

<div id="${widgetId}Wrapper">
<table id="${widgetId}"></table>
<div id="${widgetId}Pager"></div>
</div>

<script type="text/javascript">
    // creates a little helper object and does some data init. See the object definition in bdrs.js 
    // for more details....
    var ${widgetId}_GridHelper = new bdrs.JqGrid("#${widgetId}", '${pageContext.request.contextPath}/bdrs/admin/gallery/listService.htm', '${baseQueryString}');
    
    ${widgetId}_GridHelper.actionLinkFormatter = function(cellvalue, options, rowObject) {
        var links = new Array();
        <c:if test="${not empty editUrl}">
            links.push('<a style="color:blue" href="${editUrl}?galleryId=' + rowObject.id + '">Edit</a>');
        </c:if> 
        <c:if test="${not empty deleteUrl}">
            links.push('<a style="color:blue" href="javascript:if(confirm(&quot;Are you sure you want to delete this gallery?&quot;)) {bdrs.postWith(&quot;${deleteUrl}&quot;, {galleryPk' + rowObject.id + '});}">Delete</a>');
        </c:if>
        return links.join(" | ");
    };
    
    jQuery("#${widgetId}").jqGrid({
            url: ${widgetId}_GridHelper.createUrl(),
            datatype: "json",
            mtype: "GET",
            colNames:['Gallery Name','Description', 'Number of Images'
            <c:if test="${showActions}">,'Action'</c:if>
            ],
            colModel:[
                {name:'name',index:'name'},
                {name:'description', index:'description'},
                {name:'numImages', sortable: false, width:'50'}
                <c:if test="${showActions}">,{name:'action', width:'30', sortable:false, formatter:${widgetId}_GridHelper.actionLinkFormatter}</c:if>
            ],
            autowidth: true,
            jsonReader : { repeatitems: false },
            rowNum:10,
            rowList:[10,20,30],
            pager: '#${widgetId}Pager',
            sortname: 'name',
            viewrecords: true,
            sortorder: "asc",
            <c:if test="${multiselect == true}">         
                multiselect: true,
            </c:if>
            <c:if test="${scrollbars != true}">
                width: '100%',
                height: '100%',
            </c:if>
            caption:"Gallery Listing"
    });
    
    <c:if test="${scrollbars != true}">
    jQuery("#${widgetId}Wrapper .ui-jqgrid-bdiv").css('overflow-x', 'hidden');
    </c:if>
    
    jQuery("#${widgetId}Pager").jqGrid('navGrid','#${widgetId}Pager',{edit:false,add:false,del:false});
    jQuery("#${widgetId}Filter}").click(function(){
    // turn the search form into a query string and append it to our url...
        var f = jQuery("#${widgetId}SearchForm").serialize();
        ${widgetId}_GridHelper.setQueryString(f);
        ${widgetId}_GridHelper.reload();
    });

</script>