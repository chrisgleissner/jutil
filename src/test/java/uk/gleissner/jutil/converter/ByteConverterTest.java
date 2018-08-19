package uk.gleissner.jutil.converter;

import org.junit.Test;

import static org.junit.Assert.*;

public class ByteConverterTest {

    @Test
    public void toHex() {
        assertEquals("  3 byte(s): 01 02 03", ByteConverter.toHex(new byte[] { 1, 2, 3}));
    }

    @Test
    public void toHexEmptyArray() {
        assertEquals("0 bytes", ByteConverter.toHex(new byte[] {}));
    }

    @Test
    public void toHexNullArray() {
        assertEquals("null", ByteConverter.toHex(null));
    }
}