
#statistics h3 {
    padding: 0px;
    margin: 0.5em 0em 0.5em 0em;
}

#statistics table {
    padding: 0px;
}

#statistics table th,
#statistics table td {
    padding: 0em 0em 0em 0.5em;
}

#statistics table th {
    text-align: right;
    font-weight: normal;
}
#statistics table td {
    text-align: left;
}

/************************************/
/* Button Styling					*/
/************************************/
input[type=submit],input[type=button] {
    color: dimgray;
    font-weight: bold;
    margin: 0px 8px 0px 8px;
    padding: 4px 16px 4px 16px;
    font-size: 1em;
    cursor: pointer;
    /* Rounded Border */
    border: 1px solid #C8C8C8;
    -webkit-border-radius: 6px;
    -khtml-border-radius: 6px;
    -moz-border-radius: 6px;
    border-radius: 6px;
}

input[type=submit][disabled].form_action,
input[type=button][disabled].form_action,
input[type=submit][disabled].form_action:active,
input[type=button][disabled].form_action:active,
input[type=submit][disabled].form_action:hover,
input[type=button][disabled].form_action:hover {
    background-image: none;
    background-color: silver;
}

/* Page Actions */
input[type=submit].page_action,input[type=button].page_action {
    /* Color Gradient */
    background-image: -moz-linear-gradient(top, #F1F1F1, #CFCFCF);
    background-image: -webkit-gradient(linear, left top, left bottom, color-stop(0, #F1F1F1),
        color-stop(1, #CFCFCF) );
    filter: progid : DXImageTransform.Microsoft.gradient ( GradientType = 0,
        startColorstr = #F1F1F1, endColorstr = #CFCFCF );
    -ms-filter:
        "progid:DXImageTransform.Microsoft.gradient (GradientType=0, startColorstr=#F1F1F1, endColorstr=#CFCFCF)"
        ;
}

input[type=submit].page_action:hover,input[type=button].page_action:hover
    { /* Color Gradient */
    background-image: -moz-linear-gradient(top, #FFFFFF, #CFCFCF);
    background-image: -webkit-gradient(linear, left top, left bottom, color-stop(0, #FFFFFF),
        color-stop(1, #CFCFCF) );
    filter: progid : DXImageTransform.Microsoft.gradient ( GradientType = 0,
        startColorstr = #FFFFFF, endColorstr = #CFCFCF );
    -ms-filter:
        "progid:DXImageTransform.Microsoft.gradient (GradientType=0, startColorstr=#FFFFFF, endColorstr=#CFCFCF)"
        ;
}

input[type=submit].page_action:active,input[type=button].page_action:active
    { /* Color Gradient */
    background-image: none;
    background-color: #C8C8C8;
}

/* Form Actions */
input[type=submit].form_action,input[type=button].form_action {
    /* Color Gradient */
    background-image: -moz-linear-gradient(top, #F6DDCC, #F69900);
    background-image: -webkit-gradient(linear, left top, left bottom, color-stop(0, #F6DDCC),
        color-stop(1, #F69900) );
    filter: progid : DXImageTransform.Microsoft.gradient ( GradientType = 0,
        startColorstr = #F6DDCC, endColorstr = #F69900 );
    -ms-filter:
        "progid:DXImageTransform.Microsoft.gradient (GradientType=0, startColorstr=#F6DDCC, endColorstr=#F69900)"
        ;
    /* Drop Shadow */
    box-shadow: 0px 0px 8px #DBDBDB;
    -moz-box-shadow: 0px 0px 8px #DBDBDB;
    -webkit-box-shadow: 0px 0px 8px #DBDBDB;
    -ms-filter: /* IE8+ */
        "filter:progid:DXImageTransform.Microsoft.dropshadow(OffX=0, OffY=0, Color=#DBDBDB, Positive=true)"
        ;
    filter: /* IE<8 */ filter : progid :
        DXImageTransform.Microsoft.dropshadow ( OffX = 0, OffY = 0, Color =
        #DBDBDB, Positive = true );
}

input[type=submit].form_action:hover,input[type=button].form_action:hover
    { /* Color Gradient */
    background-image: -moz-linear-gradient(top, #F6EEDD, #F6AA11);
    background-image: -webkit-gradient(linear, left top, left bottom, color-stop(0, #F6EEDD),
        color-stop(1, #F6AA11) );
    filter: progid : DXImageTransform.Microsoft.gradient ( GradientType = 0,
        startColorstr = #F6EEDD, endColorstr = #F6AA11 );
    -ms-filter:
        "progid:DXImageTransform.Microsoft.gradient (GradientType=0, startColorstr=#F6EEDD, endColorstr=#F6AA11)"
        ;
}

input[type=submit].form_action:active,input[type=button].form_action:active
    {
    background-image: none;
    background-color: #F69900;
}

.input_container,.input_container_2 {
    padding: 16px 16px 16px 16px;
    /* Rounded Border */
    border: 1px solid #C9C6C6;
    -webkit-border-radius: 8px;
    -khtml-border-radius: 8px;
    -moz-border-radius: 8px;
    border-radius: 8px;
    /* Drop Shadow */
    box-shadow: 0px 0px 8px #DBDBDB;
    -moz-box-shadow: 0px 0px 8px #DBDBDB;
    -webkit-box-shadow: 0px 0px 8px #DBDBDB;
    -ms-filter: /* IE8+ */
        "filter:progid:DXImageTransform.Microsoft.dropshadow(OffX=0, OffY=0, Color=#DBDBDB, Positive=true)"
        ;
    filter: /* IE<8 */ filter : progid :
        DXImageTransform.Microsoft.dropshadow ( OffX = 0, OffY = 0, Color =
        #DBDBDB, Positive = true );
}

.input_container { /* Color Gradient */
    background-image: -moz-linear-gradient(top, #FFFFFF, #E8EAE9);
    background-image: -webkit-gradient(linear, left top, left bottom, color-stop(0, #FFFFFF),
        color-stop(1, #E8EAE9) );
    filter: progid : DXImageTransform.Microsoft.gradient ( GradientType = 0,
        startColorstr = #FFFFFF, endColorstr = #E8EAE9 );
    -ms-filter:
        "progid:DXImageTransform.Microsoft.gradient (GradientType=0, startColorstr=#FFFFFF, endColorstr=#E8EAE9)"
        ;
}

.input_container_2 { /* Color Gradient */
    background-image: -moz-linear-gradient(top, #E8E8E8, #FFFFFF);
    background-image: -webkit-gradient(linear, left top, left bottom, color-stop(0, #E8E8E8),
        color-stop(1, #FFFFFF) );
    filter: progid : DXImageTransform.Microsoft.gradient ( GradientType = 0,
        startColorstr = #E8E8E8, endColorstr = #FFFFFF );
    -ms-filter:
        "progid:DXImageTransform.Microsoft.gradient (GradientType=0, startColorstr=#E8E8E8, endColorstr=#FFFFFF)"
        ;
}

/************************************/
/* Data Table Styling               */
/************************************/
table.datatable {
    width: 100%;
    border-collapse: collapse;
}

.datatable th {
    color: #FFF;
    background-color: #7F7F7F;
    font-weight: bold;
    border: 1px solid #7F7F7F;
}

.datatable td {
    border: 1px dotted #7F7F7F;
}

/************************************/
/* Form Table Styling               */
/************************************/
.form_table {
    width: 100%;
}

.form_table tbody th {
    text-align: right;
}

.form_table th,
.form_table td {
    vertical-align: top;
}

.form_table textarea {
    height: 7em;
    width: 20em;
}

/* Attribute Input Table */
#attribute_input_table tbody td {
    vertical-align: middle;
    margin: 0;
    padding: 0.2em 0.5em 0.2em 0.5em;
}

#attribute_input_table tbody td.drag_handle {
    text-align: center;
    width: 18px;
    padding:0;
}

#attribute_input_table tbody td input[type=text],
#attribute_input_table tbody td select {
    width: 100%;
}

.deleteRow {
    background-color: #F96B6A;
}

/************************************/
/* Form Fieldset Styling            */
/************************************/
form fieldset {
	border: solid 1px grey;
	-webkit-border-radius: 5px;
    -khtml-border-radius: 5px;
    -moz-border-radius: 5px;
    border-radius: 5px;
} 

legend {
	margin: 5px;
	padding: 5px;
}

/************************************/
/* Generic Styling                  */
/************************************/
.hidden {
	display: none;
	visibility: hidden;
}

.left {
    float: left;
}

.right {
    float: right;
}

.clear {
    clear:both;
}

.textleft {
    text-align: left;
}

.textright {
    text-align: right;
}

.textcenter {
   text-align: center;
}

.vertmiddle {
    vertical-align: middle;
}

.italics {
    font-style: italic;
}

.strong {
    font-weight: bold;
}

.fillwidth {
    width: 100%;
}

.nowrap {
    white-space: nowrap;
}

.noborder {
    border-top-width: 0px;
    border-right-width: 0px;
    border-bottom-width: 0px;
    border-left-width: 0px;
}

.scientificName {
    font-style: italic;
}


.error {
	color: red;
}

.success {
	color: green;
}

.nohighlight:focus { 
    outline:none; 
}

.hr {
   border-width: 1px 0px 0px 0px;
   border-color: #CCCCCC;
   border-style: solid;
   margin: 1em 0;
}

.buttonpanel {
    padding: 1em 0em 1em 0em;
}

/************************************/
/* Default Styling                  */
/************************************/
html {
    background: #E4E4E4 url(../../images/cc/bg.png) repeat-x 0px 0px;
}

html.embed {
    background: ${ backgroundColor };
}

body {
    font-size: 12px;
}

body {
    font-size: ${ fontSize }px;
}

body {
    color: #3F3F3F;
}

body {
    color: ${ textColor };
}

body {
    font-family: Arial, Helvetica, sans-serif;
    line-height: 135%;
    margin: 0px;
    padding: 0px;
    display: block;
}

h1, h2, h3 {
    color: #F69900;
}

h1, h2, h3 {
    color: ${ headerColor };
}

h1 {
    font-size: 200%;
    font-weight: bold;
    line-height: 100%;
    padding: 10px 0;
}

h2 {
    font-size: 165%;
    font-weight: bold;
    line-height: 100%;
    padding: 10px 0;
}

h3 {
    font-size: 135%;
    font-weight: bold;
    padding: 10px 0;
}

.wrapper {
    padding-top: 18px;
    margin: 0px auto;
    width: 868px;
}

.contentwrapper {
    background-color: white;
    padding: 20px;
    min-height: 400px;
}

a:link, a:visited {
    color:#F69900;
}

a:link, a:visited {
    text-decoration:none;
}

.pageContent {
    color: #3F3F3F;
    font-size: 12px;
    line-height: 135%;
    padding-left: 10px;
    padding-right: 10px;
}

.message {
    color: red;
}

th, td {
    border: none;
    border-collapse: collapse;
}

input[type=text], input[type=password], textarea {
    padding: 0px 0px 0px 4px;
    background-color: transparent;

    /* Rounded Border */
    border: 1px solid #747474;
    -webkit-border-radius: 4px;
    -khtml-border-radius: 4px;
    -moz-border-radius: 4px;
    border-radius: 4px;

    /* Box Shadow */
    /*box-shadow: 1px 1px 7px #999;
    -moz-box-shadow: 1px 1px 7px #999;
    -webkit-box-shadow: 1px 1px 7px #999;*/
}

textarea {
    resize: both;
}

input[type=text], input[type=password] {
    resize: horizontal;
}

input[readonly],
input[disabled] {
    background-color: #DDDDDD;
}