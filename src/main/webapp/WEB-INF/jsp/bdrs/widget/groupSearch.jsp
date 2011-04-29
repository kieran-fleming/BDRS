<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<tiles:useAttribute name="widgetId" />
<tiles:useAttribute name="multiselect" />
<tiles:useAttribute name="scrollbars" ignore="true" />
<tiles:useAttribute name="editUrl" ignore="true" />
<tiles:useAttribute name="baseQueryString" ignore="true" />
<tiles:useAttribute name="showActions" ignore="true" />
<tiles:useAttribute name="deleteUrl" ignore="true" />

<div id="${widgetId}Wrapper">
	<table id="${widgetId}"></table>
	<div id="${widgetId}Pager"></div>
</div>

<script type="text/javascript">

    // creates a little helper object and does some data init. See the object definition in bdrs.js 
    // for more details....
    var ${widgetId}Grid = new bdrs.JqGrid("#${widgetId}", '${pageContext.request.contextPath}/webservice/group/searchGroups.htm', '${baseQueryString}');
    
    ${widgetId}Grid.actionLinkFormatter = function(cellvalue, options, rowObject) {
        var links = new Array();
        <c:if test="${not empty editUrl}">
            links.push('<a style="color:blue" href="${editUrl}?groupId=' + rowObject.id + '">Edit</a>');
        </c:if> 
        <c:if test="${not empty deleteUrl}">
            links.push('<a style="color:blue" href="javascript:if(confirm(&quot;Are you sure you want to delete this group?&quot;)) {bdrs.postWith(&quot;${deleteUrl}&quot;, {groupId:' + rowObject.id + '});}">Delete</a>');
        </c:if>
        return links.join(" | ");
    };

    jQuery("#${widgetId}").jqGrid({

            url: ${widgetId}Grid.createUrl(),
            datatype: "json",
            mtype: "GET",
            colNames:['Group Name','Description'
            <c:if test="${showActions}">,'Action'</c:if>
            ],
            colModel:[
                {name:'name',index:'name', width:'30%'},
                {name:'description',index:'description', width:'50%'}
                <c:if test="${showActions}">,{name:'action', width:'20%', sortable:false, formatter:${widgetId}Grid.actionLinkFormatter}</c:if>
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
            caption:"Group Listing"
    });
    
    <c:if test="${scrollbars != true}">
    jQuery("#${widgetId}Wrapper .ui-jqgrid-bdiv").css('overflow-x', 'hidden');
    </c:if>
    
    jQuery("#${widgetId}Pager").jqGrid('navGrid','#${widgetId}Pager',{edit:false,add:false,del:false});
    jQuery("#${widgetId}Filter}").click(function(){
    // turn the search form into a query string and append it to our url...
        var f = jQuery("#${widgetId}SearchForm").serialize();       
        ${widgetId}Grid.setQueryString(f);
        ${widgetId}Grid.reload();
    });

    
</script>