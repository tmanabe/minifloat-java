package io.github.tmanabe;

public class INT8 {
    public static byte encode(float f) {
        return (byte) Math.round(f * 8f);
    }

    public static float decode(byte b) {
        return b / 8f;
    }
}
