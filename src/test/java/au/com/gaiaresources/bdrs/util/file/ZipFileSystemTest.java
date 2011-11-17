package au.com.gaiaresources.bdrs.util.file;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author stephanie
 *
 */
public class ZipFileSystemTest {

    private ZipFileSystem zfs;
    private File jarFile = null;
    private File sourceDir = null;
    private JarFile readOnlyJar = null;
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        // create the temp jar file
        sourceDir = new File(getClass().getResource("themes").getPath());
        jarFile = new File(sourceDir.getParent(), "themes.jar");
        if (!jarFile.exists()) {
            Assert.assertTrue("Error creating jar file.", jarFile.createNewFile());
        }
        JarOutputStream target = new JarOutputStream(new FileOutputStream(jarFile));
        addFilesToJar(sourceDir, target);
        target.flush();
        target.close();
        
        zfs = new ZipFileSystem(jarFile.getAbsolutePath());
        readOnlyJar = new JarFile(jarFile);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        // delete the temp jar file
        //jarFile.deleteOnExit();
        //zfs.close();
        //readOnlyJar.close();
    }

    /**
     * Test method for {@link au.com.gaiaresources.bdrs.util.file.ZipFileSystem#listFiles()}.
     */
    @Test
    public final void testListFiles() {
        List<FileSystem> jarFiles = zfs.listFiles();
        File[] sourceFiles = sourceDir.listFiles();
        Assert.assertEquals("Jar file does not have the correct number of entries.", 
                            sourceFiles.length, jarFiles.size());
        int i = 0;
        for (FileSystem fs : jarFiles) {
            // zip file entries begin with '/'
            String expected = "/" + sourceFiles[i].getName();
            if (fs.isDirectory()) {
                // zip file directories end with '/'
                expected = expected + "/";
            }
            Assert.assertEquals("File paths are not equal", expected, fs.getRelativePath());
            i++;
        }
    }

    /**
     * Test method for {@link au.com.gaiaresources.bdrs.util.file.ZipFileSystem#getAbsolutePath()}.
     */
    @Test
    public final void testGetAbsolutePath() {
        List<FileSystem> jarFiles = zfs.listFiles();
        File[] sourceFiles = sourceDir.listFiles();
        Assert.assertEquals("Jar file does not have the correct number of entries.", 
                            sourceFiles.length, jarFiles.size());
        int i = 0;
        for (FileSystem fs : jarFiles) {
            testAbsolutePaths(sourceFiles[i], fs);
            i++;
        }
    }

    private void testAbsolutePaths(File source, FileSystem target) {
        // create the jar! portion of the path in the source file
        String sourcePath = source.getAbsolutePath();
        sourcePath = sourcePath.replace(sourceDir.getName(), "themes.jar!");
        if (target.isDirectory()) {
            // zip directory entries end in '/'
            sourcePath = sourcePath + "/";
        }
        Assert.assertEquals("File paths are not equal", sourcePath, target.getAbsolutePath());
    }

    /**
     * Test method for {@link au.com.gaiaresources.bdrs.util.file.ZipFileSystem#getChildInputStream(java.lang.String)}.
     * @throws IOException 
     */
    @Test
    public final void testGetChildInputStream() throws IOException {
        InputStream ins = zfs.getChildInputStream("test.vm");
        Assert.assertNull("Requesting child from incorrect place in file system", ins);
        
        List<FileSystem> jarFiles = zfs.listFiles();
        for (FileSystem fs : jarFiles) {
            ins = fs.getChildInputStream("test.vm");
            Assert.assertNotNull("Child should be in this file system", ins);
        }
        
        if (ins != null) {
            // test contents of the input stream
            StringBuilder actual = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
            String line = reader.readLine();
            while (line != null) {
                actual.append(line);
                line = reader.readLine();
            }
            reader.close();
            
            StringBuilder expected = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(sourceDir.getAbsolutePath()+"/test/test.vm"))));
            line = reader.readLine();
            while (line != null) {
                expected.append(line);
                line = reader.readLine();
            }
            reader.close();
            
            Assert.assertEquals("File contents are not equal.", expected.toString(), actual.toString());
        }
    }

    /**
     * Test method for {@link au.com.gaiaresources.bdrs.util.file.ZipFileSystem#getInputStream()}.
     * @throws IOException 
     */
    @Test
    public final void testGetInputStream() throws IOException {
        InputStream ins = zfs.getInputStream();
        Assert.assertNull("Directory shouldn't have input stream", ins);
        
        List<FileSystem> jarFiles = zfs.listFiles();
        for (FileSystem fs : jarFiles) {
            List<FileSystem> textFiles = fs.listFiles();
            for (FileSystem textFS : textFiles) {
                ins = textFS.getInputStream();
                Assert.assertNotNull("This file system should contain a file", ins);
            }
        }
        
        if (ins != null) {
            // test contents of the input stream
            StringBuilder actual = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
            String line = reader.readLine();
            while (line != null) {
                actual.append(line);
                line = reader.readLine();
            }
            reader.close();
            
            StringBuilder expected = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(sourceDir.getAbsolutePath()+"/test/test.vm"))));
            line = reader.readLine();
            while (line != null) {
                expected.append(line);
                line = reader.readLine();
            }
            reader.close();
            
            Assert.assertEquals("File contents are not equal.", expected.toString(), actual.toString());
        }
    }

    /**
     * Test method for {@link au.com.gaiaresources.bdrs.util.file.ZipFileSystem#getName()}.
     */
    @Test
    public final void testGetName() {
        List<FileSystem> jarFiles = zfs.listFiles();
        File[] sourceFiles = sourceDir.listFiles();
        int i = 0;
        for (FileSystem fs : jarFiles) {
            Assert.assertEquals("", sourceFiles[i].getName(), fs.getName());
            i++;
        }
    }

    /**
     * Test method for {@link au.com.gaiaresources.bdrs.util.file.ZipFileSystem#isDirectory()}.
     */
    @Test
    public final void testIsDirectory() {
        Assert.assertTrue("The root of the zip file should be a directory.", zfs.isDirectory());
    }

    private void addFilesToJar(File source, JarOutputStream target) throws IOException
    {
        BufferedInputStream in = null;
        try {
            if (source.isDirectory()) {
                String name = source.getPath().substring(sourceDir.getPath().length()).replace("\\", "/");
                
                if (!name.isEmpty()) {
                    if (!name.endsWith("/"))
                        name += "/";
                    JarEntry entry = new JarEntry(name);
                    entry.setTime(source.lastModified());
                    target.putNextEntry(entry);
                    target.closeEntry();
                }
          
                for (File nestedFile: source.listFiles())
                    addFilesToJar(nestedFile, target);
                return;
            }
        
            JarEntry entry = new JarEntry(source.getPath().substring(sourceDir.getPath().length()).replace("\\", "/"));
            entry.setTime(source.lastModified());
            target.putNextEntry(entry);
            in = new BufferedInputStream(new FileInputStream(source));
        
            byte[] buffer = new byte[1024];
            while (true) {
                int count = in.read(buffer);
                if (count == -1)
                    break;
                target.write(buffer, 0, count);
            }
            target.closeEntry();
        }
        finally {
            if (in != null)
                in.close();
        }
    }
}
