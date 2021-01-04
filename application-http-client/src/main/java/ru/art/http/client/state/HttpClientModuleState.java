package ru.art.http.client.state;

import lombok.Getter;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import ru.art.core.module.ModuleState;
import static ru.art.core.factory.CollectionsFactory.linkedListOf;
import java.util.List;

@Getter
public class HttpClientModuleState implements ModuleState {
    private final List<CloseableHttpClient> clients = linkedListOf();
    private final List<CloseableHttpAsyncClient> asynchronousClients = linkedListOf();

    public CloseableHttpClient registerClient(CloseableHttpClient client) {
        this.clients.add(client);
        return client;
    }

    public CloseableHttpAsyncClient registerClient(CloseableHttpAsyncClient client) {
        this.asynchronousClients.add(client);
        return client;
    }
}
