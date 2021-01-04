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

package ru.art.core.extension;

import lombok.experimental.*;
import ru.art.core.exception.*;
import static java.lang.System.*;
import static java.nio.ByteBuffer.*;
import static java.nio.channels.FileChannel.*;
import static java.nio.file.Files.*;
import static java.nio.file.Paths.*;
import static java.nio.file.StandardOpenOption.*;
import static java.util.Objects.*;
import static ru.art.core.checker.CheckerForEmptiness.*;
import static ru.art.core.constants.ArrayConstants.*;
import static ru.art.core.constants.BufferConstants.*;
import static ru.art.core.constants.StringConstants.*;
import static ru.art.core.context.Context.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.file.*;

@UtilityClass
public class FileExtensions {
    public static String readFile(String path) {
        return readFile(get(path), DEFAULT_BUFFER_SIZE);
    }

    public static String readFileQuietly(String path) {
        return readFileQuietly(get(path), DEFAULT_BUFFER_SIZE);
    }

    public static String readFile(Path path) {
        return readFile(path, DEFAULT_BUFFER_SIZE);
    }

    public static String readFileQuietly(Path path) {
        return readFileQuietly(path, DEFAULT_BUFFER_SIZE);
    }

    public static String readFile(String path, int bufferSize) {
        return readFile(get(path), bufferSize);
    }

    public static String readFile(Path path, int bufferSize) {
        if (bufferSize <= 0) {
            return EMPTY_STRING;
        }
        ByteBuffer buffer = allocateDirect(bufferSize);
        StringBuilder result = new StringBuilder(EMPTY_STRING);
        try {
            FileChannel fileChannel = open(path);
            do {
                fileChannel.read(buffer);
                buffer.flip();
                if (buffer.limit() > 1) {
                    result.append(contextConfiguration().getCharset().newDecoder().decode(buffer).toString());
                }
                buffer.clear();
            } while (fileChannel.position() < fileChannel.size());
        } catch (IOException ioException) {
            throw new InternalRuntimeException(ioException);
        }
        return result.toString();
    }

    public static String readFileQuietly(Path path, int bufferSize) {
        if (bufferSize <= 0) {
            return EMPTY_STRING;
        }
        ByteBuffer buffer = allocateDirect(bufferSize);
        StringBuilder result = new StringBuilder(EMPTY_STRING);
        try {
            FileChannel fileChannel = open(path);
            do {
                fileChannel.read(buffer);
                buffer.flip();
                result.append(contextConfiguration().getCharset().newDecoder().decode(buffer).toString());
                buffer.clear();
            } while (fileChannel.position() < fileChannel.size());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return result.toString();
    }


    public static byte[] readFileBytes(String path) {
        return readFileBytes(get(path), DEFAULT_BUFFER_SIZE);
    }

    public static byte[] readFileBytesQuietly(String path) {
        return readFileBytesQuietly(get(path), DEFAULT_BUFFER_SIZE);
    }

    public static byte[] readFileBytes(Path path) {
        return readFileBytes(path, DEFAULT_BUFFER_SIZE);
    }

    public static byte[] readFileBytesQuietly(Path path) {
        return readFileBytesQuietly(path, DEFAULT_BUFFER_SIZE);
    }

    public static byte[] readFileBytes(String path, int bufferSize) {
        return readFileBytes(get(path), bufferSize);
    }

    public static byte[] readFileBytes(Path path, int bufferSize) {
        if (bufferSize <= 0) {
            return EMPTY_BYTES;
        }
        ByteBuffer buffer = allocateDirect(bufferSize);
        byte[] result = EMPTY_BYTES;
        try {
            FileChannel fileChannel = open(path);
            do {
                fileChannel.read(buffer);
                buffer.flip();
                byte[] bufferBytes = new byte[buffer.limit()];
                buffer.get(bufferBytes);
                byte[] newResult = new byte[result.length + bufferBytes.length];
                arraycopy(result, 0, newResult, 0, result.length);
                arraycopy(bufferBytes, 0, newResult, result.length, bufferBytes.length);
                result = newResult;
                buffer.clear();
            } while (fileChannel.position() < fileChannel.size());
        } catch (IOException ioException) {
            throw new InternalRuntimeException(ioException);
        }
        return result;
    }

    public static byte[] readFileBytesQuietly(Path path, int bufferSize) {
        try {
            return readFileBytes(path, bufferSize);
        } catch (Throwable throwable) {
            return EMPTY_BYTES;
        }
    }


    public static void writeFile(String path, String content) {
        writeFile(get(path), content);
    }

    public static void writeFileQuietly(String path, String content) {
        writeFileQuietly(get(path), content);
    }

    public static void writeFile(Path path, String content) {
        writeFileQuietly(path, content.getBytes());
    }

    public static void writeFileQuietly(Path path, String content) {
        writeFileQuietly(path, content.getBytes());
    }


    public static void writeFile(String path, byte[] content) {
        writeFile(get(path), content);
    }

    public static void writeFileQuietly(String path, byte[] content) {
        writeFileQuietly(get(path), content);
    }

    public static void writeFile(Path path, byte[] content) {
        ByteBuffer byteBuffer = wrap(content);
        try {
            Path parent = path.getParent();
            if (nonNull(parent)) {
                createDirectories(parent);
            }
            FileChannel fileChannel = open(path, CREATE, TRUNCATE_EXISTING, WRITE);
            fileChannel.write(byteBuffer);
            fileChannel.close();
        } catch (IOException ioException) {
            throw new InternalRuntimeException(ioException);
        }
    }

    public static void writeFileQuietly(Path path, byte[] content) {
        ByteBuffer byteBuffer = wrap(content);
        try {
            Path parent = path.getParent();
            if (nonNull(parent)) {
                createDirectories(parent);
            }
            FileChannel fileChannel = open(path, CREATE, TRUNCATE_EXISTING, WRITE);
            fileChannel.write(byteBuffer);
            fileChannel.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }


    public static boolean deleteFileRecursive(String path) {
        return deleteFileRecursive(get(path));
    }

    public static boolean deleteFileRecursive(Path path) {
        return deleteFileRecursive(path.toFile());
    }

    @SuppressWarnings("all")
    public static boolean deleteFileRecursive(File file) {
        if (isNull(file) || !file.exists()) {
            return true;
        }
        if (file.isFile()) {
            return file.delete();
        }
        File[] children = file.listFiles();
        if (isEmpty(children)) {
            return file.delete();
        }
        for (File child : children) {
            deleteFileRecursive(child);
        }
        return file.delete();
    }
}
