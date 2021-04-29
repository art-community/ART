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

package io.art.model.modeling.communicator;

import io.art.communicator.action.CommunicatorAction.*;
import io.art.communicator.constants.*;
import io.art.core.collection.*;
import static io.art.core.checker.EmptinessChecker.*;
import static io.art.core.checker.NullityChecker.*;
import java.util.*;
import java.util.function.*;

public interface CommunicatorModel {
    String getId();

    Class<?> getCommunicatorInterface();

    String getTargetServiceId();

    CommunicatorModuleConstants.CommunicatorProtocol getProtocol();

    BiFunction<String, CommunicatorActionBuilder, CommunicatorActionBuilder> getDecorator();

    ImmutableMap<String, CommunicatorActionModel> getActions();

    default Optional<CommunicatorActionModel> getActionByName(String name) {
        return getActions().values().stream().filter(action -> action.getName().equals(name)).findFirst();
    }

    default CommunicatorActionBuilder implement(String id, CommunicatorActionBuilder current) {
        ImmutableMap<String, CommunicatorActionModel> actions = getActions();
        if (isEmpty(actions)) {
            return let(getDecorator(), decorator -> decorator.apply(id, current));
        }
        return let(actions.get(id), methodModel -> methodModel.implement(current));
    }
}
