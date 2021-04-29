/*
 * ART
 *
 * Copyright 2019-2021 ART
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

package io.art.model.modeling.module;

import io.art.model.customizer.*;
import io.art.model.modeling.communicator.*;
import io.art.model.modeling.configurator.*;
import io.art.model.modeling.server.*;
import io.art.model.modeling.storage.StorageModuleModel;
import io.art.model.modeling.value.*;
import lombok.*;
import java.util.function.*;

@Getter
@Builder
public class ModuleModel {
    private final String mainModuleId;
    private final ConfiguratorModuleModel configuratorModel;
    private final ValueModuleModel valueModel;
    private final ServerModuleModel serverModel;
    private final CommunicatorModuleModel communicatorModel;
    private final StorageModuleModel storageModel;
    private final Runnable onLoad;
    private final Runnable onUnload;
    private final Runnable beforeReload;
    private final Runnable afterReload;

    @Builder.Default
    private ModuleCustomizer customizer = new ModuleCustomizer();

    public ModuleModel customize(UnaryOperator<ModuleCustomizer> customizer) {
        this.customizer = customizer.apply(new ModuleCustomizer());
        return this;
    }
}
