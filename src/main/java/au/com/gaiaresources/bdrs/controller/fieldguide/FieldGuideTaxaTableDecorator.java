package au.com.gaiaresources.bdrs.controller.fieldguide;

import org.displaytag.decorator.TableDecorator;

import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.SpeciesProfile;

/**
 * The <code>FieldGuideTaxaTableDecorator</code> handles the custom rendering
 * of cell content on the table listing taxons in the field guide.
 */
public class FieldGuideTaxaTableDecorator extends TableDecorator {
    
    public String getThumbnail() {
        
        IndicatorSpecies taxon = (IndicatorSpecies)getCurrentRowObject();
        
        SpeciesProfile imgProfile = null;
        for(SpeciesProfile profile : taxon.getInfoItems()) {
            if(imgProfile == null && profile.isImgType()) {
                imgProfile = profile;
            }
        }
        
        if(imgProfile != null) {
            String fieldGuideURL = getTaxonFieldGuideURL(taxon);
            String imgURL = getFileURL(imgProfile);
            return String.format("<a href=\"%s\"><img class=\"max_size_img\" src=\"%s\"/></a>", fieldGuideURL, imgURL);
        } else {
            return "<span>&nbsp;</span>";
        }
    }
    
    public String getScientificName() {
        IndicatorSpecies taxon = (IndicatorSpecies)getCurrentRowObject();
        return toTextAnchorLink(getTaxonFieldGuideURL(taxon), taxon.getScientificName());
    }
    
    public String getCommonName() {
        IndicatorSpecies taxon = (IndicatorSpecies)getCurrentRowObject();
        return toTextAnchorLink(getTaxonFieldGuideURL(taxon), taxon.getCommonName());
    }
    
    private String toTextAnchorLink(String url, String text) {
        return String.format("<a href=\"%s\">%s</a>", url, text);
    }
    
    private String getFileURL(SpeciesProfile profile) {
        String contextpath = this.getPageContext().getServletContext().getContextPath();
        return String.format("%s/files/downloadByUUID.htm?uuid=%s", contextpath, profile.getContent());
    }
    
    private String getTaxonFieldGuideURL(IndicatorSpecies taxon) {
        String contextpath = this.getPageContext().getServletContext().getContextPath();
        return String.format("%s/fieldguide/taxon.htm?id=%d", contextpath, taxon.getId());
    }
}
