package ru.art.remote.scheduler.api.communicator.grpc;

import lombok.Getter;
import lombok.experimental.Accessors;
import ru.art.grpc.client.communicator.GrpcCommunicator;
import ru.art.grpc.client.specification.GrpcCommunicationSpecification;
import ru.art.service.exception.UnknownServiceMethodException;

import static ru.art.grpc.client.communicator.GrpcCommunicator.grpcCommunicator;
import static ru.art.remote.scheduler.api.constants.RemoteSchedulerApiConstants.Methods.*;
import static ru.art.remote.scheduler.api.constants.RemoteSchedulerApiConstants.REMOTE_SCHEDULER_SERVICE_ID;
import static ru.art.remote.scheduler.api.mapping.DeferredTaskMappers.DeferredTaskRequestMapper.deferredTaskRequestFromModelMapper;
import static ru.art.remote.scheduler.api.mapping.InfinityProcessMappers.InfinityProcessRequestMapper.infinityProcessRequestFromModelMapper;
import static ru.art.remote.scheduler.api.mapping.PeriodicTaskMappers.PeriodicTaskRequestMapper.periodicTaskRequestFromModelMapper;

@Getter
public class RemoteSchedulerServiceGrpcCommunicationSpecification implements GrpcCommunicationSpecification {
    private final String path;
    private final String host;
    private final Integer port;
    private final String serviceId = REMOTE_SCHEDULER_SERVICE_ID;

    public RemoteSchedulerServiceGrpcCommunicationSpecification(String path, String host, Integer port) {
        this.path = path;
        this.host = host;
        this.port = port;
    }

    @Getter(lazy = true)
    @Accessors(fluent = true)
    private final GrpcCommunicator addDeferredTask = grpcCommunicator(host, port, path)
           .serviceId(REMOTE_SCHEDULER_SERVICE_ID)
            .methodId(ADD_DEFERRED_TASK)
            .requestMapper(deferredTaskRequestFromModelMapper);

    @Getter(lazy = true)
    @Accessors(fluent = true)
    private final GrpcCommunicator addPeriodicTask = grpcCommunicator(host, port, path)
            .serviceId(REMOTE_SCHEDULER_SERVICE_ID)
            .methodId(ADD_PERIODIC_TASK)
            .requestMapper(periodicTaskRequestFromModelMapper);

    @Getter(lazy = true)
    @Accessors(fluent = true)
    private final GrpcCommunicator addInfinityProcess = grpcCommunicator(host, port, path)
            .serviceId(REMOTE_SCHEDULER_SERVICE_ID)
            .methodId(ADD_INFINITY_PROCESS)
            .requestMapper(infinityProcessRequestFromModelMapper);

    @Override
    public <P, R> R executeMethod(String methodId, P request) {
        switch (methodId) {
            case ADD_DEFERRED_TASK:
                addDeferredTask.execute();
                return null;
            case ADD_PERIODIC_TASK:
                addPeriodicTask.execute();
                return null;
            case ADD_INFINITY_PROCESS:
                addInfinityProcess.execute();
                return null;
        }
        throw new UnknownServiceMethodException(getServiceId(), methodId);
    }
}