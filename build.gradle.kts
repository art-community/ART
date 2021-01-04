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

import com.jfrog.bintray.gradle.BintrayExtension.*
import com.jfrog.bintray.gradle.tasks.*
import groovy.lang.*
import org.jfrog.gradle.plugin.artifactory.dsl.*
import org.jfrog.gradle.plugin.artifactory.task.*
import ru.art.gradle.constants.DependencyConfiguration.*
import ru.art.gradle.logging.*
import ru.art.gradle.logging.LogMessageColor.*

plugins {
    `maven-publish`
    id("io.github.art.project") version "1.0.113"
    id("com.jfrog.bintray") version "1.8.4"
    id("com.jfrog.artifactory") version "4.10.0"
}

tasks.withType(Wrapper::class.java) {
    gradleVersion = "6.0"
}

val bintrayUser: String? by project
val bintrayKey: String? by project
val version: String? by project

group = "io.github.art"

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
}

subprojects {
    group = rootProject.group

    repositories {
        jcenter()
        mavenCentral()
        maven {
            url = uri("https://oss.jfrog.org/oss-snapshot-local")
        }
    }

    apply(plugin = "io.github.art.project")
    apply(plugin = "com.jfrog.bintray")
    apply(plugin = "com.jfrog.artifactory")
    apply(plugin = "maven-publish")

    art {
        idea()
        lombok()
        tests()
    }

    if (bintrayUser.isNullOrEmpty() || bintrayKey.isNullOrEmpty()) {
        return@subprojects
    }

    afterEvaluate {
        val jar: Jar by tasks
        val sourceJar = task("sourceJar", type = Jar::class) {
            archiveClassifier.set("sources")
            from(sourceSets.main.get().allJava)
        }

        publishing {
            publications {
                create<MavenPublication>(project.name) {
                    artifact(jar)
                    artifact(sourceJar)
                    groupId = rootProject.group as String
                    artifactId = project.name
                    version = rootProject.version as String
                    pom {
                        packaging = "jar"
                        description.set(project.name)
                    }
                }
            }
        }

        artifactory {
            setContextUrl("https://oss.jfrog.org")
            publish(delegateClosureOf<PublisherConfig> {
                repository(delegateClosureOf<GroovyObject> {
                    setProperty("repoKey", "oss-snapshot-local")
                    setProperty("username", bintrayUser ?: "")
                    setProperty("password", bintrayKey ?: "")
                    setProperty("maven", true)
                })
            })
            val artifactoryPublish: ArtifactoryTask by tasks
            with(artifactoryPublish) {
                publications(project.name)
            }
            artifactoryPublish.dependsOn(tasks["generatePomFileFor${name.capitalize()}Publication"], jar, sourceJar)
        }

        bintray {
            user = bintrayUser ?: ""
            key = bintrayKey ?: ""
            publish = true
            override = true
            setPublications(project.name)
            pkg(delegateClosureOf<PackageConfig> {
                repo = "art"
                name = rootProject.group as String
                userOrg = "art-community"
                websiteUrl = "https://github.com/art-community/art"
                vcsUrl = "https://github.com/art-community/art"
                setLabels("tarantool",
                        "kafka",
                        "sql",
                        "java",
                        "rsocket",
                        "rsocket-java",
                        "grpc",
                        "grpc-java",
                        "protobuf",
                        "json",
                        "xml",
                        "framework",
                        "kit",
                        "configuration",
                        "module",
                        "gradle",
                        "kotlin",
                        "scala",
                        "art",
                        "kotlin-dsl",
                        "rocksdb",
                        "scheduling",
                        "configurator",
                        "yaml",
                        "lightbend",
                        "log4j",
                        "resilience4j",
                        "badges")
                setLicenses("Apache-2.0")
            })
            with(tasks["bintrayUpload"] as BintrayUploadTask) {
                publish = true
                dependsOn(tasks["generatePomFileFor${this@subprojects.name.capitalize()}Publication"], jar, sourceJar)
            }
        }
    }

    task<DependencyReportTask>("showDependencyTrees") {
        group = "dependencies"
    }
}

task("showExternalDependenciesDuplicates") {
    group = "dependencies"
    doLast {
        val subProjectDependencies = mutableMapOf<String, MutableList<ResolvedDependency>>()

        val exclusions = listOf(
                "application-configurator",
                "application-example",
                "application-state",
                "application-module-executor",
                "application-remote-scheduler",
                "application-kafka-broker")

        subprojects.filter { subproject -> subproject.name !in exclusions }
                .forEach { subproject ->
                    val dependencies = subproject.configurations
                            .getByName(EMBEDDED.configuration)
                            .resolvedConfiguration
                            .lenientConfiguration
                            .allModuleDependencies
                            .toList()
                            .filter { dependency -> dependency.module.id.group != rootProject.group }
                    if (dependencies.isNotEmpty()) {
                        subProjectDependencies[subproject.name] = dependencies.toMutableList()
                    }
                }
        subProjectDependencies.values
                .forEach { dependencies ->
                    dependencies.removeIf { dependency ->
                        subProjectDependencies.values
                                .filter { filtering -> filtering != dependencies }
                                .none { filtering -> dependency in filtering }
                    }
                }
        subProjectDependencies.filterValues { dependencies -> dependencies.isNotEmpty() }
                .onEach { (key, value) ->
                    println(message("[${key}]:", BLUE_BOLD))
                    value.forEach { dependency -> println("\t${message(dependency.name, RED_BOLD)}") }
                    println()
                }
                .ifEmpty {
                    println(message("ART hasn't duplicates of external dependencies!", GREEN_BOLD))
                }
    }
}

task("showAllExternalDependencies") {
    group = "dependencies"
    doLast {
        val dependencies = mutableSetOf<String>()
        subprojects.forEach { subproject ->
            subproject.configurations
                    .getByName(EMBEDDED.configuration)
                    .resolvedConfiguration
                    .lenientConfiguration
                    .allModuleDependencies
                    .toSet()
                    .filter { dependency -> dependency.module.id.group != rootProject.group }
                    .forEach { dependency -> dependencies.add(dependency.name) }
        }
        dependencies.forEach { dependency -> println(message(dependency, BLUE_BOLD)) }
    }
}

afterEvaluate {
    tasks["bintrayUpload"].enabled = false
    tasks["bintrayPublish"].enabled = false
    tasks["artifactoryPublish"].enabled = false
}