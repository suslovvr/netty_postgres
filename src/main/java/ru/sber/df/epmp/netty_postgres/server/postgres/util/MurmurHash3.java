package ru.sber.df.epmp.netty_postgres.server.postgres.util;

import org.apache.tomcat.util.codec.binary.StringUtils;

public class MurmurHash3 {
    /**
     * A random number to use for a hash code.
     *
     * @deprecated This is not used internally and will be removed in a future release.
     */
    @Deprecated
    public static final long NULL_HASHCODE = 2862933555777941757L;
    /**
     * A default seed to use for the murmur hash algorithm.
     * Has the value {@code 104729}.
     */
    public static final int DEFAULT_SEED = 104729;
    // Constants for 32-bit variant
    private static final int C1_32 = 0xcc9e2d51;
    private static final int C2_32 = 0x1b873593;
    private static final int R1_32 = 15;
    private static final int R2_32 = 13;

    private static final int M_32 = 5;
    private static final int N_32 = 0xe6546b64;
    // Constants for 128-bit variant
    private static final long C1 = 0x87c37b91114253d5L;
    private static final long C2 = 0x4cf5ad432745937fL;
    private static final int R1 = 31;
    private static final int R2 = 27;
    private static final int R3 = 33;
    private static final int M = 5;

    private static final int N1 = 0x52dce729;

    private static final int N2 = 0x38495ab5;

    /**
     * Performs the final avalanche mix step of the 32-bit hash function {@code MurmurHash3_x86_32}.
     *
     * @param hash The current hash
     * @return The final hash
     */
    private static int fmix32(int hash) {
        hash ^= hash >>> 16;
        hash *= 0x85ebca6b;
        hash ^= hash >>> 13;
        hash *= 0xc2b2ae35;
        hash ^= hash >>> 16;
        return hash;
    }
    /**
     * Gets the little-endian int from 4 bytes starting at the specified index.
     *
     * @param data The data
     * @param index The index
     * @return The little-endian int
     */
    private static int getLittleEndianInt(final byte[] data, final int index) {
        return data[index    ] & 0xff |
                (data[index + 1] & 0xff) <<  8 |
                (data[index + 2] & 0xff) << 16 |
                (data[index + 3] & 0xff) << 24;
    }

    /**
     * Gets the little-endian long from 8 bytes starting at the specified index.
     *
     * @param data The data
     * @param index The index
     * @return The little-endian long
     */
    private static long getLittleEndianLong(final byte[] data, final int index) {
        return (long) data[index    ] & 0xff |
                ((long) data[index + 1] & 0xff) <<  8 |
                ((long) data[index + 2] & 0xff) << 16 |
                ((long) data[index + 3] & 0xff) << 24 |
                ((long) data[index + 4] & 0xff) << 32 |
                ((long) data[index + 5] & 0xff) << 40 |
                ((long) data[index + 6] & 0xff) << 48 |
                ((long) data[index + 7] & 0xff) << 56;
    }
    /**
     * Generates 64-bit hash from a long with a default seed.
     *
     * <p><strong>This is not part of the original MurmurHash3 {@code c++} implementation.</strong></p>
     *
     * <p>This is a Murmur3-like 64-bit variant.
     * The method does not produce the same result as either half of the hash bytes from
     * {@linkplain #hash128x64(byte[])} with the same byte data from the {@code long}.
     * This method will be removed in a future release.</p>
     *
     * <p>Note: The sign extension bug in {@link #hash64(byte[], int, int, int)} does not effect
     * this result as the default seed is positive.</p>
     *
     * <p>This is a helper method that will produce the same result as:</p>
     *
     * <pre>
     * int offset = 0;
     * int seed = 104729;
     * long hash = MurmurHash3.hash64(ByteBuffer.allocate(8)
     *                                          .putLong(data)
     *                                          .array(), offset, 8, seed);
     * </pre>
     *
     * @param data The long to hash
     * @return The 64-bit hash
     * @see #hash64(byte[], int, int, int)
     * @deprecated Not part of the MurmurHash3 implementation.
     * Use half of the hash bytes from {@link #hash128x64(byte[])} with the bytes from the {@code long}.
     */
    @Deprecated
    public static long hash64(final long data) {
        long hash = DEFAULT_SEED;
        long k = Long.reverseBytes(data);
        // mix functions
        k *= C1;
        k = Long.rotateLeft(k, R1);
        k *= C2;
        hash ^= k;
        hash = Long.rotateLeft(hash, R2) * M + N1;
        // finalization
        hash ^= Long.BYTES;
        return fmix64(hash);
    }

    /**
     * Generates 64-bit hash from a short with a default seed.
     *
     * <p><strong>This is not part of the original MurmurHash3 {@code c++} implementation.</strong></p>
     *
     * <p>This is a Murmur3-like 64-bit variant.
     * The method does not produce the same result as either half of the hash bytes from
     * {@linkplain #hash128x64(byte[])} with the same byte data from the {@code short}.
     * This method will be removed in a future release.</p>
     *
     * <p>Note: The sign extension bug in {@link #hash64(byte[], int, int, int)} does not effect
     * this result as the default seed is positive.</p>
     *
     * <p>This is a helper method that will produce the same result as:</p>
     *
     * <pre>
     * int offset = 0;
     * int seed = 104729;
     * long hash = MurmurHash3.hash64(ByteBuffer.allocate(2)
     *                                          .putShort(data)
     *                                          .array(), offset, 2, seed);
     * </pre>
     *
     * @param data The short to hash
     * @return The 64-bit hash
     * @see #hash64(byte[], int, int, int)
     * @deprecated Not part of the MurmurHash3 implementation.
     * Use half of the hash bytes from {@link #hash128x64(byte[])} with the bytes from the {@code short}.
     */
    @Deprecated
    public static long hash64(final short data) {
        long hash = DEFAULT_SEED;
        long k1 = 0;
        k1 ^= ((long) data & 0xff) << 8;
        k1 ^= (long) ((data & 0xFF00) >> 8) & 0xff;
        k1 *= C1;
        k1 = Long.rotateLeft(k1, R1);
        k1 *= C2;
        hash ^= k1;

        // finalization
        hash ^= Short.BYTES;
        return fmix64(hash);
    }

    /**
     * Performs the intermediate mix step of the 32-bit hash function {@code MurmurHash3_x86_32}.
     *
     * @param k The data to add to the hash
     * @param hash The current hash
     * @return The new hash
     */
    private static int mix32(int k, int hash) {
        k *= C1_32;
        k = Integer.rotateLeft(k, R1_32);
        k *= C2_32;
        hash ^= k;
        return Integer.rotateLeft(hash, R2_32) * M_32 + N_32;
    }

//---------------------------------------

    /**
     * Generates 32-bit hash from the byte array with a default seed.
     * This is a helper method that will produce the same result as:
     *
     * <pre>
     * int offset = 0;
     * int seed = 104729;
     * int hash = MurmurHash3.hash32(data, offset, data.length, seed);
     * </pre>
     *
     * <p>This implementation contains a sign-extension bug in the finalization step of
     * any bytes left over from dividing the length by 4. This manifests if any of these
     * bytes are negative.</p>
     *
     * @param data The input byte array
     * @return The 32-bit hash
     * @see #hash32(byte[], int, int, int)
     * @deprecated Use {@link #hash32x86(byte[], int, int, int)}. This corrects the processing of trailing bytes.
     */
    @Deprecated
    public static int hash32(final byte[] data) {
        return hash32(data, 0, data.length, DEFAULT_SEED);
    }

    /**
     * Generates 32-bit hash from the byte array with the given length and a default seed.
     * This is a helper method that will produce the same result as:
     *
     * <pre>
     * int offset = 0;
     * int seed = 104729;
     * int hash = MurmurHash3.hash32(data, offset, length, seed);
     * </pre>
     *
     * <p>This implementation contains a sign-extension bug in the finalization step of
     * any bytes left over from dividing the length by 4. This manifests if any of these
     * bytes are negative.</p>
     *
     * @param data The input byte array
     * @param length The length of array
     * @return The 32-bit hash
     * @see #hash32(byte[], int, int, int)
     * @deprecated Use {@link #hash32x86(byte[], int, int, int)}. This corrects the processing of trailing bytes.
     */
    @Deprecated
    public static int hash32(final byte[] data, final int length) {
        return hash32(data, length, DEFAULT_SEED);
    }

    /**
     * Generates 32-bit hash from the byte array with the given length and seed. This is a
     * helper method that will produce the same result as:
     *
     * <pre>
     * int offset = 0;
     * int hash = MurmurHash3.hash32(data, offset, length, seed);
     * </pre>
     *
     * <p>This implementation contains a sign-extension bug in the finalization step of
     * any bytes left over from dividing the length by 4. This manifests if any of these
     * bytes are negative.</p>
     *
     * @param data The input byte array
     * @param length The length of array
     * @param seed The initial seed value
     * @return The 32-bit hash
     * @see #hash32(byte[], int, int, int)
     * @deprecated Use {@link #hash32x86(byte[], int, int, int)}. This corrects the processing of trailing bytes.
     */
    @Deprecated
    public static int hash32(final byte[] data, final int length, final int seed) {
        return hash32(data, 0, length, seed);
    }

    /**
     * Generates 32-bit hash from the byte array with the given offset, length and seed.
     *
     * <p>This is an implementation of the 32-bit hash function {@code MurmurHash3_x86_32}
     * from Austin Appleby's original MurmurHash3 {@code c++} code in SMHasher.</p>
     *
     * <p>This implementation contains a sign-extension bug in the finalization step of
     * any bytes left over from dividing the length by 4. This manifests if any of these
     * bytes are negative.</p>
     *
     * @param data The input byte array
     * @param offset The offset of data
     * @param length The length of array
     * @param seed The initial seed value
     * @return The 32-bit hash
     * @deprecated Use {@link #hash32x86(byte[], int, int, int)}. This corrects the processing of trailing bytes.
     */
    @Deprecated
    public static int hash32(final byte[] data, final int offset, final int length, final int seed) {
        int hash = seed;
        final int nblocks = length >> 2;

        // body
        for (int i = 0; i < nblocks; i++) {
            final int index = offset + (i << 2);
            final int k = getLittleEndianInt(data, index);
            hash = mix32(k, hash);
        }

        // tail
        // ************
        // Note: This fails to apply masking using 0xff to the 3 remaining bytes.
        // ************
        final int index = offset + (nblocks << 2);
        int k1 = 0;
        switch (offset + length - index) {
            case 3:
                k1 ^= data[index + 2] << 16;
            case 2:
                k1 ^= data[index + 1] << 8;
            case 1:
                k1 ^= data[index];

                // mix functions
                k1 *= C1_32;
                k1 = Integer.rotateLeft(k1, R1_32);
                k1 *= C2_32;
                hash ^= k1;
        }

        hash ^= length;
        return fmix32(hash);
    }

    /**
     * Generates 32-bit hash from a long with a default seed value.
     * This is a helper method that will produce the same result as:
     *
     * <pre>
     * int offset = 0;
     * int seed = 104729;
     * int hash = MurmurHash3.hash32x86(ByteBuffer.allocate(8)
     *                                            .putLong(data)
     *                                            .array(), offset, 8, seed);
     * </pre>
     *
     * @param data The long to hash
     * @return The 32-bit hash
     * @see #hash32x86(byte[], int, int, int)
     */
    public static int hash32(final long data) {
        return hash32(data, DEFAULT_SEED);
    }

    /**
     * Generates 32-bit hash from a long with the given seed.
     * This is a helper method that will produce the same result as:
     *
     * <pre>
     * int offset = 0;
     * int hash = MurmurHash3.hash32x86(ByteBuffer.allocate(8)
     *                                            .putLong(data)
     *                                            .array(), offset, 8, seed);
     * </pre>
     *
     * @param data The long to hash
     * @param seed The initial seed value
     * @return The 32-bit hash
     * @see #hash32x86(byte[], int, int, int)
     */
    public static int hash32(final long data, final int seed) {
        int hash = seed;
        final long r0 = Long.reverseBytes(data);

        hash = mix32((int) r0, hash);
        hash = mix32((int) (r0 >>> 32), hash);

        hash ^= Long.BYTES;
        return fmix32(hash);
    }

    /**
     * Generates 32-bit hash from two longs with a default seed value.
     * This is a helper method that will produce the same result as:
     *
     * <pre>
     * int offset = 0;
     * int seed = 104729;
     * int hash = MurmurHash3.hash32x86(ByteBuffer.allocate(16)
     *                                            .putLong(data1)
     *                                            .putLong(data2)
     *                                            .array(), offset, 16, seed);
     * </pre>
     *
     * @param data1 The first long to hash
     * @param data2 The second long to hash
     * @return The 32-bit hash
     * @see #hash32x86(byte[], int, int, int)
     */
    public static int hash32(final long data1, final long data2) {
        return hash32(data1, data2, DEFAULT_SEED);
    }

    /**
     * Generates 32-bit hash from two longs with the given seed.
     * This is a helper method that will produce the same result as:
     *
     * <pre>
     * int offset = 0;
     * int hash = MurmurHash3.hash32x86(ByteBuffer.allocate(16)
     *                                            .putLong(data1)
     *                                            .putLong(data2)
     *                                            .array(), offset, 16, seed);
     * </pre>
     *
     * @param data1 The first long to hash
     * @param data2 The second long to hash
     * @param seed The initial seed value
     * @return The 32-bit hash
     * @see #hash32x86(byte[], int, int, int)
     */
    public static int hash32(final long data1, final long data2, final int seed) {
        int hash = seed;
        final long r0 = Long.reverseBytes(data1);
        final long r1 = Long.reverseBytes(data2);

        hash = mix32((int) r0, hash);
        hash = mix32((int) (r0 >>> 32), hash);
        hash = mix32((int) r1, hash);
        hash = mix32((int) (r1 >>> 32), hash);

        hash ^= Long.BYTES * 2;
        return fmix32(hash);
    }

    /**
     * Generates 32-bit hash from a string with a default seed.
     * <p>
     * Before 1.14 the string was converted using default encoding.
     * Since 1.14 the string is converted to bytes using UTF-8 encoding.
     * </p>
     * This is a helper method that will produce the same result as:
     *
     * <pre>
     * int offset = 0;
     * int seed = 104729;
     * byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
     * int hash = MurmurHash3.hash32(bytes, offset, bytes.length, seed);
     * </pre>
     *
     * <p>This implementation contains a sign-extension bug in the finalization step of
     * any bytes left over from dividing the length by 4. This manifests if any of these
     * bytes are negative.</p>
     *
     * @param data The input string
     * @return The 32-bit hash
     * @see #hash32(byte[], int, int, int)
     * @deprecated Use {@link #hash32x86(byte[], int, int, int)} with the bytes returned from
     * {@link String#getBytes(java.nio.charset.Charset)}. This corrects the processing of trailing bytes.
     */
    @Deprecated
    public static int hash32(final String data) {
        final byte[] bytes = StringUtils.getBytesUtf8(data);
        return hash32(bytes, 0, bytes.length, DEFAULT_SEED);
    }

    /**
     * Generates 32-bit hash from the byte array with a seed of zero.
     * This is a helper method that will produce the same result as:
     *
     * <pre>
     * int offset = 0;
     * int seed = 0;
     * int hash = MurmurHash3.hash32x86(data, offset, data.length, seed);
     * </pre>
     *
     * @param data The input byte array
     * @return The 32-bit hash
     * @see #hash32x86(byte[], int, int, int)
     * @since 1.14
     */
    public static int hash32x86(final byte[] data) {
        return hash32x86(data, 0, data.length, 0);
    }

    /**
     * Generates 32-bit hash from the byte array with the given offset, length and seed.
     *
     * <p>This is an implementation of the 32-bit hash function {@code MurmurHash3_x86_32}
     * from Austin Appleby's original MurmurHash3 {@code c++} code in SMHasher.</p>
     *
     * @param data The input byte array
     * @param offset The offset of data
     * @param length The length of array
     * @param seed The initial seed value
     * @return The 32-bit hash
     * @since 1.14
     */
    public static int hash32x86(final byte[] data, final int offset, final int length, final int seed) {
        int hash = seed;
        final int nblocks = length >> 2;

        // body
        for (int i = 0; i < nblocks; i++) {
            final int index = offset + (i << 2);
            final int k = getLittleEndianInt(data, index);
            hash = mix32(k, hash);
        }

        // tail
        final int index = offset + (nblocks << 2);
        int k1 = 0;
        switch (offset + length - index) {
            case 3:
                k1 ^= (data[index + 2] & 0xff) << 16;
            case 2:
                k1 ^= (data[index + 1] & 0xff) << 8;
            case 1:
                k1 ^= data[index] & 0xff;

                // mix functions
                k1 *= C1_32;
                k1 = Integer.rotateLeft(k1, R1_32);
                k1 *= C2_32;
                hash ^= k1;
        }

        hash ^= length;
        return fmix32(hash);
    }

    /**
     * Generates 64-bit hash from a byte array with a default seed.
     *
     * <p><strong>This is not part of the original MurmurHash3 {@code c++} implementation.</strong></p>
     *
     * <p>This is a Murmur3-like 64-bit variant.
     * The method does not produce the same result as either half of the hash bytes from
     * {@linkplain #hash128x64(byte[])} with the same byte data.
     * This method will be removed in a future release.</p>
     *
     * <p>Note: The sign extension bug in {@link #hash64(byte[], int, int, int)} does not effect
     * this result as the default seed is positive.</p>
     *
     * <p>This is a helper method that will produce the same result as:</p>
     *
     * <pre>
     * int offset = 0;
     * int seed = 104729;
     * long hash = MurmurHash3.hash64(data, offset, data.length, seed);
     * </pre>
     *
     * @param data The input byte array
     * @return The 64-bit hash
     * @see #hash64(byte[], int, int, int)
     * @deprecated Not part of the MurmurHash3 implementation.
     * Use half of the hash bytes from {@link #hash128x64(byte[])}.
     */
    @Deprecated
    public static long hash64(final byte[] data) {
        return hash64(data, 0, data.length, DEFAULT_SEED);
    }

    /**
     * Generates 64-bit hash from a byte array with the given offset and length and a default seed.
     *
     * <p><strong>This is not part of the original MurmurHash3 {@code c++} implementation.</strong></p>
     *
     * <p>This is a Murmur3-like 64-bit variant.
     * The method does not produce the same result as either half of the hash bytes from
     * {@linkplain #hash128x64(byte[])} with the same byte data.
     * This method will be removed in a future release.</p>
     *
     * <p>Note: The sign extension bug in {@link #hash64(byte[], int, int, int)} does not effect
     * this result as the default seed is positive.</p>
     *
     * <p>This is a helper method that will produce the same result as:</p>
     *
     * <pre>
     * int seed = 104729;
     * long hash = MurmurHash3.hash64(data, offset, length, seed);
     * </pre>
     *
     * @param data The input byte array
     * @param offset The offset of data
     * @param length The length of array
     * @return The 64-bit hash
     * @see #hash64(byte[], int, int, int)
     * @deprecated Not part of the MurmurHash3 implementation.
     * Use half of the hash bytes from {@link #hash128x64(byte[], int, int, int)}.
     */
    @Deprecated
    public static long hash64(final byte[] data, final int offset, final int length) {
        return hash64(data, offset, length, DEFAULT_SEED);
    }

    /**
     * Generates 64-bit hash from a byte array with the given offset, length and seed.
     *
     * <p><strong>This is not part of the original MurmurHash3 {@code c++} implementation.</strong></p>
     *
     * <p>This is a Murmur3-like 64-bit variant.
     * This method will be removed in a future release.</p>
     *
     * <p>This implementation contains a sign-extension bug in the seed initialization.
     * This manifests if the seed is negative.</p>
     *
     * <p>This algorithm processes 8 bytes chunks of data in a manner similar to the 16 byte chunks
     * of data processed in the MurmurHash3 {@code MurmurHash3_x64_128} method. However the hash
     * is not mixed with a hash chunk from the next 8 bytes of data. The method will not return
     * the same value as the first or second 64-bits of the function
     * {@link #hash128(byte[], int, int, int)}.</p>
     *
     * <p>Use of this method is not advised. Use the first long returned from
     * {@link #hash128x64(byte[], int, int, int)}.</p>
     *
     * @param data The input byte array
     * @param offset The offset of data
     * @param length The length of array
     * @param seed The initial seed value
     * @return The 64-bit hash
     * @deprecated Not part of the MurmurHash3 implementation.
     * Use half of the hash bytes from {@link #hash128x64(byte[], int, int, int)}.
     */
    @Deprecated
    public static long hash64(final byte[] data, final int offset, final int length, final int seed) {
        //
        // Note: This fails to apply masking using 0xffffffffL to the seed.
        //
        long hash = seed;
        final int nblocks = length >> 3;

        // body
        for (int i = 0; i < nblocks; i++) {
            final int index = offset + (i << 3);
            long k = getLittleEndianLong(data, index);

            // mix functions
            k *= C1;
            k = Long.rotateLeft(k, R1);
            k *= C2;
            hash ^= k;
            hash = Long.rotateLeft(hash, R2) * M + N1;
        }

        // tail
        long k1 = 0;
        final int index = offset + (nblocks << 3);
        switch (offset + length - index) {
            case 7:
                k1 ^= ((long) data[index + 6] & 0xff) << 48;
            case 6:
                k1 ^= ((long) data[index + 5] & 0xff) << 40;
            case 5:
                k1 ^= ((long) data[index + 4] & 0xff) << 32;
            case 4:
                k1 ^= ((long) data[index + 3] & 0xff) << 24;
            case 3:
                k1 ^= ((long) data[index + 2] & 0xff) << 16;
            case 2:
                k1 ^= ((long) data[index + 1] & 0xff) << 8;
            case 1:
                k1 ^= (long) data[index] & 0xff;
                k1 *= C1;
                k1 = Long.rotateLeft(k1, R1);
                k1 *= C2;
                hash ^= k1;
        }

        // finalization
        hash ^= length;
        return fmix64(hash);
    }

    /**
     * Generates 64-bit hash from an int with a default seed.
     *
     * <p><strong>This is not part of the original MurmurHash3 {@code c++} implementation.</strong></p>
     *
     * <p>This is a Murmur3-like 64-bit variant.
     * The method does not produce the same result as either half of the hash bytes from
     * {@linkplain #hash128x64(byte[])} with the same byte data from the {@code int}.
     * This method will be removed in a future release.</p>
     *
     * <p>Note: The sign extension bug in {@link #hash64(byte[], int, int, int)} does not effect
     * this result as the default seed is positive.</p>
     *
     * <p>This is a helper method that will produce the same result as:</p>
     *
     * <pre>
     * int offset = 0;
     * int seed = 104729;
     * long hash = MurmurHash3.hash64(ByteBuffer.allocate(4)
     *                                          .putInt(data)
     *                                          .array(), offset, 4, seed);
     * </pre>
     *
     * @param data The int to hash
     * @return The 64-bit hash
     * @see #hash64(byte[], int, int, int)
     * @deprecated Not part of the MurmurHash3 implementation.
     * Use half of the hash bytes from {@link #hash128x64(byte[])} with the bytes from the {@code int}.
     */
    @Deprecated
    public static long hash64(final int data) {
        long k1 = Integer.reverseBytes(data) & -1L >>> 32;
        long hash = DEFAULT_SEED;
        k1 *= C1;
        k1 = Long.rotateLeft(k1, R1);
        k1 *= C2;
        hash ^= k1;
        // finalization
        hash ^= Integer.BYTES;
        return fmix64(hash);
    }

//---------------------------------------
    /**
     * Performs the final avalanche mix step of the 64-bit hash function {@code MurmurHash3_x64_128}.
     *
     * @param hash The current hash
     * @return The final hash
     */
    private static long fmix64(long hash) {
        hash ^= hash >>> 33;
        hash *= 0xff51afd7ed558ccdL;
        hash ^= hash >>> 33;
        hash *= 0xc4ceb9fe1a85ec53L;
        hash ^= hash >>> 33;
        return hash;
    }
}
