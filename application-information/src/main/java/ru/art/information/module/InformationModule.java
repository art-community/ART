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

package ru.art.information.module;

import lombok.*;
import ru.art.core.module.Module;
import ru.art.core.module.*;
import ru.art.information.configuration.*;
import static lombok.AccessLevel.PRIVATE;
import static ru.art.core.context.Context.*;
import static ru.art.information.configuration.InformationModuleConfiguration.*;
import static ru.art.information.constants.InformationModuleConstants.*;

@Getter
public class InformationModule implements Module<InformationModuleConfiguration, ModuleState> {
    private final String id = INFORMATION_MODULE_ID;
    private final InformationModuleConfiguration defaultConfiguration = new InformationModuleDefaultConfiguration();
    @Getter(lazy = true, value = PRIVATE)
    private final static InformationModuleConfiguration informationModule = context().getModule(INFORMATION_MODULE_ID, InformationModule::new);

    public static InformationModuleConfiguration informationModule() {
        return getInformationModule();
    }
}
