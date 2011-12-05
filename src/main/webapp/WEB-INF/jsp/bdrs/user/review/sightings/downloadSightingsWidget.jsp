<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<p>
    <h3>Click to Download</h3>
    Please choose the desired formats and click the button below to download the current search results as a zip file.
</p>

<div class="align_radio">
    <fieldset>
        <div>
            <input id="kml" type="checkbox" name="download_format" value="KML"
                <c:if test="${ download_kml_selected }">
                    checked="checked"
                </c:if>
            />
            <label for="kml">
                Keyhole Markup Language (KML) &mdash; Can be used for <a href="http://earth.google.com" target="_target">Google Earth</a>
            </label> 
        </div>
        <div>
            <input id="shp" type="checkbox" name="download_format" value="SHAPEFILE" 
                <c:if test="${ download_shp_selected }">
                    checked="checked"
                </c:if>
            />
            <label for="shp">
                Shapefile (SHP) &mdash; Can be used in <a href="http://en.wikipedia.org/wiki/Comparison_of_geographic_information_systems_software" target="_target">GIS applications</a>.
            </label> 
        </div>
        <div>
            <input id="xls" type="checkbox" name="download_format" value="XLS"
                <c:if test="${ download_xls_selected }">
                    checked="checked"
                </c:if>
            />
            <label for="xls">
                Spreadsheet (XLS) &mdash; Can be used for <a href="http://www.libreoffice.org/features/calc/">Spreadsheet Applications</a>
            </label> 
        </div>
    </fieldset>
</div>
<div class="buttonpanel textright">
    <input id="download_button" type="button" class="form_action" value="Download"/>
</div>