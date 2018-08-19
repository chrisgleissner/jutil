/*
 * Copyright (C) 2018 Christian Gleissner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.gleissner.jutil.converter;

import com.google.protobuf.Message;

import static java.lang.String.format;

public class ByteConverter {

    private final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String toHex(byte[] bytes) {
        char[] hex = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i] & 0xFF;
            hex[i * 2] = HEX_ARRAY[b >>> 4];
            hex[i * 2 + 1] = HEX_ARRAY[b & 0x0F];
        }
        return new String(hex);
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
            StringBuilder sb = new StringBuilder(format("%3d byte(s): ", bytes.length));
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
