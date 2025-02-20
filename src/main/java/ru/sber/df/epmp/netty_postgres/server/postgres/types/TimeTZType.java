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
//import org.elasticsearch.common.io.stream.StreamInput;
//import org.elasticsearch.common.io.stream.StreamOutput;

import java.util.Locale;

import static ru.sber.df.epmp.netty_postgres.server.postgres.types.TimeTZParser.exceptionForInvalidLiteral;
import static ru.sber.df.epmp.netty_postgres.server.postgres.types.TimeTZParser.timeTZOf;

public final class TimeTZType extends DataType<TimeTZ> implements FixedWidthType//, Streamer<TimeTZ>
{

    public static final int ID = 20;
    public static final int TYPE_SIZE = 12;
    public static final String NAME = "time with time zone";
    public static final TimeTZType INSTANCE = new TimeTZType();


    @Override
    public int id() {
        return ID;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Precedence precedence() {
        return Precedence.TIMETZ;
    }
/*
    @Override
    public Streamer<TimeTZ> streamer() {
        return this;
    }

 */
    @Override
    public int compare(TimeTZ val1, TimeTZ val2) {
        return val1.compareTo(val2);
    }
/*
    @Override
    public TimeTZ readValueFrom(StreamInput in) throws IOException {
        if (in.readBoolean()) {
            return null;
        }
        return new TimeTZ(in.readLong(), in.readInt());
    }

    @Override
    public void writeValueTo(StreamOutput out, TimeTZ tz) throws IOException {
        out.writeBoolean(tz == null);
        if (tz != null) {
            out.writeLong(tz.getMicrosFromMidnight());
            out.writeInt(tz.getSecondsFromUTC());
        }
    }

 */
    @Override
    public int fixedSize() {
        return TYPE_SIZE;
    }

    @Override
    public TimeTZ implicitCast(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof TimeTZ timetz) {
            return timetz;
        } else if (value instanceof String timetzStr) {
            try {
                return TimeTZParser.parse(timetzStr);
            } catch (IllegalArgumentException e0) {
                try {
                    return timeTZOf(
                        TimeTZType.class.getSimpleName(),
                        Long.parseLong(timetzStr));
                } catch (NumberFormatException e1) {
                    throw exceptionForInvalidLiteral(value);
                }
            }
        }
        throw exceptionForInvalidLiteral(value);
    }

    @Override
    public TimeTZ sanitizeValue(Object value) {
        if (value == null) {
            return null;
        }
        return (TimeTZ) value;
    }

    @Override
    public TimeTZ valueForInsert(TimeTZ value) {
        throw new UnsupportedOperationException(String.format(
            Locale.ENGLISH,
            "%s cannot be used in insert statements",
            TimeTZType.class.getSimpleName()));
    }

    @Override
    public long valueBytes(TimeTZ value) {
        return TYPE_SIZE;
    }
}
