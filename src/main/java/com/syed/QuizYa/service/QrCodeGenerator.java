package com.syed.QuizYa.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * UTILITY: QrCodeGenerator
 *
 * Generates a QR code for any given text (e.g. a join URL) and returns it
 * as a Base64-encoded data URI that can be embedded directly in an <img> tag:
 *
 *   <img src="data:image/png;base64,iVBORw0KGgo...">
 *
 * This avoids any external hosting — the QR code is generated entirely in Java
 * and stored as a string in the events.qr_code_url column.
 *
 * ZXing (pronounced "crossing") is Google's open-source barcode/QR library.
 */
public class QrCodeGenerator {

    private static final int QR_SIZE = 400; // pixels — large enough to scan across a room

    /**
     * Generates a QR code PNG encoded as a data URI.
     *
     * @param text  The content to encode (e.g. "http://localhost:8080/join?pin=ABC123")
     * @return      A string like "data:image/png;base64,..." ready for use in an <img src>
     * @throws WriterException if the QR code cannot be encoded
     * @throws IOException     if the image cannot be written to bytes
     */
    public static String generateDataUri(String text) throws WriterException, IOException {
        QRCodeWriter writer = new QRCodeWriter();

        // Hint map tells ZXing to use high error-correction (recovers even if QR is partially obscured)
        Map<EncodeHintType, Object> hints = Map.of(
                EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H,
                EncodeHintType.MARGIN, 2
        );

        // BitMatrix is the raw 2D grid of black/white cells
        BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE, hints);

        // MatrixToImageWriter converts the BitMatrix into a BufferedImage
        BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

        // Turns the QR code image into a very long line of text
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", outputStream);

        // Base64-encode and wrap in a data URI
        String base64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());
        return "data:image/png;base64," + base64;
    }
}
