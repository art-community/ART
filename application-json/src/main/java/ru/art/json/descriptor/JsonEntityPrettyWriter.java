package ru.art.json.descriptor;

import ru.art.entity.Value;
import ru.art.json.exception.JsonMappingException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

import static java.util.Objects.isNull;
import static ru.art.core.context.Context.contextConfiguration;
import static ru.art.core.extension.FileExtensions.writeFileQuietly;
import static ru.art.json.module.JsonModule.jsonModule;

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
        return JsonEntityWriter.writeJson(jsonModule().getObjectMapper().getFactory(), value, true);
    }
}
