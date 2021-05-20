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

dependencies {
    val guavaVersion: String by project
    val vavrVersion: String by project
    val nettyVersion: String by project
    val reactorVersion: String by project
    val jctoolsVersion: String by project

    api("com.google.guava", "guava", guavaVersion)
            .exclude("org.slf4j")
    api("io.vavr", "vavr", vavrVersion)
            .exclude("org.slf4j")
    api("io.netty", "netty-buffer", nettyVersion)
            .exclude("com.google.guava")
            .exclude("org.slf4j")
    api("io.projectreactor", "reactor-core", reactorVersion)
            .exclude("com.google.guava")
            .exclude("org.slf4j")
    api("org.jctools", "jctools-core", jctoolsVersion)
            .exclude("com.google.guava")
            .exclude("org.slf4j")
}
