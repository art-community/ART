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
    implementation(project(":configurator"))
    implementation(project(":model"))
    implementation(project(":core"))
    implementation(project(":logging"))
    implementation(project(":value"))
    implementation(project(":scheduler"))
    implementation(project(":json"))
    implementation(project(":yaml"))
    implementation(project(":protobuf"))
    implementation(project(":message-pack"))
    implementation(project(":xml"))
    implementation(project(":transport"))
    implementation(project(":server"))
    implementation(project(":communicator"))
    implementation(project(":rsocket"))
    implementation(project(":http"))
    implementation(project(":http"))
    implementation(project(":tarantool"))
    implementation(project(":rocks-db"))
    implementation(project(":storage"))
    implementation(project(":resilience"))
}
