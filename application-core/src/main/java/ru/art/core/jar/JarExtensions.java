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

package ru.art.core.jar;

import ru.art.core.exception.*;
import static java.nio.file.Files.*;
import static java.nio.file.StandardCopyOption.*;
import static java.util.Optional.*;
import static java.util.regex.Pattern.*;
import static java.util.stream.Collectors.*;
import static ru.art.core.constants.StringConstants.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.zip.*;

@SuppressWarnings("Duplicates")
public class JarExtensions {
    public static boolean insideJar(Class<?> jarMarkerClass) {
        return ofNullable(jarMarkerClass.getProtectionDomain())
                .map(ProtectionDomain::getCodeSource)
                .map(CodeSource::getLocation)
                .map(URL::getPath)
                .map(File::new)
                .map(File::getPath)
                .filter(file -> !isDirectory(Paths.get(file)))
                .isPresent();
    }

    public static void extractCurrentJar(Class<?> jarMarkerClass) {
        ofNullable(jarMarkerClass.getProtectionDomain())
                .map(ProtectionDomain::getCodeSource)
                .map(CodeSource::getLocation)
                .map(URL::getPath)
                .map(File::new)
                .map(File::getPath)
                .filter(file -> !isDirectory(Paths.get(file)))
                .ifPresent(JarExtensions::extractJar);
    }

    public static void extractCurrentJar(Class<?> jarMarkerClass, String directory) {
        ofNullable(jarMarkerClass.getProtectionDomain())
                .map(ProtectionDomain::getCodeSource)
                .map(CodeSource::getLocation)
                .map(URL::getPath)
                .map(File::new)
                .map(File::getPath)
                .filter(file -> !isDirectory(Paths.get(file)))
                .ifPresent(file -> extractJar(file, directory));
    }

    public static void extractJar(String jarPath) {
        extractJar(jarPath, EMPTY_STRING);
    }

    public static void extractJar(String jarPath, String directory) {
        try (ZipFile jarArchive = new ZipFile(jarPath)) {
            createDirectories(Paths.get(directory));
            for (ZipEntry entry : jarArchive.stream().collect(toList())) {
                Path entryDestination = Paths.get(directory).resolve(entry.getName());
                if (entry.isDirectory() && !exists(entryDestination)) {
                    createDirectory(entryDestination);
                    continue;
                }
                copy(jarArchive.getInputStream(entry), entryDestination, REPLACE_EXISTING);
            }
        } catch (IOException ioException) {
            throw new InternalRuntimeException(ioException);
        }
    }

    public static void extractCurrentJarEntry(Class<?> jarMarkerClass, String entryRegex) {
        ofNullable(jarMarkerClass.getProtectionDomain())
                .map(ProtectionDomain::getCodeSource)
                .map(CodeSource::getLocation)
                .map(URL::getPath)
                .map(File::new)
                .map(File::getPath)
                .filter(file -> !isDirectory(Paths.get(file)))
                .ifPresent(jarPath -> extractJarEntry(jarPath, entryRegex, EMPTY_STRING));
    }

    public static void extractCurrentJarEntry(Class<?> jarMarkerClass, String entryRegex, String directory) {
        ofNullable(jarMarkerClass.getProtectionDomain())
                .map(ProtectionDomain::getCodeSource)
                .map(CodeSource::getLocation)
                .map(URL::getPath)
                .map(File::new)
                .map(File::getPath)
                .filter(file -> !isDirectory(Paths.get(file)))
                .ifPresent(jarPath -> extractJarEntry(jarPath, entryRegex, directory));
    }

    public static void extractJarEntry(String jarPath, String entryRegex) {
        extractJarEntry(jarPath, entryRegex, EMPTY_STRING);
    }

    public static void extractJarEntry(String jarPath, String entryRegex, String directory) {
        try (ZipFile jarArchive = new ZipFile(jarPath)) {
            createDirectories(Paths.get(directory));
            List<? extends ZipEntry> entries = jarArchive
                    .stream()
                    .filter(entry -> compile(entryRegex).matcher(entry.getName()).matches())
                    .collect(toList());
            for (ZipEntry entry : entries) {
                Path entryDest = Paths.get(directory).resolve(entry.getName());

                if (entry.isDirectory() && !exists(entryDest)) {
                    createDirectory(entryDest);
                    continue;
                }
                copy(jarArchive.getInputStream(entry), entryDest, REPLACE_EXISTING);
            }
        } catch (IOException ioException) {
            throw new InternalRuntimeException(ioException);
        }
    }
}
