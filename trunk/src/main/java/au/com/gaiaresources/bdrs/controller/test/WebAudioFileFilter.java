package au.com.gaiaresources.bdrs.controller.test;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

public class WebAudioFileFilter implements FileFilter {
    
    public static final String[] WEB_AUDIO_EXTENSIONS;
    static {
        String[] extensions = {"ogg", "mp3"};
        Arrays.sort(extensions);
        WEB_AUDIO_EXTENSIONS = extensions;
    };
    
    @Override
    public boolean accept(File pathname) {
        String name = pathname.getName();
        int pos = name.lastIndexOf('.');
        String ext = name.substring(pos+1);
        return Arrays.binarySearch(WEB_AUDIO_EXTENSIONS, ext.toLowerCase()) > -1;
    }
}