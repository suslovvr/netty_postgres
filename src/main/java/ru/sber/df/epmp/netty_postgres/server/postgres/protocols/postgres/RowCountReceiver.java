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

package ru.sber.df.epmp.netty_postgres.server.postgres.protocols.postgres;

import ru.sber.df.epmp.netty_postgres.server.postgres.action.sql.BaseResultReceiver;
import ru.sber.df.epmp.netty_postgres.server.postgres.auth.AccessControl;
import ru.sber.df.epmp.netty_postgres.server.postgres.data.Row;
import ru.sber.df.epmp.netty_postgres.server.postgres.protocols.postgres.DelayableWriteChannel.DelayedWrites;
import io.netty.channel.ChannelFuture;
import org.jetbrains.annotations.NotNull;

class RowCountReceiver extends BaseResultReceiver {

    private final DelayableWriteChannel channel;
    private final String query;
    private final AccessControl accessControl;
    private final DelayedWrites delayedWrites;
    private long rowCount;

    RowCountReceiver(String query,
                     DelayableWriteChannel channel,
                     DelayedWrites delayedWrites,
                     AccessControl accessControl) {
        this.query = query;
        this.channel = channel;
        this.delayedWrites = delayedWrites;
        this.accessControl = accessControl;
    }

    @Override
    public void setNextRow(Row row) {
        rowCount = (long) row.get(0);
        /*
         * In Crate -1 means row-count unknown, and -2 means error. In JDBC -2 means row-count unknown and -3 means error.
         * See {@link java.sql.Statement#EXECUTE_FAILED}
         */
        if (rowCount < 0) {
            rowCount--;
        }
    }

    @Override
    public void allFinished() {
        ChannelFuture sendCommandComplete = Messages.sendCommandComplete(channel.bypassDelay(), query, rowCount);
        channel.writePendingMessages(delayedWrites);
        channel.flush();
        sendCommandComplete.addListener(f -> super.allFinished());
    }

    @Override
    public void fail(@NotNull Throwable throwable) {
        ChannelFuture sendErrorResponse = Messages.sendErrorResponse(channel.bypassDelay(), accessControl, throwable);
        channel.writePendingMessages(delayedWrites);
        channel.flush();
        sendErrorResponse.addListener(f -> super.fail(throwable));
    }
}
