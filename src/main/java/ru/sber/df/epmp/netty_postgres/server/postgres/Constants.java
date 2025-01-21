/*
 * Licensed to Crate.io GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial agreement.
 */

package ru.sber.df.epmp.netty_postgres.server.postgres;

public class Constants {

    public static final String DB_NAME = "test";//"crate";
    public static final int DB_OID = 0;

//    public static final String ISSUE_URL = "https://github.com/crate/crate/issues/new/choose";

    // Mapping Type that contains table definitions
    public static final String DEFAULT_MAPPING_TYPE = "default";

    public static final String DEFAULT_DATE_STYLE = "ISO";

    public static final int MAX_SHARD_MISSING_RETRIES = 3;
//-------------------------------------------------
public static final String JVM_VENDOR = System.getProperty("java.vm.vendor");
    public static final String JVM_NAME = System.getProperty("java.vm.name");
    /** @deprecated */
    @Deprecated
    public static final String JVM_VERSION = Runtime.version().toString();
    /** @deprecated */
    @Deprecated
    public static final String JVM_SPEC_VERSION = Integer.toString(Runtime.version().feature());
    /** @deprecated */
    @Deprecated
    public static final String JAVA_VERSION = System.getProperty("java.version");
    public static final String OS_NAME = System.getProperty("os.name");
    public static final boolean LINUX;
    public static final boolean WINDOWS;
    public static final boolean SUN_OS;
    public static final boolean MAC_OS_X;
    public static final boolean FREE_BSD;
    public static final String OS_ARCH;
    public static final String OS_VERSION;
    public static final String JAVA_VENDOR;
    public static final boolean JRE_IS_64BIT;
    /** @deprecated */
    @Deprecated
    public static final boolean JRE_IS_MINIMUM_JAVA8 = true;
    /** @deprecated */
    @Deprecated
    public static final boolean JRE_IS_MINIMUM_JAVA9 = true;
    /** @deprecated */
    @Deprecated
    public static final boolean JRE_IS_MINIMUM_JAVA11 = true;

    private Constants() {
    }


    //-------------------------------------------------

    static {
        LINUX = OS_NAME.startsWith("Linux");
        WINDOWS = OS_NAME.startsWith("Windows");
        SUN_OS = OS_NAME.startsWith("SunOS");
        MAC_OS_X = OS_NAME.startsWith("Mac OS X");
        FREE_BSD = OS_NAME.startsWith("FreeBSD");
        OS_ARCH = System.getProperty("os.arch");
        OS_VERSION = System.getProperty("os.version");
        JAVA_VENDOR = System.getProperty("java.vendor");
        boolean is64Bit = false;
        String datamodel = null;

        try {
            datamodel = System.getProperty("sun.arch.data.model");
            if (datamodel != null) {
                is64Bit = datamodel.contains("64");
            }
        } catch (SecurityException var3) {
        }

        if (datamodel == null) {
            if (OS_ARCH != null && OS_ARCH.contains("64")) {
                is64Bit = true;
            } else {
                is64Bit = false;
            }
        }

        JRE_IS_64BIT = is64Bit;
    }
}
