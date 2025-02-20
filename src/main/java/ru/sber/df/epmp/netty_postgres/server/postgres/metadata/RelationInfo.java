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

import ru.sber.df.epmp.netty_postgres.server.postgres.expression.symbol.Symbol;
import ru.sber.df.epmp.netty_postgres.server.postgres.metadata.table.Operation;
import ru.sber.df.epmp.netty_postgres.server.postgres.sql.tree.CheckConstraint;
//import org.elasticsearch.common.settings.Settings;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Base interface for tables (..io.crate.metadata.table.TableInfo) and views (.. io.crate.metadata.view.ViewInfo).
 */
public interface RelationInfo extends Iterable<Reference> {

    enum RelationType {
        BASE_TABLE("BASE TABLE"),
        VIEW("VIEW");

        private final String prettyName;

        RelationType(String prettyName) {
            this.prettyName = prettyName;
        }

        public String pretty() {
            return this.prettyName;
        }
    }

    /**
     * returns the top level columns of this table with predictable order
     */
    Collection<Reference> columns();

    default Collection<Reference> droppedColumns() {
        return List.of();
    }

    RowGranularity rowGranularity();

    RelationName ident();

    List<ColumnIdent> primaryKey();

    default List<CheckConstraint<Symbol>> checkConstraints() {
        return List.of();
    }
/*
    Settings parameters();

 */
    Set<Operation> supportedOperations();

    RelationType relationType();
}
