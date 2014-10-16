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
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.commons.cli.*;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

public class QRCode {

    private static final String DEFAULT_OUTPUT_FILE = "out.svg";
    private static final int DEFAULT_WIDTH = 0;
    private static final int DEFAULT_HEIGHT = 0;
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
            String pixelColourText = cmd.getOptionValue("c", DEFAULT_PIXEL_COLOUR);
            if (pixelColourText.startsWith("0x")) {
                pixelColourText = pixelColourText.substring(2);
            }
            Color pixelColour;
            if (pixelColourText.length() == 6) {
                pixelColour = new Color(Integer.parseInt(pixelColourText, 16));
            } else {
                pixelColour = new Color((int) Long.parseLong(pixelColourText, 16), true);
            }

            writeQRCode(args[l - 1], output, pixelColour);

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
     *
     * @param data        String The data to encode in a QRCode
     * @param output      String The output filename
     * @param pixelColour Color The colour of pixels representing bits
     * @throws WriterException
     * @throws IOException
     */
    private static void writeQRCode(String data, String output, Color pixelColour) throws WriterException, IOException {
        Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
        hints.put(EncodeHintType.MARGIN, DEFAULT_MARGIN);

        BitMatrix bitMatrix = new QRCodeWriter().encode(data,
                BarcodeFormat.QR_CODE,
                DEFAULT_WIDTH,
                DEFAULT_HEIGHT,
                hints);

        File file = new File(output);
        if (file.isDirectory()) {
            file = new File(output + File.separator + DEFAULT_OUTPUT_FILE);
        }
        if (!file.createNewFile() && !file.canWrite()) {
            throw new IOException("Cannot write file " + file);
        }

        SVGGraphics2D graphics2D = renderSVG(bitMatrix, pixelColour);
        graphics2D.stream(new FileWriter(file), true);
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
        options.addOption(OptionBuilder.withLongOpt("colour")
                .isRequired(false).hasArg(true)
                .withDescription("pixel colour")
                .withArgName("COLOUR")
                .create("c"));
        options.addOption("h", "help", false, "print this message");

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
     * Renders the QRCode data in an SVG document.
     *
     * @param matrix      BitMatrix BitMatrix of the encoded QRCode
     * @param pixelColour Color The colour of pixels representing bits
     * @return SVGGraphics2D
     */
    private static SVGGraphics2D renderSVG(BitMatrix matrix, Color pixelColour) {
        DOMImplementation implementation = SVGDOMImplementation.getDOMImplementation();
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        Document document = implementation.createDocument(svgNS, "svg", null);

        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

        int width = matrix.getWidth();
        int height = matrix.getHeight();

        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                if (matrix.get(x, y)) {
                    svgGenerator.setPaint(pixelColour);
                    svgGenerator.fill(new Rectangle(x, y, 1, 1));
                }
            }
        }
        return svgGenerator;
    }
}
