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

package io.art.http.refresher;

import io.art.core.changes.*;
import io.art.core.module.*;
import lombok.*;
import lombok.experimental.*;
import static io.art.core.changes.ChangesListener.*;
import static io.art.core.changes.ChangesListenerRegistry.*;

@Getter
@Accessors(fluent = true)
public class HttpModuleRefresher implements ModuleRefresher {
    private final ChangesListener serverListener = changesListener();
    private final ChangesListener serverLoggingListener = changesListener();
    private final ChangesListenerRegistry connectorListeners = changesListenerRegistry();
    private final ChangesListenerRegistry connectorLoggingListeners = changesListenerRegistry();
    private final Consumer consumer = new Consumer();

    public HttpModuleRefresher produce() {
        serverListener.produce();
        serverLoggingListener.produce();
        connectorListeners.produce();
        connectorLoggingListeners.produce();
        return this;
    }

    @Getter
    @Accessors(fluent = true)
    public class Consumer {
        private final ChangesConsumer serverConsumer = serverListener.consumer();
        private final ChangesConsumer serverLoggingConsumer = serverLoggingListener.consumer();
        private final ChangesConsumerRegistry connectorConsumers = connectorListeners.getConsumers();
        private final ChangesConsumerRegistry connectorLoggingConsumers = connectorLoggingListeners.getConsumers();
    }
}
