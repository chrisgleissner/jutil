package uk.gleissner.jutil.converter;

import com.google.protobuf.Message;

import static java.lang.String.format;

public class ByteConverter {

    private final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String toHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_ARRAY[b >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[b & 0x0F];
        }
        return new String(hexChars);
    }

    public static String toSpacedHex(Message msg) {
        return toSpacedHex(msg.toByteArray());
    }

    public static String toSpacedHex(byte[] bytes) {
        if (bytes == null) {
            return "null";
        } else if (bytes.length == 0) {
            return "0 bytes";
        } else {
            String hex = toHex(bytes);
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
