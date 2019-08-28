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

package ru.art.config.extensions.sql;

import lombok.experimental.*;
import ru.art.config.*;
import ru.art.core.annotation.*;
import static ru.art.config.extensions.ConfigExtensions.*;
import static ru.art.config.extensions.sql.SqlConfigKeys.*;

@PublicApi
@UtilityClass
public class SqlConfigProvider {
    public static Config sqlDbConfig() {
        return configInner(SQL_DB_SECTION_ID);
    }
}
