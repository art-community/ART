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

rootProject.name = "art-java"
include("core")
include("model")
include("value")
include("logging")
include("transport")
include("resilience")
include("launcher")
include("configurator")
include("yaml-configuration")
include("json")
include("yaml")
include("protobuf")
include("xml")
include("message-pack")
include("server")
include("communicator")
include("rsocket")
include("http")
include("template-engine")
include("tarantool")
include("tests")
include("scheduler")
include("graal")
include("storage")
include("rocks-db")
include("meta")
//include("metrics")
//include("metrics-http")
//include("kafka-client")
//include("kafka-consumer")
//include("kafka-producer")
//include("soap")
//include("soap-client")
//include("soap-server")
//include("sql")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url = uri("https://nexus.art-platform.io/repository/art-gradle-plugins/") }
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.contains("art")) {
                useModule("io.art.gradle:art-gradle:main")
            }
        }
    }
}
