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

package ru.sber.df.epmp.netty_postgres.server.postgres.types;

//import io.crate.Streamer;
//import org.apache.lucene.util.RamUsageEstimator;

import ru.sber.df.epmp.netty_postgres.server.postgres.util.RamUsageEstimator;

import java.util.List;

public class OidVectorType extends DataType<List<Integer>> {

    public static final String NAME = "oidvector";
    public static final int ID = 21;

    @Override
    public int compare(List<Integer> o1, List<Integer> o2) {
        return DataTypes.INTEGER_ARRAY.compare(o1, o2);
    }

    @Override
    public int id() {
        return ID;
    }

    @Override
    public Precedence precedence() {
        return Precedence.ARRAY;
    }

    @Override
    public String getName() {
        return NAME;
    }
/*
    @Override
    public Streamer<List<Integer>> streamer() {
        return DataTypes.INTEGER_ARRAY.streamer();
    }

 */
    @SuppressWarnings("unchecked")
    @Override
    public List<Integer> implicitCast(Object value) throws IllegalArgumentException, ClassCastException {
        return (List<Integer>) value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Integer> sanitizeValue(Object value) {
        return (List<Integer>) value;
    }

    @Override
    public long valueBytes(List<Integer> value) {
        if (value == null) {
            return RamUsageEstimator.NUM_BYTES_OBJECT_HEADER;
        }
        return (long) value.size() * IntegerType.INTEGER_SIZE;
    }
}
