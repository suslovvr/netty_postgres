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

package ru.sber.df.epmp.netty_postgres.server.postgres.expression.symbol;

import ru.sber.df.epmp.netty_postgres.server.postgres.metadata.IndexType;
import ru.sber.df.epmp.netty_postgres.server.postgres.metadata.ReferenceIdent;
import ru.sber.df.epmp.netty_postgres.server.postgres.metadata.RowGranularity;
import ru.sber.df.epmp.netty_postgres.server.postgres.metadata.SimpleReference;
import ru.sber.df.epmp.netty_postgres.server.postgres.sql.tree.ColumnPolicy;
import ru.sber.df.epmp.netty_postgres.server.postgres.types.DataType;
import ru.sber.df.epmp.netty_postgres.server.postgres.types.DataTypes;
//import org.elasticsearch.common.io.stream.StreamInput;


public class DynamicReference extends SimpleReference {
/*
    public DynamicReference(StreamInput in) throws IOException {
        super(in);
    }

 */
    public DynamicReference(ReferenceIdent ident, RowGranularity granularity, int position) {
        super(ident, granularity, DataTypes.UNDEFINED, position, null);
    }

    public DynamicReference(ReferenceIdent ident, RowGranularity granularity, ColumnPolicy columnPolicy, int position) {
        super(ident, granularity, DataTypes.UNDEFINED, columnPolicy, IndexType.PLAIN, true, false, position, 0, false, null);
    }

    @Override
    public SymbolType symbolType() {
        return SymbolType.DYNAMIC_REFERENCE;
    }
/*
    @Override
    public <C, R> R accept(SymbolVisitor<C, R> visitor, C context) {
        return visitor.visitDynamicReference(this, context);
    }
*/
    public void valueType(DataType<?> targetType) {
        type = targetType;
    }
}
