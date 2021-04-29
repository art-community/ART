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

package io.art.core.extensions;

import io.art.core.exception.*;
import io.art.core.handler.*;
import lombok.experimental.*;
import static io.art.core.checker.EmptinessChecker.*;
import static io.art.core.constants.ArrayConstants.*;
import static io.art.core.constants.BufferConstants.*;
import static io.art.core.constants.ExceptionMessages.*;
import static io.art.core.constants.StringConstants.*;
import static io.art.core.context.Context.*;
import static java.lang.System.*;
import static java.nio.ByteBuffer.*;
import static java.nio.channels.Channels.newInputStream;
import static java.nio.channels.Channels.newOutputStream;
import static java.nio.channels.FileChannel.*;
import static java.nio.file.Files.*;
import static java.nio.file.Paths.*;
import static java.nio.file.StandardCopyOption.*;
import static java.nio.file.StandardOpenOption.*;
import static java.text.MessageFormat.*;
import static java.util.Objects.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.nio.file.*;

@UtilityClass
public class FileExtensions {
    public static String parseExtension(String path) {
        if (isEmpty(path)) {
            throw new ParseException(format(FILE_PATH_NOT_VALID, path));
        }
        return path.substring(path.lastIndexOf(DOT) + 1);
    }

    public static String withoutExtension(String path) {
        if (isEmpty(path)) {
            throw new ParseException(format(FILE_PATH_NOT_VALID, path));
        }
        return path.substring(0, path.lastIndexOf(DOT));
    }

    public static File fileOf(URL url) {
        return new File(ExceptionHandler.<URI>wrapException(InternalRuntimeException::new).call(url::toURI));
    }


    public static void readFileToBuffer(String path, ByteBuffer buffer) {
        readFileToBuffer(get(path), buffer);
    }

    public static void readFileToBuffer(Path path, ByteBuffer buffer) {
        try (FileChannel fileChannel = open(path)) {
            fileChannel.read(buffer);
            buffer.flip();
        } catch (IOException ioException) {
            throw new InternalRuntimeException(ioException);
        }
    }


    public static String readFile(String path) {
        ByteBuffer buffer = allocate(DEFAULT_BUFFER_SIZE);
        try {
            return readFile(get(path), buffer);
        } finally {
            buffer.clear();
        }
    }

    public static String readFile(Path path) {
        ByteBuffer buffer = allocate(DEFAULT_BUFFER_SIZE);
        try {
            return readFile(path, buffer);
        } finally {
            buffer.clear();
        }
    }


    public static String readFile(String path, ByteBuffer buffer) {
        return readFile(get(path), buffer);
    }

    public static String readFile(Path path, ByteBuffer buffer) {
        StringBuilder result = new StringBuilder(EMPTY_STRING);
        CharsetDecoder decoder = context().configuration().getCharset().newDecoder();
        try (FileChannel fileChannel = open(path)) {
            do {
                fileChannel.read(buffer);
                buffer.flip();
                if (buffer.limit() > 1) {
                    result.append(decoder.decode(buffer));
                }
                buffer.clear();
            } while (fileChannel.position() < fileChannel.size());
            return result.toString();
        } catch (IOException ioException) {
            throw new InternalRuntimeException(ioException);
        }
    }


    public static String readFileQuietly(String path) {
        ByteBuffer buffer = allocate(DEFAULT_BUFFER_SIZE);
        try {
            return readFileQuietly(path, buffer);
        } finally {
            buffer.clear();
        }
    }

    public static String readFileQuietly(String path, ByteBuffer buffer) {
        return readFileQuietly(get(path), buffer);
    }


    public static String readFileQuietly(Path path) {
        ByteBuffer buffer = allocate(DEFAULT_BUFFER_SIZE);
        try {
            return readFileQuietly(path, buffer);
        } finally {
            buffer.clear();
        }
    }

    public static String readFileQuietly(Path path, ByteBuffer buffer) {
        return readFileQuietly(path, buffer, context().configuration().getCharset());
    }


    public static String readFile(String path, Charset charset) {
        ByteBuffer buffer = allocate(DEFAULT_BUFFER_SIZE);
        try {
            return readFile(path, buffer, charset);
        } finally {
            buffer.clear();
        }
    }

    public static String readFile(Path path, Charset charset) {
        ByteBuffer buffer = allocate(DEFAULT_BUFFER_SIZE);
        try {
            return readFile(path, buffer, charset);
        } finally {
            buffer.clear();
        }
    }


    public static String readFile(String path, ByteBuffer buffer, Charset charset) {
        return readFile(get(path), buffer, charset);
    }

    public static String readFile(Path path, ByteBuffer buffer, Charset charset) {
        StringBuilder result = new StringBuilder(EMPTY_STRING);
        CharsetDecoder decoder = charset.newDecoder();
        try (FileChannel fileChannel = open(path)) {
            do {
                fileChannel.read(buffer);
                buffer.flip();
                if (buffer.limit() > 1) {
                    result.append(decoder.decode(buffer));
                }
                buffer.clear();
            } while (fileChannel.position() < fileChannel.size());
            return result.toString();
        } catch (IOException ioException) {
            throw new InternalRuntimeException(ioException);
        }
    }


    public static String readFileQuietly(String path, Charset charset) {
        ByteBuffer buffer = allocate(DEFAULT_BUFFER_SIZE);
        try {
            return readFileQuietly(path, buffer, charset);
        } finally {
            buffer.clear();
        }
    }

    public static String readFileQuietly(String path, ByteBuffer buffer, Charset charset) {
        return readFileQuietly(get(path), buffer, charset);
    }


    public static String readFileQuietly(Path path, Charset charset) {
        ByteBuffer buffer = allocate(DEFAULT_BUFFER_SIZE);
        try {
            return readFileQuietly(path, buffer, charset);
        } finally {
            buffer.clear();
        }
    }

    public static String readFileQuietly(Path path, ByteBuffer buffer, Charset charset) {
        StringBuilder result = new StringBuilder(EMPTY_STRING);
        CharsetDecoder decoder = charset.newDecoder();
        try (FileChannel fileChannel = open(path)) {
            do {
                fileChannel.read(buffer);
                buffer.flip();
                result.append(decoder.decode(buffer));
                buffer.clear();
            } while (fileChannel.position() < fileChannel.size());
            return result.toString();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return result.toString();
    }


    public static byte[] readFileBytes(String path) {
        ByteBuffer buffer = allocate(DEFAULT_BUFFER_SIZE);
        try {
            return readFileBytes(get(path), buffer);
        } finally {
            buffer.clear();
        }
    }

    public static byte[] readFileBytesQuietly(String path) {
        ByteBuffer buffer = allocate(DEFAULT_BUFFER_SIZE);
        try {
            return readFileBytesQuietly(get(path), buffer);
        } finally {
            buffer.clear();
        }
    }


    public static byte[] readFileBytes(Path path) {
        ByteBuffer buffer = allocate(DEFAULT_BUFFER_SIZE);
        try {
            return readFileBytes(path, buffer);
        } finally {
            buffer.clear();
        }
    }

    public static byte[] readFileBytesQuietly(Path path) {
        ByteBuffer buffer = allocate(DEFAULT_BUFFER_SIZE);
        try {
            return readFileBytesQuietly(path, buffer);
        } finally {
            buffer.clear();
        }
    }


    public static byte[] readFileBytes(String path, ByteBuffer buffer) {
        return readFileBytes(get(path), buffer);
    }

    public static byte[] readFileBytes(Path path, ByteBuffer buffer) {
        byte[] result = EMPTY_BYTES;
        try (FileChannel fileChannel = open(path)) {
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
            return result;
        } catch (IOException ioException) {
            throw new InternalRuntimeException(ioException);
        }
    }


    public static byte[] readFileBytesQuietly(Path path, ByteBuffer buffer) {
        try {
            return readFileBytes(path, buffer);
        } catch (Throwable throwable) {
            return EMPTY_BYTES;
        }
    }


    public static InputStream fileInputStream(String path) {
        return fileInputStream(get(path));
    }

    public static InputStream fileInputStream(Path path) {
        return fileInputStream(path.toFile());
    }

    public static InputStream fileInputStream(File file) {
        try {
            return newInputStream(open(file.toPath()));
        } catch (IOException ioException) {
            throw new InternalRuntimeException(ioException);
        }
    }


    public static OutputStream fileOutputStream(String path) {
        return fileOutputStream(get(path));
    }

    public static OutputStream fileOutputStream(Path path) {
        return fileOutputStream(path.toFile());
    }

    public static OutputStream fileOutputStream(File file) {
        try {
            return newOutputStream(open(file.toPath(), CREATE, TRUNCATE_EXISTING, WRITE));
        } catch (IOException ioException) {
            throw new InternalRuntimeException(ioException);
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
        writeFile(path, wrap(content));
    }

    public static void writeFileQuietly(Path path, byte[] content) {
        writeFileQuietly(path, wrap(content));
    }


    public static void writeFile(Path path, ByteBuffer buffer) {
        try {
            Path parent = path.getParent();
            if (nonNull(parent)) {
                createDirectories(parent);
            }
            try (FileChannel fileChannel = open(path, CREATE, TRUNCATE_EXISTING, WRITE)) {
                fileChannel.write(buffer);
            }
        } catch (IOException ioException) {
            throw new InternalRuntimeException(ioException);
        }
    }

    public static void writeFileQuietly(Path path, ByteBuffer buffer) {
        try {
            Path parent = path.getParent();
            if (nonNull(parent)) {
                createDirectories(parent);
            }
            try (FileChannel fileChannel = open(path, CREATE, TRUNCATE_EXISTING, WRITE)) {
                fileChannel.write(buffer);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }


    public static boolean recursiveDelete(String path) {
        return recursiveDelete(get(path));
    }

    public static boolean recursiveDelete(Path path) {
        return recursiveDelete(path.toFile());
    }

    @SuppressWarnings("all")
    public static boolean recursiveDelete(File file) {
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
            recursiveDelete(child);
        }
        return file.delete();
    }

    public static void recursiveCopy(Path from, Path to) {
        File file = from.toFile();
        if (!file.exists()) {
            return;
        }
        try {
            if (file.isFile()) {
                if (!to.getParent().toFile().exists()) {
                    createDirectories(to.getParent());
                }
                Files.copy(from, to, REPLACE_EXISTING);
                return;
            }
            File[] children = file.listFiles();
            if (isNull(children) || isEmpty(children)) {
                return;
            }
            for (File child : children) {
                Path toPath = to.resolve(child.toPath().getFileName());
                recursiveCopy(child.toPath(), toPath);
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
