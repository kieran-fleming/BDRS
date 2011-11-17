package au.com.gaiaresources.bdrs.controller.test;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

public class WebImageFileFilter implements FileFilter {
    
    private static final String[] WEB_IMAGE_EXTENSIONS;
    static {
        String[] extensions = {"png", "gif", "jpg"};
        Arrays.sort(extensions);
        WEB_IMAGE_EXTENSIONS = extensions.clone();
    };
    
    @Override
    public boolean accept(File pathname) {
        String name = pathname.getName();
        int pos = name.lastIndexOf('.');
        String ext = name.substring(pos+1);
        return Arrays.binarySearch(WEB_IMAGE_EXTENSIONS, ext.toLowerCase()) > -1;
    }
}