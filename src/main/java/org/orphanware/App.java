package org.orphanware;

import com.sun.org.apache.bcel.internal.util.ByteSequence;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;
import org.apache.commons.codec.binary.Hex;

/**
 * Hello world!
 *
 */
public class App {

	public static void main(String[] args) {


		if (args.length == 0) {

			printHelp();
			System.exit(0);
		}

		int argIndex = 0;
		String filePath = null;
		Boolean withLB = false;
		Boolean invertPixels = false;
		String outputFileName = "image";
		
		for (String arg : args) {

			if (arg.equals("-help")) {

				printHelp();
				System.exit(0);

			}

			if (arg.equals("-f")) {

				try {
					filePath = args[argIndex + 1];
				} catch (Exception e) {

					System.out.println("-f switch found but was not followed by file path");
					System.exit(1);
				}



			}

			if (arg.equals(("-lb"))) {

				withLB = true;
			}
			
			if (arg.equals(("-i"))) {

				invertPixels = true;
			}
			
			if (arg.equals(("-o"))) {

				try {
					outputFileName = args[argIndex + 1];
				} catch (Exception e) {

					System.out.println("-o switch found but was not followed by image name");
					System.exit(1);
				}
			}

			argIndex++;
		}

		if (filePath != null) {

			convert(filePath, withLB, invertPixels, outputFileName);
			System.exit(1);

		}
                
                
                System.out.println("error!  input file not found!");


	}

	public static void convert(String filePath, Boolean withLB, 
				       Boolean invertPixels, String outputFileName) {

		FileImageInputStream fis;
                BufferedImage img;
		try {
                    
                        
			fis = new FileImageInputStream(new File(filePath));

                        
                        Iterator<ImageReader> readers = ImageIO.getImageReaders(fis);
                        
                        if(!readers.hasNext()) {
                            
                            System.out.println("Sorry file type not recognised.  Try another file type like PNG");
                            System.exit(1);
                            
                        }
                        
                        ImageReader reader = readers.next();
                        
                        System.out.println("image type is :" + reader.getFormatName());
                        reader.setInput(fis);
                        img = reader.read(0);
                        
                        int w = img.getWidth();
                        int h = img.getHeight();
                        
                        System.out.println("image width is: " + w);
                        System.out.println("image height is: " + h);
                        
                        char darkChar = '1';
                        char lightChar = '0';
                        
                        if( invertPixels ) {
                            
                            darkChar = '0';
                            lightChar = '1';
                        }
                        
                        StringBuilder out = new StringBuilder();
                        
                        
                        
                        for( int y = 0; y < h; ++y ) {
                            
                            for( int x = 0; x < w; ++x ) {
                                
                                int argb = img.getRGB(x, y);
                                
                                int r = (argb >> 16) & 0xFF;
                                int g = (argb >> 8)  & 0xFF;
                                int b = argb & 0xFF; 
                                
                                float darkness = (r + g + b) / 3;
                                
                                
                                if( Float.valueOf(darkness).compareTo(127f) > 0 ) {
                                    
                                    out.append(lightChar);
                                } else {
                                    
                                    out.append(darkChar);
                                }
                                
                                
                            }
                            
                            if( withLB ) {
                                
                                out.append("\n");
                            }
                            
                        }
                        
                        String outStr = out.toString();

			String imageTemplate = "~DG" + outputFileName + "," + outStr.toCharArray().length;
			imageTemplate       += "," + w + "," + outStr;
			FileOutputStream fos = new FileOutputStream(outputFileName + ".grf");
			fos.write(imageTemplate.getBytes());
			fos.close();

			System.out.println("Finished!  Check for file \"" + outputFileName + ".grf\" in executing dir");
                        
                        
			

		} catch (FileNotFoundException ex) {
			System.out.println("Error.  No file found at path: " + filePath);
			System.exit(1);
		} catch (IOException ex) {
			System.out.println("Error.  No file found at path: " + filePath);
			System.exit(1);
		}
	}

	
	public static void printHelp() {

                System.out.println("\nimg2grf v2.0.0 ");
		System.out.println("Zebra Image to GRF encoder. (supported file types: png, gif, bmp, jpeg) ");
		System.out.println("Written by Arash Sharif");
		System.out.println("Released under MIT license @ http://opensource.org/licenses/MIT");
		System.out.println("-----------------------------------------------------------------------------------------");
		System.out.println("Basic Use: java -jar img2grf.jar -f {path to file}");
		System.out.println("-----------------------------------------------------------------------------------------");
		System.out.println("switches:\n");
		System.out.println("required:");
		System.out.println("\n-f \t-must be followed with path to the bmp image you want to encode");
		System.out.println("\noptional:");
		System.out.println("\n-lb\t-tells encoder to insert line break at widths.  helps reading GRF ASCII with the naked eye");
		System.out.println("-i \t-tells encoder to invert pixels");
		System.out.println("-o \t-must be followed by the name of the GRF file (WITHOUT EXTENTION!). Used for encoding!  If left blank the name \"image\" will be used");
		System.out.println("-----------------------------------------------------------------------------------------");
		System.out.println("Source found @ https://github.com/asharif/img2grf");
		System.out.println("-----------------------------------------------------------------------------------------");
	}
}
