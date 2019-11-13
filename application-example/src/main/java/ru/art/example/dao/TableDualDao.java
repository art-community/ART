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

package ru.art.example.dao;

import org.jooq.*;
import static org.jooq.impl.DSL.*;
import static ru.art.core.extension.StringExtensions.*;
import static ru.art.example.constants.ExampleAppModuleConstants.SqlConstants.*;
import static ru.art.sql.module.SqlModule.*;

/**
 * Dao is made for exchanging data with database
 */
public class TableDualDao {

    public static String testQuery() {
        Result<?> testQueryValue = using(sqlModule().getJooqConfiguration())
                .select(ONE)
                .from(TABLE_DUAL)
                .maxRows(1)
                .fetch();

        return emptyIfNull(testQueryValue.getValue(0, ONE));
    }
}