import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import ru.art.gradle.constants.lombok

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
    testModules {
        kit()
        kafkaBroker()
    }
    spockFramework()
}

configurations {
    with(testRuntimeClasspath.get()) {
        exclude("org.apache.logging.log4j")
    }
}

dependencies {
    annotationProcessor(lombok().inGradleNotation())
    testAnnotationProcessor(lombok().inGradleNotation())
    testImplementation("org.hsqldb", "hsqldb", "2+")
}

tasks.withType<Test> {
    testLogging {
        events = setOf(PASSED, FAILED, SKIPPED)
        exceptionFormat = FULL
    }
}