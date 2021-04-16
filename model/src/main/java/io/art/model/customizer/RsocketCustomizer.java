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

package io.art.model.customizer;

import io.art.core.annotation.*;
import io.art.rsocket.configuration.*;
import io.art.rsocket.refresher.*;
import io.art.rsocket.module.*;
import lombok.*;

@Getter
@UsedByGenerator
public class RsocketCustomizer {
    private final Custom configuration;

    public RsocketCustomizer(RsocketModule module) {
        this.configuration = new Custom(module.getRefresher());
    }

    public RsocketCustomizer activateServer() {
        configuration.activateServer = true;
        return this;
    }

    public RsocketCustomizer activateCommunicator() {
        configuration.activateCommunicator = true;
        return this;
    }

    @Getter
    public static class Custom extends RsocketModuleConfiguration {
        private boolean activateServer;
        private boolean activateCommunicator;

        public Custom(RsocketModuleRefresher refresher) {
            super(refresher);
        }
    }
}
