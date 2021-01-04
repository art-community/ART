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

package ru.art.remote.scheduler.action;

import ru.art.core.caster.*;
import ru.art.entity.*;
import ru.art.remote.scheduler.api.model.*;
import ru.art.scheduler.db.adapter.api.model.*;
import ru.art.service.model.*;
import static java.text.MessageFormat.*;
import static ru.art.core.constants.StringConstants.*;
import static ru.art.core.extension.NullCheckingExtensions.*;
import static ru.art.grpc.client.communicator.GrpcCommunicator.*;
import static ru.art.logging.LoggingModule.*;
import static ru.art.remote.scheduler.api.constants.RemoteSchedulerApiConstants.TaskStatus.*;
import static ru.art.remote.scheduler.constants.RemoteSchedulerModuleConstants.LoggingMessages.*;
import static ru.art.remote.scheduler.constants.RemoteSchedulerModuleConstants.*;
import static ru.art.remote.scheduler.module.RemoteSchedulerModule.*;
import static ru.art.scheduler.db.adapter.api.constants.SchedulerDbAdapterApiConstants.Methods.*;
import static ru.art.service.ServiceController.*;
import static ru.art.task.deferred.executor.SchedulerModuleActions.*;
import java.util.*;

public interface TaskExecutionActions {
    static void executeDeferredTask(DeferredTask task) {
        loggingModule().getLogger(TaskExecutionActions.class).info(format(TASK_STARTED_MESSAGE, task));
        try {
            ServiceResponse<Value> response = grpcCommunicator(remoteSchedulerModule().getBalancerHost(),
                    remoteSchedulerModule().getBalancerPort(),
                    task.getExecutableServletPath())
                    .serviceId(task.getExecutableServiceId())
                    .methodId(task.getExecutableMethodId())
                    .requestMapper(Caster::cast)
                    .execute(task.getExecutableRequest());
            handleDeferredTaskCompletion(task, response);
        } catch (Throwable throwable) {
            handleDeferredTaskError(throwable, task);
        }
    }

    static void submitPeriodicTask(PeriodicTask task) {
        loggingModule().getLogger(TaskExecutionActions.class).info(format(TASK_STARTED_MESSAGE, task));
        try {
            ServiceResponse<Value> response = grpcCommunicator(remoteSchedulerModule().getBalancerHost(),
                    remoteSchedulerModule().getBalancerPort(),
                    task.getExecutableServletPath())
                    .serviceId(task.getExecutableServiceId())
                    .methodId(task.getExecutableMethodId())
                    .requestMapper(Caster::cast)
                    .execute(task.getExecutableRequest());
            handlePeriodicTaskCompletion(task, response);
        } catch (Throwable throwable) {
            handlePeriodicTaskError(throwable, task);
        }
    }

    static void submitInfinityProcess(InfinityProcess process) {
        loggingModule().getLogger(TaskExecutionActions.class).info(format(PROCESS_STARTED_MESSAGE, process));
        try {
            ServiceResponse<Value> response = grpcCommunicator(remoteSchedulerModule().getBalancerHost(),
                    remoteSchedulerModule().getBalancerPort(),
                    process.getExecutableServletPath())
                    .serviceId(process.getExecutableServiceId())
                    .methodId(process.getExecutableMethodId())
                    .requestMapper(Caster::cast)
                    .execute(process.getExecutableRequest());
            handleInfinityProcessCompletion(process, response);
        } catch (Throwable throwable) {
            handleInfinityProcessError(throwable, process);
        }
    }

    static void handlePeriodicTaskError(Throwable error, PeriodicTask task) {
        loggingModule().getLogger(TaskExecutionActions.class).error(format(TASK_FAILED_MESSAGE, task), error);
        handlePeriodicTaskAction(task);
    }

    static void handleDeferredTaskError(Throwable error, DeferredTask task) {
        loggingModule().getLogger(TaskExecutionActions.class).error(format(TASK_FAILED_MESSAGE, task), error);
        executeServiceMethod(remoteSchedulerModule().getDbAdapterServiceId(), UPDATE_DEFERRED_TASK_STATUS, new UpdateTaskStatusRequest(task.getId(), FAILED));
    }

    static void handleInfinityProcessError(Throwable error, InfinityProcess process) {
        loggingModule().getLogger(TaskExecutionActions.class).error(format(PROCESS_FAILED_MESSAGE, process), error);
    }

    static void handlePeriodicTaskCompletion(PeriodicTask task, ServiceResponse<Value> response) {
        loggingModule().getLogger(TaskExecutionActions.class).info(format(TASK_COMPLETED_MESSAGE, task, getOrElse(response.getResponseData(), EMPTY_STRING)));

        if (task.isFinishAfterCompletion()) {
            executeServiceMethod(remoteSchedulerModule().getDbAdapterServiceId(), INC_EXECUTION_COUNT, task.getId());
            executeServiceMethod(remoteSchedulerModule().getDbAdapterServiceId(), UPDATE_PERIODIC_TASK_STATUS, new UpdateTaskStatusRequest(task.getId(), FINISHED));
            cancelPeriodicTask(task.getId());
            return;
        }

        handlePeriodicTaskAction(task);
    }

    static void handleDeferredTaskCompletion(DeferredTask task, ServiceResponse<Value> response) {
        loggingModule().getLogger(TaskExecutionActions.class).info(format(TASK_COMPLETED_MESSAGE, task, getOrElse(response.getResponseData(), EMPTY_STRING)));
        executeServiceMethod(remoteSchedulerModule().getDbAdapterServiceId(), UPDATE_DEFERRED_TASK_STATUS, new UpdateTaskStatusRequest(task.getId(), COMPLETED));
    }

    static void handleInfinityProcessCompletion(InfinityProcess process, ServiceResponse<Value> response) {
        loggingModule().getLogger(TaskExecutionActions.class).info(format(PROCESS_COMPLETED_MESSAGE, process, getOrElse(response.getResponseData(), EMPTY_STRING)));
    }

    static void updateTaskStatusProcessingIfFirstAttempt(String taskId, int currentRetryCount) {
        if (currentRetryCount <= 1) {
            executeServiceMethod(remoteSchedulerModule().getDbAdapterServiceId(), UPDATE_PERIODIC_TASK_STATUS, new UpdateTaskStatusRequest(taskId, PROCESSING));
        }
    }

    static void handlePeriodicTaskAction(PeriodicTask task) {
        Optional<Integer> currentExecutionCount = executeServiceMethod(remoteSchedulerModule().getDbAdapterServiceId(), INC_EXECUTION_COUNT, task.getId());
        updateTaskStatusProcessingIfFirstAttempt(task.getId(), currentExecutionCount.orElse(ZERO));
        executeServiceMethod(remoteSchedulerModule().getDbAdapterServiceId(), UPDATE_TASK_EXECUTION_TIME, new UpdateTaskExecutionTimeRequest(task.getId(), task.getExecutionPeriodSeconds()));

        if (!currentExecutionCount.isPresent() || currentExecutionCount.get() < task.getMaxExecutionCount()) {
            return;
        }
        removePeriodicTask(task.getId());
        executeServiceMethod(remoteSchedulerModule().getDbAdapterServiceId(), UPDATE_PERIODIC_TASK_STATUS, new UpdateTaskStatusRequest(task.getId(), FINISHED));
    }
}
