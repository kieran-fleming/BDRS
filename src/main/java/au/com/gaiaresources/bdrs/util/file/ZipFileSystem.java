package au.com.gaiaresources.bdrs.util.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;

/**
 * Implements access to a zip/jar file as a file system via the {@link FileSystem} interface.
 * 
 * @author stephanie
 */
public class ZipFileSystem extends AbstractFileSystem implements FileSystem {
    private static final String ZIP_ENTRY_REGEX = "\\/?\\w+((\\.?\\w+)|(\\/))";
    
    /**
     * The zip file that represents the file system to read from.
     */
    private ZipFile rootFile;
    /**
     * The current entry in the file system
     */
    private ZipEntry thisEntry;
    /**
     * Pattern for matching first level directory/file entries in the zip.
     */
    private String pattern = null;
    private Logger log = Logger.getLogger(getClass());
    
    /**
     * Constructs a ZipFileSystem from the given path to a resource in a jar file.
     * @param rootURL The URL for a resource in a jar file to represent as a FileSystem.
     */
    public ZipFileSystem(String rootURL) {
        super(rootURL);
        try {
            int index = rootURL.indexOf("!");
            // get the path to the zip file from the URL
            // the URL will start with 'file:' but if it is a path String instead, it will not start with 'file:'
            // the URL should also contain a '!' that denotes where the end of the jar path is
            // create the zip path from the substring between the 'file:' and '!' characters
            String zipPath = rootURL.substring(rootURL.startsWith("file:") ? 5 : 0, 
                                               index == -1 ? rootURL.length() : index);
            rootFile = new ZipFile(zipPath);
            // the path to the root of the file system for a jar file is 
            // the relative path to the root of the jar
            // construct this from a substring of the url after the '!'
            // the first character after the '!' is a '/', which we also want to remove
            fileSystemRoot = index == -1 ? "" : rootURL.substring(index+2);
            // construct the pattern to match only the direct children of this location
            pattern = fileSystemRoot + ZIP_ENTRY_REGEX;
        } catch (IOException e) {
            log.error("Error creating zip file from "+rootURL, e);
            throw new Error("Error creating zip file from "+rootURL, e);
        }
    }

    /**
     * Create a ZipFileSystem with rootFile as the root zip file and entry as the 
     * current entry of the file system.
     * @param rootFile The zip file to read from
     * @param entry The current entry of the file system
     */
    private ZipFileSystem(ZipFile rootFile, ZipEntry entry) {
        super(entry.getName());
        this.rootFile = rootFile;
        this.thisEntry = entry;
        pattern = fileSystemRoot + ZIP_ENTRY_REGEX;
    }

    @Override
    public List<FileSystem> listFiles() {
        List<FileSystem> fileList = new ArrayList<FileSystem>();
        
        Enumeration<? extends ZipEntry> zipFileEntries = rootFile.entries();
        ZipEntry entry;
        while (zipFileEntries.hasMoreElements()) {
            entry = zipFileEntries.nextElement();
            // only retrieve first level children to match File behavior
            if (entry.getName().matches(pattern)) {
                fileList.add(new ZipFileSystem(rootFile, entry));
            }
        }
        return fileList;
    }

    @Override
    public String getAbsolutePath() {
        return rootFile.getName() + "!" + fileSystemRoot;
    }

    @Override
    public InputStream getChildInputStream(String child) throws IOException {
        String absChildPath = fileSystemRoot + child;
        ZipEntry childEntry = rootFile.getEntry(absChildPath);
        if (childEntry != null) {
            return rootFile.getInputStream(childEntry);
        }
        return null;
    }
    
    @Override
    public InputStream getInputStream() throws IOException {
        if (thisEntry == null) {
            return null;
        }
        return rootFile.getInputStream(thisEntry);
    }

    @Override
    public String getName() {
        if (thisEntry == null) {
            return rootFile.getName();
        }
        
        String name = thisEntry.getName();
        // trim the trailing '/' from directory names
        if (name.endsWith("/")) {
            name = name.substring(0, thisEntry.getName().length()-1);
        }
        // get the last part of the path name
        return name.substring(name.lastIndexOf("/")+1);
    }

    @Override
    public String getRelativePath() {
        return thisEntry == null ? "" : thisEntry.getName();
    }

    @Override
    public boolean isDirectory() {
        return thisEntry == null || thisEntry.isDirectory();
    }

    @Override
    public void close() throws IOException {
        rootFile.close();
    }

    @Override
    public long length() {
        return thisEntry.getSize();
    }
}
