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

package io.art.resilience.state;

import io.art.core.module.*;
import io.github.resilience4j.bulkhead.*;
import io.github.resilience4j.circuitbreaker.*;
import io.github.resilience4j.ratelimiter.*;
import io.github.resilience4j.retry.*;
import io.github.resilience4j.timelimiter.*;
import lombok.*;

@Getter
public class ResilienceModuleState implements ModuleState {
    private final RateLimiterRegistry rateLimiters = RateLimiterRegistry.ofDefaults();
    private final CircuitBreakerRegistry circuitBreakers = CircuitBreakerRegistry.ofDefaults();
    private final BulkheadRegistry bulkheads = BulkheadRegistry.ofDefaults();
    private final TimeLimiterRegistry timeLimiters = TimeLimiterRegistry.ofDefaults();
    private final RetryRegistry retriers = RetryRegistry.ofDefaults();

    public void reset() {
        rateLimiters.getAllRateLimiters().forEach(rateLimiter -> rateLimiters.remove(rateLimiter.getName()));
        circuitBreakers.getAllCircuitBreakers().forEach(circuitBreaker -> circuitBreakers.remove(circuitBreaker.getName()));
        bulkheads.getAllBulkheads().forEach(bulkhead -> bulkheads.remove(bulkhead.getName()));
        timeLimiters.getAllTimeLimiters().forEach(timeLimiter -> timeLimiters.remove(timeLimiter.getName()));
        retriers.getAllRetries().forEach(retry -> retriers.remove(retry.getName()));
    }
}
