package io.art.tarantool.module.connection.client.connector;

import io.art.logging.logger.*;
import io.tarantool.driver.StandaloneTarantoolClient;
import io.tarantool.driver.TarantoolClientConfig;
import io.tarantool.driver.TarantoolServerAddress;
import io.tarantool.driver.auth.SimpleTarantoolCredentials;
import io.tarantool.driver.exceptions.*;
import lombok.*;
import io.tarantool.driver.api.TarantoolClient;
import io.art.tarantool.configuration.*;
import io.art.tarantool.exception.*;
import static io.art.logging.module.LoggingModule.*;
import static io.art.tarantool.constants.TarantoolModuleConstants.LoggingMessages.TARANTOOL_CLIENT_CREATED;
import static java.text.MessageFormat.*;
import static lombok.AccessLevel.*;
import static io.art.tarantool.constants.TarantoolModuleConstants.ExceptionMessages.*;


public class TarantoolConnector {
    @Getter(lazy = true, value = PRIVATE)
    private static final Logger logger = logger(TarantoolConnector.class);

    public static TarantoolClient connect(String instanceId, TarantoolInstanceConfiguration config) {
        TarantoolClientConfig clientConfig = new TarantoolClientConfig();
        clientConfig.setConnections(config.getConnections());
        clientConfig.setConnectTimeout(config.getConnectionTimeout());
        clientConfig.setCredentials(new SimpleTarantoolCredentials(config.getUsername(), config.getPassword()));
        clientConfig.setReadTimeout(config.getReadTimeout());
        clientConfig.setRequestTimeout(config.getRequestTimeout());

        TarantoolServerAddress address = new TarantoolServerAddress(config.getHost(), config.getPort());

        int retries = 0;
        while (retries < config.getMaxConnectionRetries()){
            try {
                TarantoolClient client = new StandaloneTarantoolClient(clientConfig, address);
                getLogger().info(format(TARANTOOL_CLIENT_CREATED, instanceId, address.toString()));
                return client;
            } catch (NoAvailableConnectionsException | TarantoolClientNotConnectedException | TarantoolSocketException exception) {
                getLogger().warn(format(UNABLE_TO_CONNECT_TO_TARANTOOL_RETRY, instanceId, address.toString()));
            } catch (Throwable throwable) {
                getLogger().error(format(UNABLE_TO_CONNECT_TO_TARANTOOL_RETRY, instanceId, address.toString()));
                throw new TarantoolConnectionException(format(UNABLE_TO_CONNECT_TO_TARANTOOL_RETRY, instanceId, address.toString()), throwable);
            }
            retries++;
        }
        getLogger().error(format(UNABLE_TO_CONNECT_TO_TARANTOOL_RETRY, instanceId, address.toString()));
        throw new TarantoolConnectionException(format(UNABLE_TO_CONNECT_TO_TARANTOOL, instanceId, address.toString()));
    }

}
