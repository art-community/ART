package ru.art.grpc.client.state;

import io.grpc.ManagedChannel;
import lombok.Getter;
import ru.art.core.module.ModuleState;
import static ru.art.core.factory.CollectionsFactory.linkedListOf;
import java.util.List;

@Getter
public class GrpcClientModuleState implements ModuleState {
    private final List<ManagedChannel> channels = linkedListOf();

    public ManagedChannel registerChannel(ManagedChannel channel) {
        channels.add(channel);
        return channel;
    }
}
