package au.com.gaiaresources.bdrs.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;

public class ZipUtils {
    public static final Logger log = Logger.getLogger(ZipUtils.class);
    
    /**
     * Unzips the zip file to the specified directory.
     * 
     * @param zipFile the file to be decompressed.
     * @param dir the location where decompressed files shall be saved.
     * @throws IOException thrown if there has been a problem reading data
     * from the zip file or writing data to the destination.
     */
    public static void decompressToDir(ZipFile zipFile, File dir) throws IOException {
        
        byte[] buffer = new byte[4096];
        
        File file;
        InputStream is;
        FileOutputStream fos;
        log.debug("Zip File: "+zipFile.getName());
        Enumeration<? extends ZipEntry> zipFileEntries = zipFile.entries();
        ZipEntry entry;
        while(zipFileEntries.hasMoreElements()) {
            entry = zipFileEntries.nextElement();
            log.debug("Zip Entry: "+entry.getName());
            file = new File(dir, entry.getName());
            if(entry.isDirectory()) {
                boolean dirCreateSuccess = file.mkdirs();
                if(!dirCreateSuccess) {
                    throw new IOException("Unable to create directory path: "+file.getAbsolutePath());
                }
            } else {
                File parentFile = file.getParentFile();
                if(!parentFile.exists()) {
                    boolean dirCreateSuccess = parentFile.mkdirs();
                    if(!dirCreateSuccess) {
                        throw new IOException("Unable to create directory path: "+parentFile.getAbsolutePath());
                    }
                }
                
                is = zipFile.getInputStream(entry);
                fos = new FileOutputStream(file);
                
                try {
                    for(int read = is.read(buffer); read > -1; read = is.read(buffer)) {
                        fos.write(buffer, 0, read);
                    }
                    fos.flush();
                } catch(IOException ioe) {
                    throw ioe;
                } finally {
                    fos.close();
                    is.close();
                }
            }
        }
    }
}