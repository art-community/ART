/*
 * ART Java
 *
 * Copyright 2019 ART
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.art.sql.constants;

import lombok.*;

@Getter
@AllArgsConstructor
public enum DbProvider {
    POSTGRES("org.postgresql.Driver", "select 1"),
    ORACLE("oracle.jdbc.OracleDriver", "select 1 from dual"),
    MSSQL("com.microsoft.sqlserver.jdbc.SQLServerDriver", "select 1"),
    HSQLDB("org.hsqldb.jdbcDriver", "select 1 from INFORMATION_SCHEMA.SYSTEM_USERS"),
    DERBY("org.apache.derby.jdbc.ClientDriver", "values 1"),
    DERBY_EMBEDDED("org.apache.derby.jdbc.EmbeddedDriver", "values 1");

    private final String driverClassName;
    private final String validationQuery;
}
