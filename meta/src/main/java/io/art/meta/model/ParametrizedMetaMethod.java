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

package io.art.meta.model;

public class ParametrizedMetaMethod<T> extends MetaMethod<T> {
    private final MetaMethod<T> template;

    protected ParametrizedMetaMethod(MetaMethod<T> template) {
        super(template);
        this.template = template;
    }

    @Override
    public boolean isStatic() {
        return template.isStatic();
    }
}
