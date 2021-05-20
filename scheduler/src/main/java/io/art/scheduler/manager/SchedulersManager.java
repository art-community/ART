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

package io.art.scheduler.manager;

import io.art.core.callable.*;
import io.art.core.runnable.*;
import io.art.scheduler.model.*;
import lombok.experimental.*;
import static io.art.scheduler.constants.SchedulerModuleConstants.PeriodicTaskMode.*;
import static io.art.scheduler.factory.TaskFactory.*;
import static io.art.scheduler.module.SchedulerModule.*;
import static java.time.LocalDateTime.*;
import java.time.*;
import java.util.concurrent.*;

@UtilityClass
public class SchedulersManager {
    public static <T> Future<? extends T> schedule(Callable<? extends T> task) {
        return deferredExecutor().submit(task, now());
    }

    public static <T> Future<? extends T> schedule(Callable<? extends T> task, LocalDateTime startTime) {
        return deferredExecutor().submit(task, startTime);
    }

    public static Future<?> schedule(Runnable task) {
        return deferredExecutor().execute(task, now());
    }

    public static Future<?> schedule(Runnable task, LocalDateTime startTime) {
        return deferredExecutor().execute(task, startTime);
    }


    public static <T> void scheduleFixedRate(ExceptionCallable<? extends T> task, Duration period) {
        scheduleFixedRate(task(task), period);
    }

    public static <T> void scheduleFixedRate(ExceptionCallable<? extends T> task, LocalDateTime startTime, Duration period) {
        scheduleFixedRate(task(task), startTime, period);
    }


    public static void scheduleFixedRate(ExceptionRunnable task, Duration duration) {
        scheduleFixedRate(task(task), duration);
    }

    public static void scheduleFixedRate(ExceptionRunnable task, LocalDateTime triggerTime, Duration duration) {
        scheduleFixedRate(task(task), triggerTime, duration);
    }


    public static <T> void scheduleFixedRate(CallableTask<? extends T> task, Duration period) {
        scheduleFixedRate(task, now(), period);
    }

    public static <T> void scheduleFixedRate(CallableTask<? extends T> task, LocalDateTime startTime, Duration period) {
        PeriodicCallableTask<T> periodicTask = PeriodicCallableTask.<T>builder()
                .delegate(task)
                .startTime(startTime)
                .period(period)
                .mode(FIXED)
                .build();
        periodicExecutor().submit(periodicTask);
    }


    public static void scheduleFixedRate(RunnableTask task, Duration period) {
        scheduleFixedRate(task, now(), period);
    }

    public static void scheduleFixedRate(RunnableTask task, LocalDateTime startTime, Duration period) {
        PeriodicRunnableTask periodicTask = PeriodicRunnableTask.builder()
                .delegate(task)
                .startTime(startTime)
                .period(period)
                .mode(FIXED)
                .build();
        periodicExecutor().execute(periodicTask);
    }


    public static void scheduleDelayed(ExceptionRunnable task, Duration delay) {
        scheduleDelayed(task(task), delay);
    }

    public static void scheduleDelayed(ExceptionRunnable task, LocalDateTime triggerTime, Duration delay) {
        scheduleDelayed(task(task), triggerTime, delay);
    }


    public static <T> void scheduleDelayed(ExceptionCallable<? extends T> task, Duration delay) {
        scheduleDelayed(task(task), delay);
    }

    public static <T> void scheduleDelayed(ExceptionCallable<? extends T> task, LocalDateTime startTime, Duration delay) {
        scheduleDelayed(task(task), startTime, delay);
    }


    public static void scheduleDelayed(RunnableTask task, Duration period) {
        scheduleDelayed(task, now(), period);
    }

    public static void scheduleDelayed(RunnableTask task, LocalDateTime startTime, Duration period) {
        PeriodicRunnableTask periodicTask = PeriodicRunnableTask.builder()
                .delegate(task)
                .startTime(startTime)
                .period(period)
                .mode(DELAYED)
                .build();
        periodicExecutor().execute(periodicTask);
    }


    public static <T> void scheduleDelayed(CallableTask<? extends T> task, Duration period) {
        scheduleDelayed(task, now(), period);
    }

    public static <T> void scheduleDelayed(CallableTask<? extends T> task, LocalDateTime startTime, Duration period) {
        PeriodicCallableTask<T> periodicTask = PeriodicCallableTask.<T>builder()
                .delegate(task)
                .startTime(startTime)
                .period(period)
                .mode(DELAYED)
                .build();
        periodicExecutor().submit(periodicTask);
    }


    public static boolean hasTask(String taskId) {
        return periodicExecutor().hasTask(taskId);
    }

    public static boolean cancelTask(String taskId) {
        return periodicExecutor().cancelTask(taskId);
    }
}
