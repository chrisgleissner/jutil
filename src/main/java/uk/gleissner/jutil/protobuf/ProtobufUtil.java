package uk.gleissner.jutil.protobuf;

import com.google.protobuf.Message;

import javax.xml.bind.DatatypeConverter;

import static java.lang.String.format;

public class ProtobufUtil {

    public static String toHex(Message message) {
        byte[] bytes = message.toByteArray();
        String hex = DatatypeConverter.printHexBinary(bytes);
        StringBuilder sb = new StringBuilder();
        sb.append(format("%3d byte(s): ", bytes.length));
        for (int i = 0; i < hex.length(); i+= 2) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(hex.substring(i, i + 2));
        }
        return sb.toString();
    }
}
