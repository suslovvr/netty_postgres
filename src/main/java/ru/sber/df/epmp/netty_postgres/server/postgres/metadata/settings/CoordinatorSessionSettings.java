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

package ru.sber.df.epmp.netty_postgres.server.postgres.metadata.settings;

import ru.sber.df.epmp.netty_postgres.server.postgres.common.unit.TimeValue;
import ru.sber.df.epmp.netty_postgres.server.postgres.metadata.SearchPath;
//import com.example.spring.netty.spring_netty.server.postgres.planner.optimizer.Rule;
import ru.sber.df.epmp.netty_postgres.server.postgres.user.User;

import static ru.sber.df.epmp.netty_postgres.server.postgres.Constants.DEFAULT_DATE_STYLE;

/**
 * A superset of {@link SessionSettings}.
 * Contains all available session settings including their setters
 *
 * <p>
 * A subset of this information can be streamed as {@link SessionSettings}
 * </p>
 */
public class CoordinatorSessionSettings extends SessionSettings {

    private final User authenticatedUser;
    private User sessionUser;
//    private Set<Class<? extends Rule<?>>> excludedOptimizerRules;
    private String applicationName;
    private String dateStyle;
    private TimeValue statementTimeout;

    public CoordinatorSessionSettings(User authenticatedUser, String ... searchPath) {
        this(authenticatedUser, authenticatedUser, searchPath);
    }

    public CoordinatorSessionSettings(User authenticatedUser, User sessionUser, String ... searchPath) {
        this(
            authenticatedUser,
            sessionUser,
            SearchPath.createSearchPathFrom(searchPath),
            true,
//            Set.of(),
            true,
            0
        );
    }

    public CoordinatorSessionSettings(User authenticatedUser,
                                      User sessionUser,
                                      SearchPath searchPath,
                                      boolean hashJoinsEnabled,
    //                                  Set<Class<? extends Rule<?>>> excludedOptimizerRules,
                                      boolean errorOnUnknownObjectKey,
                                      int memoryLimit) {
        super(authenticatedUser.name(), searchPath, hashJoinsEnabled, errorOnUnknownObjectKey, memoryLimit);
        this.authenticatedUser = authenticatedUser;
        this.sessionUser = sessionUser;
   //     this.excludedOptimizerRules = new HashSet<>(excludedOptimizerRules);
        this.dateStyle = DEFAULT_DATE_STYLE;
        this.statementTimeout = TimeValue.ZERO;
        this.memoryLimit = memoryLimit;
    }

    public User sessionUser() {
        return sessionUser;
    }

    public User authenticatedUser() {
        return authenticatedUser;
    }

    public static CoordinatorSessionSettings systemDefaults() {
        return new CoordinatorSessionSettings(User.SUPER_USER);
    }

    public void setErrorOnUnknownObjectKey(boolean newValue) {
        errorOnUnknownObjectKey = newValue;
    }

    public void setSearchPath(String ... schemas) {
        this.searchPath = SearchPath.createSearchPathFrom(schemas);
    }

    public void setSearchPath(SearchPath searchPath) {
        this.searchPath = searchPath;
    }

    public void setHashJoinEnabled(boolean newValue) {
        hashJoinsEnabled = newValue;
    }

    public void setSessionUser(User user) {
        sessionUser = user;
        userName = user.name();
    }
/*
    public Set<Class<? extends Rule<?>>> excludedOptimizerRules() {
        return excludedOptimizerRules;
    }

 */
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    @Override
    public String applicationName() {
        return this.applicationName;
    }

    public void setDateStyle(String dateStyle) {
        this.dateStyle = dateStyle;
    }

    @Override
    public String dateStyle() {
        return this.dateStyle;
    }

    public TimeValue statementTimeout() {
        return statementTimeout;
    }

    public void statementTimeout(TimeValue statementTimeout) {
        this.statementTimeout = statementTimeout;
    }

    public void memoryLimit(int memoryLimit) {
        this.memoryLimit = memoryLimit;
    }
}
