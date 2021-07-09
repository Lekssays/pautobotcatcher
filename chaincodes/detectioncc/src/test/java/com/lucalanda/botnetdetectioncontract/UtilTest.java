package com.lucalanda.botnetdetectioncontract;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static com.lucalanda.botnetdetectioncontract.Util.splitByteArrayBySize;
import static com.lucalanda.botnetdetectioncontract.Util.splitStringBySize;
import static java.util.Arrays.asList;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UtilTest {
    @Test
    public void isInteger_works_with_trailing_zeroes() {
        String timestamp = "1602280800000";

        assertTrue(Util.isInteger(timestamp));
    }

    @Test
    public void isInteger_works_correctly() {
        assertTrue(Util.isInteger("123"));
        assertTrue(Util.isInteger("123098"));

        assertFalse(Util.isInteger("123d"));
        assertFalse(Util.isInteger("123d098"));
    }

    @Test
    public void isInteger_returns_false_for_empty_strings() {
        assertFalse(Util.isInteger(""));
    }

    @Test
    public void repeat_repeats_string_correctly() {
        assertEquals("00000000000000000000", Util.repeat("0", 20));
        assertEquals("01010101010101010101", Util.repeat("01", 10));
        assertEquals("012012012012012", Util.repeat("012", 5));
    }

    @Test
    public void leftPad_works_correctly() {
        assertEquals("00000000000000000000", Util.leftPad("000", 20, "0"));
        assertEquals("000000000000123", Util.leftPad("123", 15, "0"));
    }
    
    @Test
    public void splitStringBySize_works_correctly() {
        String s = "1234567890abcdefghijk";

        Collection<String> actual = splitStringBySize(s, 4);
        List<String> expected = asList("1234", "5678", "90ab", "cdef", "ghij", "k");

        assertEquals(expected, actual);
    }

    @Test
    public void splitByteArrayBySize_works_correctly() {
        byte[] original = new byte[]{0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9};

        byte[][] expectedSplit_2 = new byte[][]{
                new byte[]{0x0, 0x1},
                new byte[]{0x2, 0x3},
                new byte[]{0x4, 0x5},
                new byte[]{0x6, 0x7},
                new byte[]{0x8, 0x9},
        };
        byte[][] expectedSplit_3 = new byte[][]{
                new byte[]{0x0, 0x1, 0x2},
                new byte[]{0x3, 0x4, 0x5},
                new byte[]{0x6, 0x7, 0x8},
                new byte[]{0x9},
        };
        byte[][] expectedSplit_6 = new byte[][]{
                new byte[]{0x0, 0x1, 0x2, 0x3, 0x4, 0x5},
                new byte[]{0x6, 0x7, 0x8, 0x9},
        };
        byte[][] expectedSplit_10 = new byte[][]{
                new byte[]{0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9}
        };
        byte[][] expectedSplit_100 = expectedSplit_10;

        byte[][] actualSplit_2 = splitByteArrayBySize(original, 2);
        byte[][] actualSplit_3 = splitByteArrayBySize(original, 3);
        byte[][] actualSplit_6 = splitByteArrayBySize(original, 6);
        byte[][] actualSplit_10 = splitByteArrayBySize(original, 10);
        byte[][] actualSplit_100 = splitByteArrayBySize(original, 100);

        assertSplitEquals(expectedSplit_2, actualSplit_2);
        assertSplitEquals(expectedSplit_3, actualSplit_3);
        assertSplitEquals(expectedSplit_6, actualSplit_6);
        assertSplitEquals(expectedSplit_10, actualSplit_10);
        assertSplitEquals(expectedSplit_100, actualSplit_100);
    }

    private void assertSplitEquals(byte[][] expectedSplit, byte[][] actualSplit) {
        assertEquals(expectedSplit.length, actualSplit.length);

        for(int i = 0; i < expectedSplit.length; i++) {
            assertArrayEquals(expectedSplit[i], actualSplit[i]);
        }
    }
}
