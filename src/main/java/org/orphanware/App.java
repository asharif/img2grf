package org.orphanware;

import com.sun.org.apache.bcel.internal.util.ByteSequence;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
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

			convertBMP(filePath, withLB, invertPixels, outputFileName);
			System.exit(0);

		}



	}

	public static void convertBMP(String filePath, Boolean withLB, 
				       Boolean invertPixels, String outputFileName) {

		FileInputStream fis;
		try {
			fis = new FileInputStream(filePath);
			BufferedImage img = ImageIO.read(new File(filePath));
			int h = img.getHeight();
			int w = img.getWidth();
			byte[] origBytes = readFully(fis);
			System.out.println("height: " + h + " width: " + w + " total byte length: " + origBytes.length);

			int pixeloffset = origBytes[10] + origBytes[11] + origBytes[12] + origBytes[13];
			if (pixeloffset == 62) {

				System.out.println("pixel offset: " + pixeloffset);

			} else {

				System.out.println("pixel offset (WARNING! NOT THE DEFAULT OF 62): " + pixeloffset);

			}



			byte[] withoutHeaderBytes = new byte[origBytes.length - pixeloffset];

			int newByteIndex = 0;
			int byteW =  (int) Math.ceil(((double)w) / 8);
			
			for (int i = origBytes.length-1; i >= pixeloffset; i--) {

				int tmp = i - (byteW-1);
				
				for( int j = tmp; j < tmp + byteW; j++ ) {

					withoutHeaderBytes[newByteIndex++] = origBytes[j];

				}
				i = tmp;
			}

			if ( invertPixels ) {
				
				System.out.println("pixels inverted!");
				for (int i = 0; i < withoutHeaderBytes.length; i++) {
					withoutHeaderBytes[i] ^= 0xFF;
				}
			}

			String byteAsString = Hex.encodeHexString(withoutHeaderBytes);

			if (withLB) {
				
				char[] bytesAsCharArr = byteAsString.toCharArray();

				int lineBreakCount = (int) Math.ceil(((double)w) / 4);

				System.out.println("Adding line break every: " + lineBreakCount + " bytes");
				StringBuilder lineBreakedStr = new StringBuilder();
				for (int i = 0; i < bytesAsCharArr.length; i++) {

					if (i % lineBreakCount == 0) {

						lineBreakedStr.append("\n");
					}

					lineBreakedStr.append(bytesAsCharArr[i]);

				}

				byteAsString = lineBreakedStr.toString();
			}

			int wInBytes = (int) Math.ceil(((double)w) / 8);
			String imageTemplate = "~DG" + outputFileName + "," + withoutHeaderBytes.length;
			imageTemplate       += "," + wInBytes + "," + byteAsString;
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

	public static byte[] readFully(InputStream stream) throws IOException {
		byte[] buffer = new byte[8192];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		int bytesRead;
		while ((bytesRead = stream.read(buffer)) != -1) {
			baos.write(buffer, 0, bytesRead);
		}
		return baos.toByteArray();
	}

	public static void printHelp() {

		System.out.println("\nZebra BMP to GRF encoder.");
		System.out.println("Written by Arash Sharif");
		System.out.println("Released under MIT license @ http://opensource.org/licenses/MIT");
		System.out.println("-----------------------------------------------------------------------------------------");
		System.out.println("Basic Use: java -jar img2grf.jar -f {path to file}");
		System.out.println("-----------------------------------------------------------------------------------------");
		System.out.println("switches:\n");
		System.out.println("required:");
		System.out.println("-f \t-must be followed with path to the bmp image you want to encode");
		System.out.println("optional:");
		System.out.println("-lb\t-tells encoder to insert line break at widths.  helps reading eye with naked eye.");
		System.out.println("-i \t-tells encoder to invert pixels");
		System.out.println("-o \t-must be followed by the name of the grf file (WITHOUT EXTENTION!). Used for encoding!");
		System.out.println("-----------------------------------------------------------------------------------------");
		System.out.println("Source found @ https://github.com/asharif/img2grf");
		System.out.println("-----------------------------------------------------------------------------------------");
	}
}
