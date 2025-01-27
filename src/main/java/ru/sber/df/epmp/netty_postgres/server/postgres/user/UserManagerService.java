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

import ru.sber.df.epmp.netty_postgres.server.postgres.action.sql.Sessions;
import ru.sber.df.epmp.netty_postgres.server.postgres.auth.AccessControl;
import ru.sber.df.epmp.netty_postgres.server.postgres.auth.AccessControlImpl;
import ru.sber.df.epmp.netty_postgres.server.postgres.exceptions.UserAlreadyExistsException;
import ru.sber.df.epmp.netty_postgres.server.postgres.exceptions.UserUnknownException;
//import ru.sber.df.epmp.netty_postgres.server.postgres.execution.engine.collect.sources.SysTableRegistry;
//import ru.sber.df.epmp.netty_postgres.server.postgres.metadata.cluster.DDLClusterStateService;
import ru.sber.df.epmp.netty_postgres.server.postgres.metadata.settings.CoordinatorSessionSettings;
import ru.sber.df.epmp.netty_postgres.server.postgres.statistics.TableStats;
//import ru.sber.df.epmp.netty_postgres.server.postgres.user.metadata.SysPrivilegesTableInfo;
//import ru.sber.df.epmp.netty_postgres.server.postgres.user.metadata.SysUsersTableInfo;
/*
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.Singleton;
 */
import ru.sber.df.epmp.netty_postgres.server.postgres.common.inject.Singleton;
import ru.sber.df.epmp.netty_postgres.server.postgres.common.inject.Inject;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Singleton
public class UserManagerService implements UserManager {

    private static final Consumer<User> ENSURE_DROP_USER_NOT_SUPERUSER = user -> {
        if (user != null && user.isSuperUser()) {
            throw new UnsupportedOperationException(String.format(
                Locale.ENGLISH, "Cannot drop a superuser '%s'", user.name()));
        }
    };

    private static final Consumer<User> ENSURE_PRIVILEGE_USER_NOT_SUPERUSER = user -> {
        if (user != null && user.isSuperUser()) {
            throw new UnsupportedOperationException(String.format(
                Locale.ENGLISH, "Cannot alter privileges for superuser '%s'", user.name()));
        }
    };
/*
    private static final UserManagerDDLModifier DDL_MODIFIER = new UserManagerDDLModifier();

    private final TransportCreateUserAction transportCreateUserAction;
    private final TransportDropUserAction transportDropUserAction;
    private final TransportAlterUserAction transportAlterUserAction;
    private final TransportPrivilegesAction transportPrivilegesAction;
*/
    private UserLookup userLookup;
//-----------------------------
    private static UserManagerService instance=null;
    public synchronized static UserManagerService getInstance() {
        if(instance == null){
            instance = new UserManagerService(UserLookupService.getInstance());
        }
        return instance;
    }
//------------------------------
    @Inject
    public UserManagerService(
//            TransportCreateUserAction transportCreateUserAction,
//                              TransportDropUserAction transportDropUserAction,
//                              TransportAlterUserAction transportAlterUserAction,
//                              TransportPrivilegesAction transportPrivilegesAction,
//                              SysTableRegistry sysTableRegistry,
//                              ClusterService clusterService,
                              UserLookup userLookup //,
//                              DDLClusterStateService ddlClusterStateService
    ) {
        this.userLookup = userLookup;
/*
        this.transportCreateUserAction = transportCreateUserAction;
        this.transportDropUserAction = transportDropUserAction;
        this.transportAlterUserAction = transportAlterUserAction;
        this.transportPrivilegesAction = transportPrivilegesAction;
        this.userLookup = userLookup;
        var userTable = SysUsersTableInfo.create();
        sysTableRegistry.registerSysTable(
            userTable,
            () -> CompletableFuture.completedFuture(userLookup.users()),
            userTable.expressions(),
            false
        );

        var privilegesTable = SysPrivilegesTableInfo.create();
        sysTableRegistry.registerSysTable(
            privilegesTable,
            () -> CompletableFuture.completedFuture(SysPrivilegesTableInfo.buildPrivilegesRows(userLookup.users())),
            privilegesTable.expressions(),
            false
        );

        ddlClusterStateService.addModifier(DDL_MODIFIER);

 */
    }

/*
    @Override
    public CompletableFuture<Long> createUser(String userName, @Nullable SecureHash hashedPw) {
        return transportCreateUserAction.execute(new CreateUserRequest(userName, hashedPw), r -> {
            if (r.doesUserExist()) {
                throw new UserAlreadyExistsException(userName);
            }
            return 1L;
        });

    }

    @Override
    public CompletableFuture<Long> dropUser(String userName, boolean suppressNotFoundError) {
        ENSURE_DROP_USER_NOT_SUPERUSER.accept(userLookup.findUser(userName));
        return transportDropUserAction.execute(new DropUserRequest(userName, suppressNotFoundError), r -> {
            if (r.doesUserExist() == false) {
                if (suppressNotFoundError) {
                    return 0L;
                }
                throw new UserUnknownException(userName);
            }
            return 1L;
        });
    }

    @Override
    public CompletableFuture<Long> alterUser(String userName, @Nullable SecureHash newHashedPw) {
        return transportAlterUserAction.execute(new AlterUserRequest(userName, newHashedPw), r -> {
            if (r.doesUserExist() == false) {
                throw new UserUnknownException(userName);
            }
            return 1L;
        });
    }

    @Override
    public CompletableFuture<Long> applyPrivileges(Collection<String> userNames, Collection<Privilege> privileges) {
        userNames.forEach(s -> ENSURE_PRIVILEGE_USER_NOT_SUPERUSER.accept(userLookup.findUser(s)));
        return transportPrivilegesAction.execute(new PrivilegesRequest(userNames, privileges), r -> {
            if (!r.unknownUserNames().isEmpty()) {
                throw new UserUnknownException(r.unknownUserNames());
            }
            return r.affectedRows();
        });
    }
 */
    @Override
    public AccessControl getAccessControl(CoordinatorSessionSettings sessionSettings) {
        return new AccessControlImpl(userLookup, sessionSettings);
    }

    public Iterable<User> users() {
        return userLookup.users();
    }
}
