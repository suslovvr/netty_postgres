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

package ru.sber.df.epmp.netty_postgres.server.postgres.exceptions;

import ru.sber.df.epmp.netty_postgres.server.postgres.metadata.RelationName;

/**
 * Defines that an exception is thrown while operating on table level operations
 * (e.g. creating, selecting from a table).
 */
public interface TableScopeException //extends CrateException
{
/*
    @Override
    default <C, R> R accept(CrateExceptionVisitor<C, R> exceptionVisitor, C context) {
        return exceptionVisitor.visitTableScopeException(this, context);
    }

 */
    Iterable<RelationName> getTableIdents();
}
