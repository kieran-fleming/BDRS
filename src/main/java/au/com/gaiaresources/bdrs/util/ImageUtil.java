package au.com.gaiaresources.bdrs.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

import org.apache.log4j.Logger;


public class ImageUtil {

    // Strange mime type set by IE8 when uploading certain jpeg images. treat as image/jpeg
    private static final String IE8_JPEG_MIMETYPE = "image/pjpeg";
    private static final String JPEG_MIMETYPE = "image/jpeg";
    
    private static Logger log = Logger.getLogger(ImageUtil.class);
    
    public static BufferedImage resizeImage(InputStream inputStream, Integer width, Integer height) throws IOException {
        // Resize the image as required to fit the space
        BufferedImage sourceImage = ImageIO.read(inputStream);
        
        if (width != null && height != null) {
            BufferedImage scaledImage = new BufferedImage(width.intValue(), height.intValue(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2_scaled = scaledImage.createGraphics();
            // Better scaling
            g2_scaled.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    
            g2_scaled.setBackground(new Color(255,255,255,0));
            g2_scaled.clearRect(0,0,width.intValue(),height.intValue());
    
            int origWidth = sourceImage.getWidth();
            int origHeight = sourceImage.getHeight();
            
            double widthRatio = (double)width / (double) origWidth;
            double heightRatio = (double)height / (double) origHeight;
            
            if (heightRatio > widthRatio) {
                int scaledHeight = (int) Math.round(widthRatio * origHeight);
                g2_scaled.drawImage(sourceImage, 0, (scaledImage.getHeight() - scaledHeight) / 2, scaledImage.getWidth(), scaledHeight, g2_scaled.getBackground(), null);
            } else {
                int scaledWidth = (int) Math.round(heightRatio * origWidth);
                g2_scaled.drawImage(sourceImage, (scaledImage.getWidth() - scaledWidth) / 2, 0, scaledWidth, scaledImage.getHeight(), g2_scaled.getBackground(), null);
            }
            return scaledImage;
        } else if (width != null && height == null) {
            int origWidth = sourceImage.getWidth();
            int origHeight = sourceImage.getHeight();
            
            double widthRatio = (double)width / (double) origWidth;
            int scaledHeight = (int) Math.round(widthRatio * origHeight);
            
            BufferedImage scaledImage = new BufferedImage(width.intValue(), scaledHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2_scaled = scaledImage.createGraphics();
            
            // no need to set background. The size is exact.
            //g2_scaled.setBackground(Color.WHITE);
            //g2_scaled.clearRect(0,0,width.intValue(),height.intValue());
            
            g2_scaled.drawImage(sourceImage, 0, (scaledImage.getHeight() - scaledHeight) / 2, scaledImage.getWidth(), scaledHeight, g2_scaled.getBackground(), null);
            
            // Better scaling
            g2_scaled.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2_scaled.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          
            return scaledImage;
        } else if (width == null && height != null ) {
            throw new IllegalArgumentException("not supported");
        } else {
            // width == null and height == null
            throw new IllegalArgumentException("not supported");
        }
    }
    
    public static void saveImage(File targetFile, BufferedImage image, String mimeType) throws FileNotFoundException, IOException {
        saveImage(targetFile, image, mimeType, 100);
    }
    
    public static void saveImage(File targetFile, BufferedImage image, String mimeType, int quality) throws FileNotFoundException, IOException {
        
        // treat the IE8 exception case as a standard jpeg image
        if (IE8_JPEG_MIMETYPE.equals(mimeType.toLowerCase())) {
            mimeType = JPEG_MIMETYPE;
        }
        
        FileImageOutputStream out = null;
        try {
            out = new FileImageOutputStream(targetFile);
            ImageWriter writer = ImageIO.getImageWritersByMIMEType(mimeType).next();
            
            // tried this but it doesn't seem to do anything...
            //ImageWriteParam iwp = writer.getDefaultWriteParam();
            //iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            //iwp.setCompressionQuality(1);   // an integer between 0 and 1
            // 1 specifies minimum compression and maximum quality
            
            writer.setOutput(out);
            writer.write(image);
            out.flush();
        } catch (FileNotFoundException e) {
            log.error("Error saving image", e);
            throw e;
        } catch (IOException e) {
            log.error("Error saving image", e);
            throw e;
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
    
    public static byte[] fileToByteArray(File file) throws IOException {
        if (file.length() > Integer.MAX_VALUE) {
            throw new IOException("File too big, cannot put into byte array");
        }
        byte[] target = new byte[(int)file.length()];
        
        FileInputStream inputStream = new FileInputStream(file);
        try {
            int readLength = inputStream.read(target, 0, (int)file.length());
            if (readLength != (int)file.length()) {
                throw new IOException("Did not read entire file");
            }
        } finally {
            inputStream.close();
        }
        return target;
    }
    
    public static boolean isMimetypeSupported(String mimeType) {
        return (ImageIO.getImageReadersByMIMEType(mimeType).hasNext() || IE8_JPEG_MIMETYPE.equals(mimeType.toLowerCase()));
    }
}
