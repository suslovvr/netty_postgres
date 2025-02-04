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

package ru.sber.df.epmp.netty_postgres.server.postgres.metadata;

//import org.elasticsearch.common.io.stream.StreamInput;

import java.util.List;
import java.util.Locale;

public enum IndexType {
    FULLTEXT,
    PLAIN,
    NONE;

    private static final List<IndexType> VALUES = List.of(values());
/*
    static IndexType fromStream(StreamInput in) throws IOException {
        return VALUES.get(in.readVInt());
    }

 */
    public static IndexType of(String indexMethod) {
        return switch (indexMethod.toLowerCase(Locale.ENGLISH)) {
            case "fulltext" -> IndexType.FULLTEXT;
            case "off" -> IndexType.NONE;
            case "plain" -> IndexType.PLAIN;
            default -> IndexType.PLAIN;
        };
    }
}
