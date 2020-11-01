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

dependencies {
    val rsocketVersion: String by project
    val nettyVersion: String by project
    val reactorNettyVersion: String by project

    implementation(project(":core"))
    implementation(project(":value"))
    implementation(project(":logging"))
    implementation(project(":server"))
    implementation(project(":communicator"))
    implementation(project(":protobuf"))
    implementation(project(":json"))
    implementation(project(":xml"))
    implementation(project(":message-pack"))

    api("io.rsocket", "rsocket-transport-netty", rsocketVersion)
            .exclude("io.netty")
            .exclude("io.projectreactor", "reactor-core")
            .exclude("org.slf4j")

    api("io.projectreactor.netty", "reactor-netty", reactorNettyVersion)
            .exclude("io.netty")
            .exclude("io.projectreactor", "reactor-core")
            .exclude("org.slf4j")

    api("io.netty", "netty-all", nettyVersion)
            .exclude("org.slf4j")
}