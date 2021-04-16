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
    val sl4jVersion: String by project
    val commonsLoggingVersion: String by project
    val log4jVersion: String by project
    val jacksonVersion: String by project
    val disruptorVersion: String by project

    implementation(project(":core"))
    implementation(project(":value"))

    api("org.slf4j", "slf4j-api", sl4jVersion)
    api("org.slf4j", "log4j-over-slf4j", sl4jVersion)
    api("org.apache.logging.log4j", "log4j-slf4j-impl", log4jVersion)

    api("commons-logging", "commons-logging", commonsLoggingVersion)
    api("org.apache.logging.log4j", "log4j-jcl", log4jVersion)

    api("org.apache.logging.log4j", "log4j-api", log4jVersion)
    api("org.apache.logging.log4j", "log4j-core", log4jVersion)

    api("org.apache.logging.log4j", "log4j-jul", log4jVersion)
    api("com.lmax", "disruptor", disruptorVersion)

    api("com.fasterxml.jackson.dataformat", "jackson-dataformat-yaml", jacksonVersion)
    api("com.fasterxml.jackson.core", "jackson-core", jacksonVersion)
    api("com.fasterxml.jackson.core", "jackson-databind", jacksonVersion)
}
