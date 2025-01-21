package ru.sber.df.epmp.netty_postgres.server.postgres.util;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

public class BitUtil {
    public static final VarHandle VH_LE_SHORT;
    public static final VarHandle VH_LE_INT;
    public static final VarHandle VH_LE_LONG;
    public static final VarHandle VH_LE_FLOAT;
    public static final VarHandle VH_LE_DOUBLE;
    /** @deprecated */
    @Deprecated
    public static final VarHandle VH_BE_SHORT;
    /** @deprecated */
    @Deprecated
    public static final VarHandle VH_BE_INT;
    /** @deprecated */
    @Deprecated
    public static final VarHandle VH_BE_LONG;
    /** @deprecated */
    @Deprecated
    public static final VarHandle VH_BE_FLOAT;
    /** @deprecated */
    @Deprecated
    public static final VarHandle VH_BE_DOUBLE;
    private static final long MAGIC0 = 6148914691236517205L;
    private static final long MAGIC1 = 3689348814741910323L;
    private static final long MAGIC2 = 1085102592571150095L;
    private static final long MAGIC3 = 71777214294589695L;
    private static final long MAGIC4 = 281470681808895L;
    private static final long MAGIC5 = 4294967295L;
    private static final long MAGIC6 = -6148914691236517206L;
    private static final long SHIFT0 = 1L;
    private static final long SHIFT1 = 2L;
    private static final long SHIFT2 = 4L;
    private static final long SHIFT3 = 8L;
    private static final long SHIFT4 = 16L;

    private BitUtil() {
    }

    public static int nextHighestPowerOfTwo(int v) {
        --v;
        v |= v >> 1;
        v |= v >> 2;
        v |= v >> 4;
        v |= v >> 8;
        v |= v >> 16;
        ++v;
        return v;
    }

    public static long nextHighestPowerOfTwo(long v) {
        --v;
        v |= v >> 1;
        v |= v >> 2;
        v |= v >> 4;
        v |= v >> 8;
        v |= v >> 16;
        v |= v >> 32;
        ++v;
        return v;
    }

    public static long interleave(int even, int odd) {
        long v1 = 4294967295L & (long)even;
        long v2 = 4294967295L & (long)odd;
        v1 = (v1 | v1 << 16) & 281470681808895L;
        v1 = (v1 | v1 << 8) & 71777214294589695L;
        v1 = (v1 | v1 << 4) & 1085102592571150095L;
        v1 = (v1 | v1 << 2) & 3689348814741910323L;
        v1 = (v1 | v1 << 1) & 6148914691236517205L;
        v2 = (v2 | v2 << 16) & 281470681808895L;
        v2 = (v2 | v2 << 8) & 71777214294589695L;
        v2 = (v2 | v2 << 4) & 1085102592571150095L;
        v2 = (v2 | v2 << 2) & 3689348814741910323L;
        v2 = (v2 | v2 << 1) & 6148914691236517205L;
        return v2 << 1 | v1;
    }

    public static long deinterleave(long b) {
        b &= 6148914691236517205L;
        b = (b ^ b >>> 1) & 3689348814741910323L;
        b = (b ^ b >>> 2) & 1085102592571150095L;
        b = (b ^ b >>> 4) & 71777214294589695L;
        b = (b ^ b >>> 8) & 281470681808895L;
        b = (b ^ b >>> 16) & 4294967295L;
        return b;
    }

    public static long flipFlop(long b) {
        return (b & -6148914691236517206L) >>> 1 | (b & 6148914691236517205L) << 1;
    }

    public static int zigZagEncode(int i) {
        return i >> 31 ^ i << 1;
    }

    public static long zigZagEncode(long l) {
        return l >> 63 ^ l << 1;
    }

    public static int zigZagDecode(int i) {
        return i >>> 1 ^ -(i & 1);
    }

    public static long zigZagDecode(long l) {
        return l >>> 1 ^ -(l & 1L);
    }

    static {
        VH_LE_SHORT = MethodHandles.byteArrayViewVarHandle(short[].class, ByteOrder.LITTLE_ENDIAN);
        VH_LE_INT = MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.LITTLE_ENDIAN);
        VH_LE_LONG = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.LITTLE_ENDIAN);
        VH_LE_FLOAT = MethodHandles.byteArrayViewVarHandle(float[].class, ByteOrder.LITTLE_ENDIAN);
        VH_LE_DOUBLE = MethodHandles.byteArrayViewVarHandle(double[].class, ByteOrder.LITTLE_ENDIAN);
        VH_BE_SHORT = MethodHandles.byteArrayViewVarHandle(short[].class, ByteOrder.BIG_ENDIAN);
        VH_BE_INT = MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.BIG_ENDIAN);
        VH_BE_LONG = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.BIG_ENDIAN);
        VH_BE_FLOAT = MethodHandles.byteArrayViewVarHandle(float[].class, ByteOrder.BIG_ENDIAN);
        VH_BE_DOUBLE = MethodHandles.byteArrayViewVarHandle(double[].class, ByteOrder.BIG_ENDIAN);
    }

}
