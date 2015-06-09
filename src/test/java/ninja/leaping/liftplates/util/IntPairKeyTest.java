package ninja.leaping.liftplates.util;

import static org.junit.Assert.assertEquals;

/**
 * @author zml2008
 */
public class IntPairKeyTest {
    // @Test // (This test takes a long time, so only needs to be run if there are issues)
    public void testIntPairKey() {
        for (int a = Integer.MIN_VALUE >> 16; a < Integer.MAX_VALUE >> 16; a += 5) {
            for (int b = Integer.MIN_VALUE >> 16; b < Integer.MAX_VALUE >> 16; b += 7) {
                long key = IntPairKey.key(a, b);
                assertEquals(a, IntPairKey.key1(key));
                assertEquals(b, IntPairKey.key2(key));
            }
        }
    }
}
