package io.art.tarantool.module.connection.client.caller;

import io.art.tarantool.exception.TarantoolDaoException;
import io.tarantool.driver.api.TarantoolClient;
import lombok.Getter;
import io.art.logging.logger.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static io.art.logging.module.LoggingModule.logger;
import static io.art.tarantool.constants.TarantoolModuleConstants.LoggingMessages.*;
import static io.art.tarantool.module.TarantoolModule.tarantoolModule;
import static java.text.MessageFormat.format;
import static lombok.AccessLevel.PRIVATE;

public class TarantoolFunctionCaller {
    @Getter(lazy = true, value = PRIVATE)
    private static final Logger logger = logger(TarantoolFunctionCaller.class);

    public static CompletableFuture<List<?>> call(TarantoolClient client, String function, Object... args){
        try {
            logInfo(format(CALLING_FUNCTION, function));
            return client.call(function, args);
        } catch (Throwable throwable){
            getLogger().error(format(FAILED_FUNCTION, function));
            throw new TarantoolDaoException(format(FAILED_FUNCTION, function), throwable);
        }
    }

    private static void logInfo(String message){
        if (tarantoolModule().configuration().logging) getLogger().info(message);
    }
}
