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

package ru.art.config.extensions.service;

public interface ServiceConfigKeys {
    String DEACTIVATED = "deactivated";
    String DEACTIVATED_METHODS = "deactivatedMethods";
    String SERVICE = "service";

    String CIRCUIT_BREAKER = "circuitBreaker";
    String FAILURE_RATE_THRESHOLD = "circuitBreaker.failureRateThreshold";
    String PERMITTED_NUMBER_OF_CALLS_IN_HALF_OPEN_STATE = "circuitBreaker.permittedNumberOfCallsInHalfOpenState";
    String SLIDING_WINDOW_SIZE = "circuitBreaker.slidingWindow.size";
    String SLIDING_WINDOW_TYPE = "circuitBreaker.slidingWindow.type";
    String SLIDING_WINDOW_MINIMUM_NUMBER_OF_CALLS = "circuitBreaker.slidingWindow.minimumNumberOfCalls";
    String WAIT_DURATION_IN_OPEN_STATE = "circuitBreaker.waitDurationInOpenState";
    String AUTOMATIC_TRANSITION_FROM_OPEN_TO_HALF_OPEN_ENABLED = "circuitBreaker.automaticTransitionFromOpenToHalfOpenEnabled";

    String RATE_LIMITER = "rateLimiter";
    String LIMIT_FOR_PERIOD = "rateLimiter.limitForPeriod";
    String LIMIT_REFRESH_PERIOD = "rateLimiter.limitRefreshPeriod";
    String TIMEOUT_DURATION = "rateLimiter.timeoutDuration";

    String BULKHEAD = "bulkhead";
    String MAX_CONCURRENT_CALLS = "bulkhead.maxConcurrentCalls";
    String MAX_WAIT_DURATION = "bulkhead.maxWaitDuration";

    String RETRYER = "retryer";
    String MAX_ATTEMPTS = "retryer.maxAttempts";
    String WAIT_DURATION = "retryer.waitDuration";
}
