public class INT8 {
    public static byte encode(float f) {
        return (byte) (f * 64);
    }

    public static float decode(byte b) {
        return ((float) b) / 64;
    }
}
