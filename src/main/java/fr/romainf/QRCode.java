/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Romain Francez
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package fr.romainf;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.apache.commons.cli.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class QRCode {

    private static final String DEFAULT_IMAGE_FORMAT = "png";
    private static final String DEFAULT_OUTPUT_FILE = "out.png";
    private static final String DEFAULT_WIDTH = "300";
    private static final String DEFAULT_HEIGHT = "300";
    private static final String DEFAULT_PIXEL_COLOUR = "0xFF000000";
    private static final int DEFAULT_MARGIN = 0;

    /**
     * Cannot instantiate this class
     */
    private QRCode() {
    }

    public static void main(String[] args) {
        Options options = buildOptions();

        CommandLineParser parser = new BasicParser();

        try {
            CommandLine cmd = parser.parse(options, args);
            args = cmd.getArgs();

            int l = args.length;
            if (l != 1) {
                System.out.println("Can only encode one datum at a time (" + l + " given)");
                printUsage(options);
                System.exit(1);
            }
            if (cmd.hasOption("help")) {
                printUsage(options);
                System.exit(0);
            }

            String output = cmd.getOptionValue("o", DEFAULT_OUTPUT_FILE);
            int width = Integer.parseInt(cmd.getOptionValue("w", DEFAULT_WIDTH));
            int height = Integer.parseInt(cmd.getOptionValue("h", DEFAULT_HEIGHT));
            String pixelColourText = cmd.getOptionValue("c", DEFAULT_PIXEL_COLOUR);
            if (pixelColourText.startsWith("0x")) {
                pixelColourText = pixelColourText.substring(2);
            }
            int pixelColour = (int)Long.parseLong(pixelColourText, 16);

            writeQRCode(args[l - 1], width, height, output, pixelColour);

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            printUsage(options);
            System.exit(1);
        } catch (WriterException e) {
            System.out.println("Could not create QRCode from data (" + e.getMessage() + ")");
            System.exit(2);
        } catch (IOException e) {
            System.out.println("Could not save QRCode to file (" + e.getMessage() + ")");
            System.exit(4);
        }
        System.exit(0);
    }

    /**
     * Writes data as a QRCode in an image.
     * TODO: too many arguments
     *
     * @param data   String The data to encode in a QRCode
     * @param width  int The width of the final image
     * @param height int The height of the final image
     * @param output String The output filename
     * @param pixelColour int The colour of pixels representing bits
     * @throws WriterException
     * @throws IOException
     */
    private static void writeQRCode(String data, int width, int height, String output, int pixelColour) throws WriterException, IOException {
        Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
        hints.put(EncodeHintType.MARGIN, DEFAULT_MARGIN);

        BitMatrix bitMatrix = new QRCodeWriter().encode(data,
                BarcodeFormat.QR_CODE,
                width,
                height,
                hints);

        File file = new File(output);
        if (file.isDirectory()) {
            file = new File(output + File.separator + DEFAULT_OUTPUT_FILE);
        }
        if (!file.createNewFile() && !file.canWrite()) {
            throw new IOException("Cannot write file " + file);
        }
        ImageIO.write(renderImage(bitMatrix, pixelColour), DEFAULT_IMAGE_FORMAT, file);
    }

    /**
     * Creates the options object required for the program.
     *
     * @return Options
     */
    private static Options buildOptions() {
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("output")
                .isRequired(false).hasArg(true)
                .withDescription("file to write to")
                .withArgName("FILENAME")
                .create("o"));
        options.addOption(OptionBuilder.withLongOpt("width")
                .isRequired(false).hasArg(true)
                .withDescription("image width")
                .withArgName("WIDTH")
                .create("w"));
        options.addOption(OptionBuilder.withLongOpt("height")
                .isRequired(false).hasArg(true)
                .withDescription("image height")
                .withArgName("HEIGHT")
                .create("h"));
        options.addOption(OptionBuilder.withLongOpt("colour")
                .isRequired(false).hasArg(true)
                .withDescription("pixel colour")
                .withArgName("COLOUR")
                .create("c"));
        options.addOption(null, "help", false, "print this message");

        return options;
    }

    /**
     * Prints the help message given the options.
     *
     * @param options Options The options for the command line
     */
    private static void printUsage(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("qrcode [OPTIONS] DATUM", options);
    }

    /**
     * Renders the QRCode data in an image.
     *
     * @param matrix BitMatrix BitMatrix of the encoded QRCode
     * @param pixelColour int The colour of pixels representing bits
     * @return BufferedImage
     */
    public static BufferedImage renderImage(BitMatrix matrix, int pixelColour) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                bufferedImage.setRGB(x, y, matrix.get(x, y) ? pixelColour : 0x00FFFFF);
            }
        }

        return bufferedImage;
    }
}
