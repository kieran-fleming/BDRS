package au.com.gaiaresources.bdrs.util.file;


/**
 * Contains a reference to the path of the root of the file system and methods 
 * shared by all types of file systems.
 * @author stephanie
 */
public abstract class AbstractFileSystem implements FileSystem {
    /**
     * The path of the root of the file system.
     */
    protected String fileSystemRoot;
    
    /**
     * @param rootURL The URL to the root of the file system.
     */
    public AbstractFileSystem(String rootURL) {
        fileSystemRoot = rootURL;
    }

    @Override
    public String getExtension() {
        String name = getName();
        int index = name.indexOf('.');
        if (index > 0) {
            return name.substring(index + 1);
        }
        return "";
    }
}
