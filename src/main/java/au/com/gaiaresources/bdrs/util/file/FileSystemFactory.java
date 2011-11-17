package au.com.gaiaresources.bdrs.util.file;

/**
 * Factory class for creating an appropriate type of file system based on the URL
 * passed.
 * 
 * @author stephanie
 */
public class FileSystemFactory {

    /**
     * Gets an instance of a {@link FileSystem} based on the rootURL parameter passed.
     * @param rootURL The path of the {@link FileSystem}.
     * @return An instance of {@link ZipFileSystem} will be returned if rootUrl contains "jar!", 
     * otherwise an instance of {@link LocalFileSystem} will be returned.
     */
    public static FileSystem getFileSystem(String rootURL) {
        // TODO: add handling for zip files
        // currently only handles jar files
        if (rootURL.contains("jar!")) {
            return new ZipFileSystem(rootURL);
        } else {
            return new LocalFileSystem(rootURL);
        }
    }
}
