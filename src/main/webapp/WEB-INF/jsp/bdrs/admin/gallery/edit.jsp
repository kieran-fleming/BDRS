<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<c:choose>
    <c:when test="${ gallery.id == null }">
        <h1>Add Gallery</h1>
    </c:when>
    <c:otherwise>
        <h1>Edit Gallery</h1>
    </c:otherwise>
</c:choose>

<p>Lorem ipsum dolor sit amet</p>


<h4>Upload Image Instructions:</h4>
<ul>
	<li>Click on the 'Add Image' button</li>
	<li>Select a 'Full Sized Image' to upload</li>
	<li>Select how to generate your 'Slideshow Sized Image'
	   <ol>
	   	<li>
	   		Auto resize slideshow image: Resizes your uploaded image to 250 pixels wide by 154 pixels high.
	   	</li>
		<li>
            Upload slideshow image: You may resize/crop your own images and upload them. If you have selected this method, please select an image to upload.
        </li>
	   </ol>
	</li>
	<li>Enter your image file properties. These will appear underneth your slideshow as a caption. If you leave these fields empty there will be no caption.</li>
</ul>

<h4>Using your gallery:</h4>
<p>
Once you have successfully saved gallery go to the <a href="${pageContext.request.contextPath}/bdrs/public/embedded/widgetBuilder.htm">Embedded Widgets page</a>. 
Select the 'Image Gallery' widget and edit the display properties as required.
</p>

<h4>Tips on getting the most out of your gallery</h4>
<ul>
	<li>If you are using auto resizing, try to have the images as close to a 1.6:1 width-to-height ratio</li>
	<li>If you are cropping your images manually, make all the images the same dimensions. When creating your embedded widget, set the image width/height properties
	to the same dimensions as your uploaded image</li>
</ul>

<form method="POST" enctype="multipart/form-data" action="${pageContext.request.contextPath}/bdrs/admin/gallery/edit.htm">
    <input type="hidden" name="galleryPk" value="${gallery.id}" />
	<h3>Gallery Properties</h3>
	
    <table>
        <tr>
            <td>Gallery Name:</td>
            <td><input class="validate(required, maxlength(255))" type="text" style="width:40em" name="name" value="<c:out value="${gallery.name}" />" size="40"  autocomplete="off"></td>
        </tr>
        <tr>
            <td>Gallery Description:</td>
            <td><input class="validate(required, maxlength(1023))" type="text" style="width:40em" name="description" value="<c:out value="${gallery.description}" />" size="40"  autocomplete="off"></td>
        </tr>
    </table>
    
    <h3>Images</h3>

    <div id="imageContainer">
        <div class="textright buttonpanel">
            <a id="maximiseLink" class="text-left" href="javascript:bdrs.util.maximise('#maximiseLink', '#imageContainer', 'Enlarge Table', 'Shrink Table')">Enlarge Table</a>
            <input id="addImageBtn" class="form_action" type="button" value="Add Image" />
        </div>
        <table id="images_input_table" class="datatable attribute_input_table">
            <thead>
                <tr>
                    <th>&nbsp;</th>
                    <th>Full Sized Image</th>
					<th>Slideshow Sized Image</th>
                    <th>File Properties</th>
                    <th>Delete</th>
                </tr>
            </thead>
            <tbody id="imageTbody">
            </tbody>
        </table>
    </div>
    
    <div class="buttonpanel textright">
        <input type="submit" class="form_action" type="button" value="Save" />
     </div>
</form>

<div style="display:none">
    <table>
       <tbody id="rowTemplate">
            <tr>
                <td class="drag_handle">
                    <input type="hidden" value="0" class="sort_weight" />         
                </td>   
				<td valign="top">
					<input type="hidden" name="fileUuid" value="${'${'}uuid}" />
					<!-- if the uuid exists the file has been previously uploaded --> 
					{{if (uuid)}}
					<div class="imageGalleryEditImageContainer">
					<a href="${pageContext.request.contextPath}/bdrs/public/gallery/fullImg.htm?uuid=${'${'}uuid}">
                        <img src="${pageContext.request.contextPath}/bdrs/public/gallery/fullImg.htm?uuid=${'${'}uuid}" alt="No image uploaded" width="250" border="30"/>
                    </a>
					</div>
					{{/if}}
					<div style="padding-top:2px">
					   <input type="file" name="fileFile" onchange="jQuery(this).siblings('a').remove();" />
					</div>
                </td>
                <td valign="top">
                	{{if (uuid)}}
                    <div class="imageGalleryEditImageContainer">
                    <a href="${pageContext.request.contextPath}/bdrs/public/gallery/slideshowImg.htm?uuid=${'${'}uuid}">
                        <img src="${pageContext.request.contextPath}/bdrs/public/gallery/slideshowImg.htm?uuid=${'${'}uuid}" width="250" alt="No slide show image uploaded" />
                    </a>
                    </div>
                    <div style="padding-top:2px;" class="imageGalleryInput">                       
                        <div style="padding-top:2px;" class="imageGalleryInput">
                            <input class="changeSlideshow" type="checkbox" onchange="changeSlideshowCheckbox(jQuery(this));" /><label>Change slideshow sized image</label>
                            <input type="hidden" name="slideshowAction" value="none" />
                        </div>
                           <select class="slideshowAction imageGalleryInput" name="slideshowAction" onChange="slideshowActionChanged(jQuery(this));" disabled="disabled" style="display:none">
                               <option value="autoresize_ss">Auto resize slideshow image</option>
                               <option value="upload_ss">Upload slideshow image</option>
                           </select>
                        <input class="uploadSlideshow" type="file" name="fileSlideshowFile" onchange="jQuery(this).parent().siblings('a').remove();" style="display:none" />
                    </div>
					{{else}}
                    <div style="padding-top:2px;" class="imageGalleryInput">                       
                           <select class="slideshowAction imageGalleryInput" name="slideshowAction" onChange="slideshowActionChanged(jQuery(this));">
                               <option value="autoresize_ss">Auto resize slideshow image</option>
                               <option value="upload_ss">Upload slideshow image</option>
                           </select>
                        <input class="uploadSlideshow" type="file" name="fileSlideshowFile" onchange="jQuery(this).parent().siblings('a').remove();" style="display:none" />
                    </div>
					{{/if}}
                </td>
				<td>
					<h4>Description</h4>
					<textarea name="fileDescription" style="width:96%">${'${'}description}</textarea>
					<h4>Credit</h4>
                    <textarea name="fileCredit" style="width:96%">${'${'}credit}</textarea>
					<h4>License</h4>
                    <textarea name="fileLicense" style="width:96%">${'${'}license}</textarea>
				</td>
                <td class="textcenter">                                                                     
                    <a href="javascript: void(0);" onclick="jQuery(this).parents('tr').hide().find('select, input, textarea').attr('disabled', 'disabled').removeClass(); return false;">   
                    <img src="${pageContext.request.contextPath}/images/icons/delete.png" alt="Delete" class="vertmiddle"/>                                                                         
                    </a>
                </td>
            </tr>    
        </tbody> 
    </table>
</div>


<script type="text/javascript">
    $(function() {		
        bdrs.dnd.attachTableDnD('#images_input_table');
        
        $( "#addImageBtn" ).click(function() {
			if (pageContext.numAdded < 10) {
			    addTableRow();
				++pageContext.numAdded;
			} else {
				bdrs.message.set("You may only add 10 images at a time to a gallery. Save to continue adding more images.");
			}
        });
        
        // populate the table
        <c:forEach var="managedFile" items="${managedFileList}">
			addTableRow({
				uuid: "${managedFile.uuid}",
				description: "${managedFile.description}",
				credit: "${managedFile.credit}",
				license: "${managedFile.license}",
				filename: "${managedFile.filename}"
			});
        </c:forEach>
    });
	
	var pageContext = {
		numAdded: 0
	};
	
    // not the greatest code. feel free to refactor.
	var changeSlideshowCheckbox = function(checkbox) {
		if (checkbox.attr('checked')) {
			// disable the hidden input 'slideshowAction'
			checkbox.siblings(':input').attr('disabled','disabled');
			// enable select input 'slideshowAction'
			checkbox.parent().siblings(".slideshowAction").show().removeAttr('disabled');
			// depending on what is selected in the slideshow action select, enable the file upload input
			slideshowActionChanged(checkbox.parent().siblings(".slideshowAction"));
		} else {
			// enable the hidden input 'slideshowAction'
			checkbox.siblings(':input').removeAttr('disabled');
			// disable select input 'slideshowAction'
			checkbox.parent().siblings(".slideshowAction").hide().attr('disabled', 'disabled');
			// disable the slideshow file upload input
			checkbox.parent().siblings(".uploadSlideshow").hide();//.attr('disabled', 'disabled');
		}
	};
	
	// select is the jquery selected <select> node
	var slideshowActionChanged = function(select) {
		if (select.val() == "autoresize_ss") {
			select.siblings(".uploadSlideshow").hide();//.attr('disabled', 'disabled');
		} else if (select.val() == "upload_ss") {
			select.siblings(".uploadSlideshow").show();//.removeAttr('disabled');
		} else {
			throw select.val() + ' is an unexpected value';
		}
	};
    
    var addTableRow = function(managedFileJsonObj) {
		if (managedFileJsonObj) {
			var pkNodes = $("input[name=fileUuid]");
			for (; pkNodes.length > 0; pkNodes.splice(0, 1)) {
				if (pkNodes.val() == managedFileJsonObj.id) {
					bdrs.message.set("Image has already been added.");
					return;
				}
			}
			var row = $('#rowTemplate').tmpl(managedFileJsonObj);
			bdrs.dnd.addDnDRow("#images_input_table", row);
		} else {
			var emptyManagedFileJsonObj = {
				uuid: null,
				description: "",
				credit: "",
				license: ""
			};
			var row = $('#rowTemplate').tmpl(emptyManagedFileJsonObj);
            bdrs.dnd.addDnDRow("#images_input_table", row);
		}
    };
    
</script>