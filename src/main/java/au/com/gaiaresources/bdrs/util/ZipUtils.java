package au.com.gaiaresources.bdrs.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

public class ZipUtils {
    public static final Logger log = Logger.getLogger(ZipUtils.class);

    /**
     * Convenience method to decompress files. No need to create ZipFile object.
     * Will throw exceptions if file is not a valid zip file.
     * 
     * @param fileToDecompress
     * @param targetDir
     * @throws IOException 
     * @throws ZipException 
     */
    public static void decompressToDir(File fileToDecompress, File targetDir) throws ZipException, IOException {
        ZipFile zipFile = new ZipFile(fileToDecompress);
        decompressToDir(zipFile, targetDir);
    }
    
    /**
     * Unzips the zip file to the specified directory.
     * 
     * @param zipFile
     *            the file to be decompressed.
     * @param dir
     *            the location where decompressed files shall be saved.
     * @throws IOException
     *             thrown if there has been a problem reading data from the zip
     *             file or writing data to the destination.
     */
    public static void decompressToDir(ZipFile zipFile, File dir)
            throws IOException {

        byte[] buffer = new byte[4096];

        File file;
        InputStream is;
        FileOutputStream fos;
        log.info("Zip File: " + zipFile.getName());
        Enumeration<? extends ZipEntry> zipFileEntries = zipFile.entries();
        ZipEntry entry;
        while (zipFileEntries.hasMoreElements()) {
            entry = zipFileEntries.nextElement();
            log.info("Zip Entry: " + entry.getName());
            file = new File(dir, entry.getName());
            if (entry.isDirectory()) {
                boolean dirCreateSuccess = file.mkdirs();
                if (!dirCreateSuccess) {
                    throw new IOException("Unable to create directory path: "
                            + file.getAbsolutePath());
                }
            } else {
                File parentFile = file.getParentFile();
                if (!parentFile.exists()) {
                    boolean dirCreateSuccess = parentFile.mkdirs();
                    if (!dirCreateSuccess) {
                        throw new IOException(
                                "Unable to create directory path: "
                                        + parentFile.getAbsolutePath());
                    }
                }

                is = zipFile.getInputStream(entry);
                fos = new FileOutputStream(file);

                try {
                    for (int read = is.read(buffer); read > -1; read = is.read(buffer)) {
                        fos.write(buffer, 0, read);
                    }
                    fos.flush();
                } catch (IOException ioe) {
                    throw ioe;
                } finally {
                    fos.close();
                    is.close();
                }
            }
        }
    }

    static final int BUFFER = 2048;

    public static void compress(List<File> inFiles, File outFile) throws IOException {
        // convert the list to an array
        File[] files = new File[inFiles.size()];
        files = inFiles.toArray(files);
        compress(files, outFile);
    }
    
    public static void compress(File[] inFiles, File outFile) throws IOException {
        FileOutputStream dest = null;
        try {
            dest = new FileOutputStream(outFile);

            compressToStream(inFiles, dest);
            dest.close();
        } catch (Exception e) {
            log.error("Error zipping files", e);
        } finally {
            if (dest != null) {
                dest.close();
            }
        }
    }
    
    public static void compressToStream(File[] inFiles, OutputStream dest) throws IOException {
        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(new BufferedOutputStream(dest));

            compressFiles(inFiles, out, "");
            out.close();
        } catch (Exception e) {
            log.error("Error zipping files", e);
        } finally {
            if (dest != null) {
                dest.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    private static void compressFiles(File[] inFiles, ZipOutputStream out, String parent) throws IOException {
        byte data[] = new byte[BUFFER];

        for (File f : inFiles) {
            log.info("Zipping: " + f.getAbsolutePath());

            if (f.isDirectory()) {
                compressFiles(f.listFiles(), out, parent + 
                              (!StringUtils.nullOrEmpty(parent) ? "/" : "") + f.getName());
            } else {
                FileInputStream fi = null;
                BufferedInputStream origin = null;
                try {
                    fi = new FileInputStream(f);
                    origin =  new BufferedInputStream(fi, BUFFER);
                    ZipEntry entry = new ZipEntry(parent + 
                                                  (!StringUtils.nullOrEmpty(parent) ? "/" : "") + f.getName());
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                } finally {
                    if (fi != null) {
                        fi.close();
                    }
                    if (origin != null) {
                        origin.close();
                    }
                }
            }
        }
    }
}