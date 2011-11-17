package au.com.gaiaresources.bdrs.util.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * An interface representing a file system for accessing zip/jar files as you would
 * a file system.
 * 
 * @author stephanie
 */
public interface FileSystem {

    /**
     * Lists the contents of this file system as FileSystem objects
     * @return
     */
    List<FileSystem> listFiles();

    /**
     * Gets the short name of the file system.
     * @return
     */
    String getName();

    /**
     * Gets an InputStream for the resource with name child in the file system.
     * @param child The name of the child to read
     * @return An InputStream for the resource or null if the child doesn't exist
     * @throws IOException
     */
    InputStream getChildInputStream(String child) throws IOException;

    /**
     * Checks if the root of the file system is a directory.
     * @return True if the root is a directory, false otherwise.
     */
    boolean isDirectory();

    /**
     * Gets the absolute path of the file system root.
     * @return The absolute path of the file system root.
     */
    String getAbsolutePath();

    /**
     * Gets the relative path of the file system.
     * @return The relative path of the file system.
     */
    String getRelativePath();

    /**
     * Gets an InputStream for the root of the file system.
     * @return An InputStream for the root of the file system or null if the object cannot be opened for reading.
     * @throws IOException 
     */
    InputStream getInputStream() throws IOException;

    /**
     * Closes the file system.
     * @throws IOException
     */
    void close() throws IOException;

    /**
     * Get the length in bytes of the file system
     * @return
     */
    long length();

    /**
     * Gets the extension of the file or an empty string if none.
     * @return The file extension for the FileSystem location
     */
    String getExtension();

}
