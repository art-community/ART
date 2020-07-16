package io.art.json.descriptor;

import io.art.entity.immutable.*;
import io.art.json.exception.*;
import static io.art.core.context.Context.*;
import static io.art.core.extensions.FileExtensions.*;
import static io.art.json.module.JsonModule.*;
import static java.util.Objects.*;
import java.io.*;
import java.nio.file.*;

public class JsonEntityPrettyWriter {
    public static byte[] prettyWriteJsonToBytes(Value value) {
        return prettyWriteJson(value).getBytes(contextConfiguration().getCharset());
    }

    public static void prettyWriteJson(Value value, OutputStream outputStream) {
        if (isNull(outputStream)) {
            return;
        }
        try {
            outputStream.write(prettyWriteJson(value).getBytes(contextConfiguration().getCharset()));
        } catch (IOException ioException) {
            throw new JsonMappingException(ioException);
        }
    }

    public static void prettyWriteJson(Value value, Path path) {
        writeFileQuietly(path, prettyWriteJson(value));
    }

    public static String prettyWriteJson(Value value) {
        return JsonEntityWriter.writeJson(jsonModule().getConfiguration().getObjectMapper().getFactory(), value, true);
    }
}
