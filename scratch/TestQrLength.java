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

public class TestQrLength {
    private static final int QR_SIZE = 400;

    public static void main(String[] args) throws Exception {
        String text = "http://localhost:8080/join?pin=ABC123";
        QRCodeWriter writer = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = Map.of(
                EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H,
                EncodeHintType.MARGIN, 2
        );
        BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE, hints);
        BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", outputStream);
        String base64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());
        String dataUri = "data:image/png;base64," + base64;
        System.out.println("Data URI length: " + dataUri.length());
    }
}
