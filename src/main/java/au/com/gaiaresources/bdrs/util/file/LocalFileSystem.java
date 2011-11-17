package au.com.gaiaresources.bdrs.util.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements access to the local file system via the FileSystem interface.
 * 
 * @author stephanie
 */
public class LocalFileSystem extends AbstractFileSystem implements FileSystem {
    /**
     * A reference to the {@link File} at the root of the file system.
     */
    private File rootFile;
    
    /**
     * Creates a LocalFileSystem at the location given by rootURL
     * @param rootURL The location of the root of this FileSystem
     */
    public LocalFileSystem(String rootURL) {
        super(rootURL);
        rootFile = new File(rootURL);
    }

    /**
     * Creates a LocalFileSystem at the location given by the rootDir {@link File} object.
     * @param rootDir The {@link File} at the root of the file system.
     */
    private LocalFileSystem(File rootDir) {
        super(rootDir.getAbsolutePath());
        rootFile = rootDir;
    }

    @Override
    public List<FileSystem> listFiles() {
        List<FileSystem> fileList = new ArrayList<FileSystem>();
        for (File file : rootFile.listFiles()) {
            fileList.add(new LocalFileSystem(file.getAbsolutePath()));
        }
        return fileList;
    }

    @Override
    public String getName() {
        return rootFile.getName();
    }

    @Override
    public InputStream getChildInputStream(String child) throws IOException {
        return new FileInputStream(new File(rootFile, child));
    }

    @Override
    public boolean isDirectory() {
        return rootFile.isDirectory();
    }

    @Override
    public String getAbsolutePath() {
        return rootFile.getAbsolutePath();
    }

    @Override
    public String getRelativePath() {
        return rootFile.getName();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(rootFile);
    }

    @Override
    public void close() throws IOException {
        // nothing to close for local file system.
    }

    @Override
    public long length() {
        return rootFile.length();
    }
}
