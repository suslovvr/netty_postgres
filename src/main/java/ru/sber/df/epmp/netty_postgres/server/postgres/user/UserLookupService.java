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

package ru.sber.df.epmp.netty_postgres.server.postgres.user;

import ru.sber.df.epmp.netty_postgres.server.postgres.user.metadata.UsersMetadata;
import ru.sber.df.epmp.netty_postgres.server.postgres.user.metadata.UsersPrivilegesMetadata;
/*
import org.elasticsearch.cluster.ClusterChangedEvent;
import org.elasticsearch.cluster.ClusterStateListener;
import org.elasticsearch.cluster.metadata.Metadata;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;

 */
import org.jetbrains.annotations.Nullable;
import ru.sber.df.epmp.netty_postgres.server.postgres.common.inject.Singleton;
import ru.sber.df.epmp.netty_postgres.server.postgres.common.inject.Inject;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UserLookupService implements UserLookup//, ClusterStateListener
{

    private volatile Set<User> users = Set.of(User.SUPER_USER,User.CRATE_USER);

    @Inject
    public UserLookupService(
////            ClusterService clusterService
    ) {
//        clusterService.addListener(this);
    }
    //-----------------------------
    private static UserLookupService instance=null;
    public synchronized static UserLookupService getInstance() {
        if(instance == null){
            instance = new UserLookupService();
        }
        return instance;
    }
    //------------------------------
    @Override
    public Iterable<User> users() {
        return users;
    }
/*
    @Override
    public void clusterChanged(ClusterChangedEvent event) {
        Metadata prevMetadata = event.previousState().metadata();
        Metadata newMetadata = event.state().metadata();

        UsersMetadata prevUsers = prevMetadata.custom(UsersMetadata.TYPE);
        UsersMetadata newUsers = newMetadata.custom(UsersMetadata.TYPE);

        UsersPrivilegesMetadata prevUsersPrivileges = prevMetadata.custom(UsersPrivilegesMetadata.TYPE);
        UsersPrivilegesMetadata newUsersPrivileges = newMetadata.custom(UsersPrivilegesMetadata.TYPE);

        if (prevUsers != newUsers || prevUsersPrivileges != newUsersPrivileges) {
            users = getUsers(newUsers, newUsersPrivileges);
        }
    }

 */


    static Set<User> getUsers(@Nullable UsersMetadata metadata,
                              @Nullable UsersPrivilegesMetadata privilegesMetadata) {
        HashSet<User> users = new HashSet<>();
        users.add(User.CRATE_USER);
        users.add(User.SUPER_USER);
        if (metadata != null) {
            for (Map.Entry<String, SecureHash> user: metadata.users().entrySet()) {
                String userName = user.getKey();
                SecureHash password = user.getValue();
                Set<Privilege> privileges = null;
                if (privilegesMetadata != null) {
                    privileges = privilegesMetadata.getUserPrivileges(userName);
                    if (privileges == null) {
                        // create empty set
                        privilegesMetadata.createPrivileges(userName, Set.of());
                    }
                }
                users.add(User.of(userName, privileges, password));
            }
        }
        return Collections.unmodifiableSet(users);
    }
}
