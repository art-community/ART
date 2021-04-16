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

package io.art.soap.server.specification;

import io.art.server.*;
import io.art.soap.server.model.*;
import static io.art.core.factory.CollectionsFactory.*;
import static io.art.soap.server.constans.SoapServerModuleConstants.*;
import java.util.*;

public interface SoapServiceSpecification extends Specification {
    SoapService getSoapService();

    @Override
    default List<String> getServiceTypes() {
        return fixedArrayOf(SOAP_SERVICE_TYPE);
    }
}
