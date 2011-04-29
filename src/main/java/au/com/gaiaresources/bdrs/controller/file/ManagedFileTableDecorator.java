package au.com.gaiaresources.bdrs.controller.file;

import org.displaytag.decorator.TableDecorator;

import au.com.gaiaresources.bdrs.model.file.ManagedFile;

/**
 * The <code>ManagedFileTableDecorator</code> handles the custom rendering
 * of cell content on the table listing managed files.
 */
public class ManagedFileTableDecorator extends TableDecorator {
    
    public String getActionLinks() {
        String contextPath = this.getPageContext().getServletContext().getContextPath();
        ManagedFile managedFile = (ManagedFile) getCurrentRowObject();
        String url = String.format("%s/bdrs/user/managedfile/edit.htm?id=%d", contextPath, managedFile.getId());
        return String.format("<a href=\"%s\" />Edit</a>", url);
    }
    
    public String getFilename() {
        String contextPath = this.getPageContext().getServletContext().getContextPath();
        ManagedFile managedFile = (ManagedFile) getCurrentRowObject();
        return String.format("<a href=\"%s/files/download.htm?%s\">%s</a>", contextPath, managedFile.getFileURL(), managedFile.getFilename());
    }
    
    public String getSelection() {
        ManagedFile managedFile = (ManagedFile) getCurrentRowObject();
        return String.format("<input type=\"checkbox\" name=\"managedFilePk\" value=\"%d\"/>", managedFile.getId());
    }
}
