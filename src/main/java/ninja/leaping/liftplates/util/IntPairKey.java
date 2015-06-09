package ninja.leaping.liftplates.util;

/**
 * @author zml2008
 */
public class IntPairKey {
    public static long key(int key1, int key2) {
        return ((long) key1) << 32 | key2 & 0xFFFFFFFFL;
    }

    public static int key1(long key) {
        return (int) (key >> 32);
    }

    public static int key2(long key) {
        return (int) key;
    }


}
