class INT4 {
    static byte encode(float f) {
        int i = Math.round(f);
        if (i < -8) {
            return (byte) -8;  // 0b1000
        }
        if (7 < i) {
            return (byte) 7;  // 0b0111
        }
        return (byte) i;
    }

    static float decode(byte b) {
        return b;
    }

    static byte pack(byte a, byte b) {
        int result = 0;
        if (a < 0) {
            result |= 1 << 7;
        }
        result |= (a & 0b111) << 4;
        if (b < 0) {
            result |= 1 << 3;
        }
        result |= b & 0b111;
        return (byte) result;
    }

    static byte[] unpack(byte b) {
        int[] results = new int[2];
        results[0] = b >> 4;
        if (0 < (b & (1 << 3))) {
            results[1] = 0b11111000 | (b & 0b111);
        } else {
            results[1] = b & 0b111;
        }
        return new byte[]{(byte) results[0], (byte) results[1]};
    }

    static byte[] batchEncode(float[] fs) {
        assert 0 == fs.length % 2;
        byte[] bs = new byte[fs.length / 2];
        for (int i = 0; i < bs.length; ++i) {
            bs[i] = pack(encode(fs[i * 2]), encode(fs[i * 2 + 1]));
        }
        return bs;
    }

    static float dotProduct(float[] fs, byte[] bs) {
        assert fs.length == bs.length * 2;
        float result = 0f;
        for (int i = 0; i < bs.length; ++i) {
            result += fs[i * 2] * decode(unpack(bs[i])[0]);
            result += fs[i * 2 + 1] * decode(unpack(bs[i])[1]);
        }
        return result;
    }
}
