package ru.art.remote.scheduler.api.communicator.grpc;

import lombok.Getter;
import lombok.experimental.Accessors;
import ru.art.grpc.client.communicator.GrpcCommunicator;
import ru.art.grpc.client.specification.GrpcCommunicationSpecification;
import ru.art.service.exception.UnknownServiceMethodException;

import static ru.art.core.caster.Caster.cast;
import static ru.art.entity.PrimitiveMapping.stringMapper;
import static ru.art.grpc.client.communicator.GrpcCommunicator.grpcCommunicator;
import static ru.art.grpc.client.module.GrpcClientModule.grpcClientModule;
import static ru.art.remote.scheduler.api.constants.RemoteSchedulerApiConstants.Methods.*;
import static ru.art.remote.scheduler.api.constants.RemoteSchedulerApiConstants.REMOTE_SCHEDULER_SERVICE_ID;
import static ru.art.remote.scheduler.api.mapping.DeferredTaskMappers.DeferredTaskCollectionMapper.deferredTaskCollectionToModelMapper;
import static ru.art.remote.scheduler.api.mapping.DeferredTaskMappers.DeferredTaskRequestMapper.deferredTaskRequestFromModelMapper;
import static ru.art.remote.scheduler.api.mapping.DeferredTaskMappers.DeferredTaskRequestMapper.deferredTaskRequestToModelMapper;
import static ru.art.remote.scheduler.api.mapping.InfinityProcessMappers.InfinityProcessCollectionMapper.processCollectionToModelMapper;
import static ru.art.remote.scheduler.api.mapping.InfinityProcessMappers.InfinityProcessRequestMapper.infinityProcessRequestFromModelMapper;
import static ru.art.remote.scheduler.api.mapping.PeriodicTaskMappers.PeriodicTaskCollectionMapper.periodicTaskCollectionToModelMapper;
import static ru.art.remote.scheduler.api.mapping.PeriodicTaskMappers.PeriodicTaskRequestMapper.periodicTaskRequestFromModelMapper;
import static ru.art.remote.scheduler.api.mapping.PeriodicTaskMappers.PeriodicTaskRequestMapper.periodicTaskRequestToModelMapper;
import static ru.art.service.ServiceResponseDataExtractor.extractResponseDataChecked;

@Getter
public class RemoteSchedulerServiceGrpcCommunicationSpec implements GrpcCommunicationSpecification {
    private final String serviceId = REMOTE_SCHEDULER_SERVICE_ID;


    @Getter(lazy = true)
    @Accessors(fluent = true)
    private final GrpcCommunicator addDeferredTask = grpcCommunicator(grpcClientModule()
            .getCommunicationTargetConfiguration(serviceId))
            .serviceId(REMOTE_SCHEDULER_SERVICE_ID)
            .methodId(ADD_DEFERRED_TASK)
            .requestMapper(deferredTaskRequestFromModelMapper)
            .responseMapper(stringMapper.getToModel());

    @Getter(lazy = true)
    @Accessors(fluent = true)
    private final GrpcCommunicator addPeriodicTask = grpcCommunicator(grpcClientModule()
            .getCommunicationTargetConfiguration(serviceId))
            .serviceId(REMOTE_SCHEDULER_SERVICE_ID)
            .methodId(ADD_PERIODIC_TASK)
            .requestMapper(periodicTaskRequestFromModelMapper)
            .responseMapper(stringMapper.getToModel());

    @Getter(lazy = true)
    @Accessors(fluent = true)
    private final GrpcCommunicator addInfinityProcess = grpcCommunicator(grpcClientModule()
            .getCommunicationTargetConfiguration(serviceId))
            .serviceId(REMOTE_SCHEDULER_SERVICE_ID)
            .methodId(ADD_INFINITY_PROCESS)
            .requestMapper(infinityProcessRequestFromModelMapper)
            .responseMapper(stringMapper.getToModel());

    @Getter(lazy = true)
    @Accessors(fluent = true)
    private final GrpcCommunicator getDeferredTaskById = getGrpcCommunicator(GET_DEFERRED_TASK_BY_ID)
            .responseMapper(deferredTaskRequestToModelMapper);

    @Getter(lazy = true)
    @Accessors(fluent = true)
    private final GrpcCommunicator getPeriodicTaskById = getGrpcCommunicator(GET_PERIODIC_TASK_BY_ID)
            .responseMapper(periodicTaskRequestToModelMapper);

    @Getter(lazy = true)
    @Accessors(fluent = true)
    private final GrpcCommunicator getAllDeferredTasks = grpcCommunicator(grpcClientModule()
            .getCommunicationTargetConfiguration(serviceId))
            .serviceId(REMOTE_SCHEDULER_SERVICE_ID)
            .methodId(GET_ALL_DEFERRED_TASKS)
            .responseMapper(deferredTaskCollectionToModelMapper);

    @Getter(lazy = true)
    @Accessors(fluent = true)
    private final GrpcCommunicator getAllPeriodicTasks = grpcCommunicator(grpcClientModule()
            .getCommunicationTargetConfiguration(serviceId))
            .serviceId(REMOTE_SCHEDULER_SERVICE_ID)
            .methodId(GET_ALL_PERIODIC_TASKS)
            .responseMapper(periodicTaskCollectionToModelMapper);

    @Getter(lazy = true)
    @Accessors(fluent = true)
    private final GrpcCommunicator getAllInfinityProcesses = grpcCommunicator(grpcClientModule()
            .getCommunicationTargetConfiguration(serviceId))
            .serviceId(REMOTE_SCHEDULER_SERVICE_ID)
            .methodId(GET_ALL_INFINITY_PROCESSES)
            .responseMapper(processCollectionToModelMapper);
    @Override
    public <RequestType, ResponseType> ResponseType executeMethod(String methodId, RequestType request) {
        switch (methodId) {
            case ADD_DEFERRED_TASK:
                return cast(extractResponseDataChecked(addDeferredTask().execute(request)));
            case ADD_PERIODIC_TASK:
                return cast(extractResponseDataChecked(addPeriodicTask().execute(request)));
            case ADD_INFINITY_PROCESS:
                return cast(extractResponseDataChecked(addInfinityProcess().execute(request)));
            case GET_DEFERRED_TASK_BY_ID:
                return cast(extractResponseDataChecked(getDeferredTaskById().execute(request)));
            case GET_PERIODIC_TASK_BY_ID:
                return cast(extractResponseDataChecked(getPeriodicTaskById().execute(request)));
            case GET_ALL_DEFERRED_TASKS:
                return cast(extractResponseDataChecked(getAllDeferredTasks().execute()));
            case GET_ALL_PERIODIC_TASKS:
                return cast(extractResponseDataChecked(getAllPeriodicTasks().execute()));
            case GET_ALL_INFINITY_PROCESSES:
                return cast(extractResponseDataChecked(getAllInfinityProcesses().execute()));
            case CANCEL_PERIODIC_TASK:
                cancelPeriodicTask().execute(request);
                return null;
            default:
                throw new UnknownServiceMethodException(getServiceId(), methodId);
        }
    }

    @Getter(lazy = true)
    @Accessors(fluent = true)
    private final GrpcCommunicator cancelPeriodicTask = getGrpcCommunicator(CANCEL_PERIODIC_TASK);

    private GrpcCommunicator getGrpcCommunicator(String cancelPeriodicTask) {
        return grpcCommunicator(grpcClientModule()
                .getCommunicationTargetConfiguration(serviceId))
                .serviceId(REMOTE_SCHEDULER_SERVICE_ID)
                .methodId(cancelPeriodicTask)
                .requestMapper(stringMapper.getFromModel());
    }
}
