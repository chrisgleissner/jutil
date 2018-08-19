package uk.gleissner.jutil.converter;

import org.junit.Test;

import static org.junit.Assert.*;

public class ByteConverterTest {

    @Test
    public void toHex() {
        assertEquals("010203", ByteConverter.toHex(new byte[]{1, 2, 3}));
    }

    @Test
    public void toSpacedHex() {
        assertEquals("  3 byte(s): 01 02 03", ByteConverter.toSpacedHex(new byte[]{1, 2, 3}));
    }

    @Test
    public void toSpacedHexEmptyArray() {
        assertEquals("0 bytes", ByteConverter.toSpacedHex(new byte[]{}));
    }

    @Test
    public void toSpacedHexNullArray() {
        assertEquals("null", ByteConverter.toSpacedHex((byte[]) null));
    }
}