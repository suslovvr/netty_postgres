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
//import org.elasticsearch.common.io.stream.StreamOutput;

import java.util.List;

/**
 * granularity of references
 * <p>
 * order from large-grained to fine-grained:
 * <p>
 * CLUSTER &gt; PARTITION &gt; NODE &gt; SHARD &gt; DOC
 */
public enum RowGranularity {

    // a higher ordinal represents a higher granularity, so order matters here
    CLUSTER,
    PARTITION,
    NODE,
    SHARD,
    DOC;

    private static final List<RowGranularity> VALUES = List.of(values());
/*
    public static RowGranularity fromStream(StreamInput in) throws IOException {
        return VALUES.get(in.readVInt());
    }

    public static void toStream(RowGranularity granularity, StreamOutput out) throws IOException {
        out.writeVInt(granularity.ordinal());
    }

 */
}
