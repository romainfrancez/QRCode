/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Romain Francez

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
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

            int l = args.length;
            if (l <= 0 || cmd.hasOption("help")) {
                printUsage(options);
                return;
            }

            String output = cmd.getOptionValue("o", DEFAULT_OUTPUT_FILE);
            int width = Integer.parseInt(cmd.getOptionValue("w", DEFAULT_WIDTH));
            int height = Integer.parseInt(cmd.getOptionValue("h", DEFAULT_HEIGHT));

            writeQRCode(args[l - 1], width, height, output);

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            printUsage(options);
        } catch (WriterException e) {
            System.out.println("Could not create QRCode from data (" + e.getMessage() + ")");
        } catch (IOException e) {
            System.out.println("Could not save QRCode to file (" + e.getMessage() + ")");
        }
    }

    /**
     * Writes data as a QRCode in an image.
     *
     * @param data   String The data to encode in a QRCode
     * @param width  int The width of the final image
     * @param height int The height of the final image
     * @param output String The output filename
     * @throws WriterException
     * @throws IOException
     */
    private static void writeQRCode(String data, int width, int height, String output) throws WriterException, IOException {
        Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
        hints.put(EncodeHintType.MARGIN, DEFAULT_MARGIN);

        BitMatrix bitMatrix = new QRCodeWriter().encode(data,
                BarcodeFormat.QR_CODE,
                width,
                height,
                hints);

        File file = new File(output);
        if (!file.canWrite()) {
            throw new IOException("Cannot write file " + file);
        }
        ImageIO.write(renderImage(bitMatrix), DEFAULT_IMAGE_FORMAT, file);
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
        helpFormatter.printHelp("qrcode [OPTIONS] DATA", options);
    }

    /**
     * Renders the QRCode data in an image.
     *
     * @param matrix BitMatrix BitMatrix of the encoded QRCode
     * @return BufferedImage
     */
    public static BufferedImage renderImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);

        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                bufferedImage.setRGB(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFF);
            }
        }

        return bufferedImage;
    }
}
