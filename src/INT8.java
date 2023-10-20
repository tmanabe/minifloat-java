class INT8 {
    static byte encode(float f) {
        return (byte) Math.round(f * 8f);
    }

    static float decode(byte b) {
        return b / 8f;
    }
}
