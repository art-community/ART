/*
 * ART
 *
 * Copyright 2020 ART
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

package io.art.service.state;

import lombok.*;
import io.art.core.module.*;
import io.art.entity.Value;
import io.art.service.registry.*;
import static java.util.Optional.*;
import java.util.*;

public class ServiceModuleState implements ModuleState {
    private final ThreadLocal<Value> requestValue = new ThreadLocal<>();

    @Getter
    private final ServiceRegistry serviceRegistry = new ServiceRegistry();

    public Optional<Value> getRequestValue() {
        return ofNullable(requestValue.get());
    }

    public void setRequestValue(Value value) {
        requestValue.set(value);
    }
}