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



        project(":Core()
        project(":Entity()
        project(":Logging()
        project(":Service()
        project(":ReactiveService()
        project(":Protobuf()
        project(":Json()
        project(":Xml()
        project(":MessagePack()
    }
}

dependencies {

        api("io.rsocket", "rsocket-core", rsocketVersion)
                .exclude("io.netty")
                .exclude("io.projectreactor", "reactor-core")
                .exclude("org.slf4j")
        api("io.rsocket", "rsocket-transport-netty", rsocketVersion)
                .exclude("io.netty")
                .exclude("io.projectreactor", "reactor-core")
                .exclude("org.slf4j")
        api("io.netty", "netty-all", nettyVersion)
                .exclude("org.slf4j")
    }
}
