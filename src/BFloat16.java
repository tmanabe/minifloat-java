public class BFloat16 {
    static short encode(float f) {
        return (short) ((Float.floatToIntBits(f) + (1 << 15)) >> 16);
    }

    static float decode(short s) {
        return Float.intBitsToFloat(s << 16);
    }
}
