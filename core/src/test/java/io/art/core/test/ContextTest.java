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

package io.art.core.test;

import io.art.core.configuration.*;
import io.art.core.context.*;
import io.art.core.module.*;
import org.junit.jupiter.api.*;
import static io.art.core.constants.EmptyFunctions.*;
import static io.art.core.context.Context.*;
import static io.art.core.factory.SetFactory.*;
import static org.junit.jupiter.api.Assertions.*;

public class ContextTest {
    private static class TestModuleConfiguration implements ModuleConfiguration {
        private static class Configurator implements ModuleConfigurator<TestModuleConfiguration, Configurator> {
        }
    }

    private static class TestModule implements StatelessModule<TestModuleConfiguration, TestModuleConfiguration.Configurator> {
        private volatile boolean unloaded;

        @Override
        public String getId() {
            return "test";
        }

        @Override
        public TestModuleConfiguration.Configurator getConfigurator() {
            return null;
        }

        @Override
        public TestModuleConfiguration getConfiguration() {
            return null;
        }

        @Override
        public void onUnload(Context.Service contextService) {
            StatelessModule.super.onUnload(contextService);
            unloaded = true;
        }
    }

    @Test
    public void testContextShutdown() {
        prepareInitialization(ContextConfiguration.defaults(), emptyConsumer());
        TestModule testModule = new TestModule();
        processInitialization(immutableSetOf(testModule));
        shutdown();
        assertTrue(testModule.unloaded);
    }

}
