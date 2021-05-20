package io.art.scheduler.model;

import io.art.scheduler.constants.SchedulerModuleConstants.*;
import lombok.*;
import java.time.*;

@Getter
@Builder(toBuilder = true)
public class PeriodicCallableTask<T> {
    private final PeriodicTaskMode mode;
    private final CallableTask<? extends T> delegate;
    private final LocalDateTime startTime;
    private final Duration period;
    private final int order;
    private final Runnable decrement;
}
