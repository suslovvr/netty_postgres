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

package ru.sber.df.epmp.netty_postgres.server.postgres.metadata.blob;

//import ru.sber.df.epmp.netty_postgres.server.postgres.analyze.TableParameters;
//import ru.sber.df.epmp.netty_postgres.server.postgres.analyze.WhereClause;
import ru.sber.df.epmp.netty_postgres.server.postgres.metadata.*;
//import ru.sber.df.epmp.netty_postgres.server.postgres.metadata.settings.CoordinatorSessionSettings;
import ru.sber.df.epmp.netty_postgres.server.postgres.metadata.*;
import ru.sber.df.epmp.netty_postgres.server.postgres.metadata.table.Operation;
//import ru.sber.df.epmp.netty_postgres.server.postgres.metadata.table.ShardedTable;
//import ru.sber.df.epmp.netty_postgres.server.postgres.metadata.table.StoredTable;
import ru.sber.df.epmp.netty_postgres.server.postgres.metadata.table.TableInfo;
import ru.sber.df.epmp.netty_postgres.server.postgres.types.DataType;
import ru.sber.df.epmp.netty_postgres.server.postgres.types.DataTypes;
/*
import org.elasticsearch.Version;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.common.settings.Settings;
import org.jetbrains.annotations.Nullable;
*/
import java.util.*;

public class BlobTableInfo implements TableInfo//, ShardedTable, StoredTable
{

    private final RelationName ident;
    private final int numberOfShards;
    private final String numberOfReplicas;
    private final String index;
    private final LinkedHashSet<Reference> columns = new LinkedHashSet<>();
    private final String blobsPath;
//    private final TableParameters supportedTableParameters;
//    private final Settings tableParameters;
//    private final Version versionCreated;
//    private final Version versionUpgraded;
    private final boolean closed;

    private final Map<ColumnIdent, Reference> infos = new LinkedHashMap<>();
    private static final List<ColumnIdent> PRIMARY_KEY = List.of(new ColumnIdent("digest"));

    public BlobTableInfo(RelationName ident,
                         String index,
                         int numberOfShards,
                         String numberOfReplicas,
//                         Settings tableParameters,
                         String blobsPath,
//                         @Nullable Version versionCreated,
//                         @Nullable Version versionUpgraded,
                         boolean closed) {
        assert ident.indexNameOrAlias().equals(index) : "RelationName indexName must match index";
        this.ident = ident;
        this.index = index;
        this.numberOfShards = numberOfShards;
        this.numberOfReplicas = numberOfReplicas;
        this.blobsPath = blobsPath;
//        this.supportedTableParameters = TableParameters.ALTER_BLOB_TABLE_PARAMETERS;
//        this.tableParameters = tableParameters;
//        this.versionCreated = versionCreated;
//        this.versionUpgraded = versionUpgraded;
        this.closed = closed;

        addColumn("digest", DataTypes.STRING, 0);
        addColumn("last_modified", DataTypes.TIMESTAMPZ, 1);
    }

//    @Nullable
    @Override
    public Reference getReference(ColumnIdent columnIdent) {
        return infos.get(columnIdent);
    }

    @Override
    public Collection<Reference> columns() {
        return columns;
    }

    @Override
    public RowGranularity rowGranularity() {
        return RowGranularity.DOC;
    }

    @Override
    public RelationName ident() {
        return ident;
    }
/*
    @Override
    public Routing getRouting(ClusterState state,
                              RoutingProvider routingProvider,
                              WhereClause whereClause,
                              RoutingProvider.ShardSelection shardSelection,
                              CoordinatorSessionSettings sessionSettings) {
        return routingProvider.forIndices(state, new String[] { index }, Collections.emptyMap(), false, shardSelection);
    }

 */
    @Override
    public List<ColumnIdent> primaryKey() {
        return PRIMARY_KEY;
    }
/*
    @Override
    public int numberOfShards() {
        return numberOfShards;
    }

    @Override
    public String numberOfReplicas() {
        return numberOfReplicas;
    }

    @Nullable
    @Override
    public ColumnIdent clusteredBy() {
        return PRIMARY_KEY.get(0);
    }

 */
    @Override
    public Iterator<Reference> iterator() {
        return columns.iterator();
    }

    private void addColumn(String name, DataType<?> type, int position) {
        SimpleReference ref = new SimpleReference(
            new ReferenceIdent(ident(), name, null),
            RowGranularity.DOC,
            type,
            position,
            null
        );
        columns.add(ref);
        infos.put(ref.column(), ref);
    }

    public String blobsPath() {
        return blobsPath;
    }
/*
    public TableParameters tableParameters() {
        return supportedTableParameters;
    }

    public Settings parameters() {
        return tableParameters;
    }

 */
    @Override
    public Set<Operation> supportedOperations() {
        return Operation.BLOB_OPERATIONS;
    }

    @Override
    public RelationType relationType() {
        return RelationType.BASE_TABLE;
    }
/*
    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public String[] concreteIndices() {
        return new String[] { index };
    }

    @Nullable
    @Override
    public Version versionCreated() {
        return versionCreated;
    }

    @Nullable
    @Override
    public Version versionUpgraded() {
        return versionUpgraded;
    }

 */
}
