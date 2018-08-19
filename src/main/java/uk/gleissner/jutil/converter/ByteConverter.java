package uk.gleissner.jutil.converter;

import com.google.protobuf.Message;

import static java.lang.String.format;
import static javax.xml.bind.DatatypeConverter.printHexBinary;

public class ByteConverter {

    public static String toHex(Message msg) {
        return toHex(msg.toByteArray());
    }

    public static String toHex(byte[] bytes) {
        if (bytes == null) {
            return "null";
        } else if (bytes.length == 0) {
            return "0 bytes";
        } else {
            String hex = printHexBinary(bytes);
            StringBuilder sb = new StringBuilder();
            sb.append(format("%3d byte(s): ", bytes.length));
            for (int i = 0; i < hex.length(); i += 2) {
                if (i > 0) {
                    sb.append(" ");
                }
                sb.append(hex, i, i + 2);
            }
            return sb.toString();
        }
    }
}
