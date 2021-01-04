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

package ru.art.logging;

import lombok.*;
import java.util.*;

@Getter
@Builder
public class ServiceCallLoggingParameters {
    private final String serviceId;
    private final String serviceMethodId;
    private final String serviceMethodCommand;
    private final String logEventType;
    private final Set<String> loadedServices;
}
