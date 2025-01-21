package ru.sber.df.epmp.netty_postgres.server.postgres.util;

import ru.sber.df.epmp.netty_postgres.server.postgres.Constants;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
@Slf4j
public class RamUsageEstimator {
    public static final long ONE_KB = 1024L;
    public static final long ONE_MB = 1048576L;
    public static final long ONE_GB = 1073741824L;
    public static final boolean COMPRESSED_REFS_ENABLED;
    public static final int NUM_BYTES_OBJECT_REF;
    public static final int NUM_BYTES_OBJECT_HEADER;
    public static final int NUM_BYTES_ARRAY_HEADER;
    public static final int NUM_BYTES_OBJECT_ALIGNMENT;
    public static final int QUERY_DEFAULT_RAM_BYTES_USED = 1024;
    public static final int UNKNOWN_DEFAULT_RAM_BYTES_USED = 256;
    public static final Map<Class<?>, Integer> primitiveSizes;
    static final int INTEGER_SIZE;
    static final int LONG_SIZE;
    static final int STRING_SIZE;
    static final boolean JVM_IS_HOTSPOT_64BIT;
    static final String MANAGEMENT_FACTORY_CLASS = "java.lang.management.ManagementFactory";
    static final String HOTSPOT_BEAN_CLASS = "com.sun.management.HotSpotDiagnosticMXBean";
    public static final long HASHTABLE_RAM_BYTES_PER_ENTRY;
    public static final long LINKED_HASHTABLE_RAM_BYTES_PER_ENTRY;
    public static final int MAX_DEPTH = 1;

    private RamUsageEstimator() {
    }

    public static long alignObjectSize(long size) {
        size += (long)NUM_BYTES_OBJECT_ALIGNMENT - 1L;
        return size - size % (long)NUM_BYTES_OBJECT_ALIGNMENT;
    }

    public static long shallowSizeOf(Object obj) {
        if (obj == null) {
            return 0L;
        } else {
            Class<?> clz = obj.getClass();
            return clz.isArray() ? shallowSizeOfArray(obj) : shallowSizeOfInstance(clz);
        }
    }

    public static long shallowSizeOfInstance(Class<?> clazz) {
        if (clazz.isArray()) {
            throw new IllegalArgumentException("This method does not work with array classes.");
        } else if (clazz.isPrimitive()) {
            return (long)(Integer)primitiveSizes.get(clazz);
        } else {
            long size;
            for(size = (long)NUM_BYTES_OBJECT_HEADER; clazz != null; clazz = clazz.getSuperclass()) {
                Class<?> target = clazz;

                Field[] fields = target.getDeclaredFields();
                /*
                try {
                    Objects.requireNonNull(target);
                    fields = (Field[]) AccessController.doPrivileged(target::getDeclaredFields);
                } catch (AccessControlException e) {
                    throw new RuntimeException("Can't access fields of class: " + clazz, e);
                }
*/
                for(Field f : fields) {
                    if (!Modifier.isStatic(f.getModifiers())) {
                        size = adjustForField(size, f);
                    }
                }
            }

            return alignObjectSize(size);
        }
    }

    private static long shallowSizeOfArray(Object array) {
        long size = (long)NUM_BYTES_ARRAY_HEADER;
        int len = Array.getLength(array);
        if (len > 0) {
            Class<?> arrayElementClazz = array.getClass().getComponentType();
            if (arrayElementClazz.isPrimitive()) {
                size += (long)len * (long)(Integer)primitiveSizes.get(arrayElementClazz);
            } else {
                size += (long)NUM_BYTES_OBJECT_REF * (long)len;
            }
        }

        return alignObjectSize(size);
    }

    public static long adjustForField(long sizeSoFar, Field f) {
        Class<?> type = f.getType();
        int fsize = type.isPrimitive() ? (Integer)primitiveSizes.get(type) : NUM_BYTES_OBJECT_REF;
        return sizeSoFar + (long)fsize;
    }

    public static String humanReadableUnits(long bytes) {
        return humanReadableUnits(bytes, new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.ROOT)));
    }

    public static String humanReadableUnits(long bytes, DecimalFormat df) {
        if (bytes / 1073741824L > 0L) {
            String var4 = df.format((double)((float)bytes / 1.0737418E9F));
            return var4 + " GB";
        } else if (bytes / 1048576L > 0L) {
            String var3 = df.format((double)((float)bytes / 1048576.0F));
            return var3 + " MB";
        } else if (bytes / 1024L > 0L) {
            String var10000 = df.format((double)((float)bytes / 1024.0F));
            return var10000 + " KB";
        } else {
            return bytes + " bytes";
        }
    }


    public static long sizeOfMap(Map<?, ?> map) {
        return sizeOfMap(map, 0, 256L);
    }

    public static long sizeOfMap(Map<?, ?> map, long defSize) {
        return sizeOfMap(map, 0, defSize);
    }

    private static long sizeOfMap(Map<?, ?> map, int depth, long defSize) {
        if (map == null) {
            return 0L;
        } else {
            long size = shallowSizeOf((Object)map);
            if (depth > 1) {
                return size;
            } else {
                long sizeOfEntry = -1L;

                for(Map.Entry<?, ?> entry : map.entrySet()) {
                    if (sizeOfEntry == -1L) {
                        sizeOfEntry = shallowSizeOf((Object)entry);
                    }

                    size += sizeOfEntry;
                    size += sizeOfObject(entry.getKey(), depth, defSize);
                    size += sizeOfObject(entry.getValue(), depth, defSize);
                }

                return alignObjectSize(size);
            }
        }
    }

    public static long sizeOfObject(Object o) {
        return sizeOfObject(o, 0, 256L);
    }

    public static long sizeOfObject(Object o, long defSize) {
        return sizeOfObject(o, 0, defSize);
    }

    private static long sizeOfObject(Object o, int depth, long defSize) {
        if (o == null) {
            return 0L;
        } else {
            long size;
            /*if (o instanceof Accountable) {
                size = ((Accountable)o).ramBytesUsed();
            } else*/ if (o instanceof String) {
                size = sizeOf((String)o);
            } else if (o instanceof boolean[]) {
                size = sizeOf((boolean[])o);
            } else if (o instanceof byte[]) {
                size = sizeOf((byte[])o);
            } else if (o instanceof char[]) {
                size = sizeOf((char[])o);
            } else if (o instanceof double[]) {
                size = sizeOf((double[])o);
            } else if (o instanceof float[]) {
                size = sizeOf((float[])o);
            } else if (o instanceof int[]) {
                size = sizeOf((int[])o);
            } else if (o instanceof Integer) {
                size = sizeOf((Integer)o);
            } else if (o instanceof Long) {
                size = sizeOf((Long)o);
            } else if (o instanceof long[]) {
                size = sizeOf((long[])o);
            } else if (o instanceof short[]) {
                size = sizeOf((short[])o);
            } else if (o instanceof String[]) {
                size = sizeOf((String[])o);
            }/* else if (o instanceof Query) {
                size = sizeOf((Query)o, defSize);
            }*/ else if (o instanceof Map) {
                Map var10000 = (Map)o;
                ++depth;
                size = sizeOfMap(var10000, depth, defSize);
            } /*else if (o instanceof Collection) {
                Collection var8 = (Collection)o;
                ++depth;
                size = sizeOfCollection(var8, depth, defSize);
            }*/ else if (defSize > 0L) {
                size = defSize;
            } else {
                size = shallowSizeOf(o);
            }

            return size;
        }
    }
    public static long sizeOf(Integer ignored) {
        return (long)INTEGER_SIZE;
    }

    public static long sizeOf(Long ignored) {
        return (long)LONG_SIZE;
    }

    public static long sizeOf(byte[] arr) {
        return alignObjectSize((long)NUM_BYTES_ARRAY_HEADER + (long)arr.length);
    }

    public static long sizeOf(boolean[] arr) {
        return alignObjectSize((long)NUM_BYTES_ARRAY_HEADER + (long)arr.length);
    }

    public static long sizeOf(char[] arr) {
        return alignObjectSize((long)NUM_BYTES_ARRAY_HEADER + 2L * (long)arr.length);
    }

    public static long sizeOf(short[] arr) {
        return alignObjectSize((long)NUM_BYTES_ARRAY_HEADER + 2L * (long)arr.length);
    }

    public static long sizeOf(int[] arr) {
        return alignObjectSize((long)NUM_BYTES_ARRAY_HEADER + 4L * (long)arr.length);
    }

    public static long sizeOf(float[] arr) {
        return alignObjectSize((long)NUM_BYTES_ARRAY_HEADER + 4L * (long)arr.length);
    }

    public static long sizeOf(long[] arr) {
        return alignObjectSize((long)NUM_BYTES_ARRAY_HEADER + 8L * (long)arr.length);
    }

    public static long sizeOf(double[] arr) {
        return alignObjectSize((long)NUM_BYTES_ARRAY_HEADER + 8L * (long)arr.length);
    }

    public static long sizeOf(String[] arr) {
        long size = shallowSizeOf((Object[])arr);

        for(String s : arr) {
            if (s != null) {
                size += sizeOf(s);
            }
        }

        return size;
    }

    public static long sizeOf(String s) {
        if (s == null) {
            return 0L;
        } else {
            long size = (long)STRING_SIZE + (long)NUM_BYTES_ARRAY_HEADER + 2L * (long)s.length();
            return alignObjectSize(size);
        }
    }
    static {
        Map<Class<?>, Integer> primitiveSizesMap = new IdentityHashMap();
        primitiveSizesMap.put(Boolean.TYPE, 1);
        primitiveSizesMap.put(Byte.TYPE, 1);
        primitiveSizesMap.put(Character.TYPE, 2);
        primitiveSizesMap.put(Short.TYPE, 2);
        primitiveSizesMap.put(Integer.TYPE, 4);
        primitiveSizesMap.put(Float.TYPE, 4);
        primitiveSizesMap.put(Double.TYPE, 8);
        primitiveSizesMap.put(Long.TYPE, 8);
        primitiveSizes = Collections.unmodifiableMap(primitiveSizesMap);
        if (Constants.JRE_IS_64BIT) {
            boolean compressedOops = false;
            int objectAlignment = 8;
            boolean isHotspot = false;

            try {
                Class<?> beanClazz = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
                Object hotSpotBean = Class.forName("java.lang.management.ManagementFactory").getMethod("getPlatformMXBean", Class.class).invoke((Object)null, beanClazz);
                if (hotSpotBean != null) {
                    isHotspot = true;
                    Method getVMOptionMethod = beanClazz.getMethod("getVMOption", String.class);

                    try {
                        Object vmOption = getVMOptionMethod.invoke(hotSpotBean, "UseCompressedOops");
                        compressedOops = Boolean.parseBoolean(vmOption.getClass().getMethod("getValue").invoke(vmOption).toString());
                    } catch (RuntimeException | ReflectiveOperationException var8) {
                        isHotspot = false;
                    }

                    try {
                        Object vmOption = getVMOptionMethod.invoke(hotSpotBean, "ObjectAlignmentInBytes");
                        objectAlignment = Integer.parseInt(vmOption.getClass().getMethod("getValue").invoke(vmOption).toString());
                    } catch (RuntimeException | ReflectiveOperationException var7) {
                        isHotspot = false;
                    }
                }
            } catch (RuntimeException | ReflectiveOperationException var9) {
                    log.warn("Lucene cannot correctly calculate object sizes on 64bit JVMs that are not based on Hotspot or a compatible implementation.");
            }

            JVM_IS_HOTSPOT_64BIT = isHotspot;
            COMPRESSED_REFS_ENABLED = compressedOops;
            NUM_BYTES_OBJECT_ALIGNMENT = objectAlignment;
            NUM_BYTES_OBJECT_REF = COMPRESSED_REFS_ENABLED ? 4 : 8;
            NUM_BYTES_OBJECT_HEADER = 8 + NUM_BYTES_OBJECT_REF;
            NUM_BYTES_ARRAY_HEADER = (int)alignObjectSize((long)(NUM_BYTES_OBJECT_HEADER + 4));
        } else {
            JVM_IS_HOTSPOT_64BIT = false;
            COMPRESSED_REFS_ENABLED = false;
            NUM_BYTES_OBJECT_ALIGNMENT = 8;
            NUM_BYTES_OBJECT_REF = 4;
            NUM_BYTES_OBJECT_HEADER = 8;
            NUM_BYTES_ARRAY_HEADER = NUM_BYTES_OBJECT_HEADER + 4;
        }

        INTEGER_SIZE = (int)shallowSizeOfInstance(Integer.class);
        LONG_SIZE = (int)shallowSizeOfInstance(Long.class);
        STRING_SIZE = (int)shallowSizeOfInstance(String.class);
        HASHTABLE_RAM_BYTES_PER_ENTRY = 2L * (long)NUM_BYTES_OBJECT_REF * 2L;
        LINKED_HASHTABLE_RAM_BYTES_PER_ENTRY = HASHTABLE_RAM_BYTES_PER_ENTRY + 2L * (long)NUM_BYTES_OBJECT_REF;
    }

}
