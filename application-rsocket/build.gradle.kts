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

art {
    providedModules {
        applicationCore()
        applicationEntity()
        applicationLogging()
        applicationService()
        applicationReactiveService()
        applicationProtobuf()
        applicationJson()
        applicationXml()
        applicationMessagePack()
    }
}

dependencies {
    with(art.externalDependencyVersionsConfiguration) {
        embedded("io.rsocket", "rsocket-core", "1.0.1")
                .exclude("io.netty")
                .exclude("io.projectreactor", "reactor-core")
                .exclude("org.slf4j")
        embedded("io.rsocket", "rsocket-transport-netty", "1.0.1")
                .exclude("io.netty")
                .exclude("io.projectreactor", "reactor-core")
                .exclude("org.slf4j")
        embedded("io.netty", "netty-all", nettyVersion)
                .exclude("org.slf4j")
    }
}
