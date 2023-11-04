package io.github.tmanabe;

public class BFloat16 {
    public static short encode(float f) {
        return (short) ((Float.floatToIntBits(f) + (1 << 15)) >> 16);
    }

    public static float decode(short s) {
        return Float.intBitsToFloat(s << 16);
    }
}
