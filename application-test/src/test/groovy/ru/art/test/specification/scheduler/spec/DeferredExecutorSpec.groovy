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

package ru.art.test.specification.scheduler.spec

import ru.art.test.specification.scheduler.model.DeferredEventResult
import spock.lang.IgnoreIf
import spock.lang.Specification
import spock.lang.Unroll

import static java.lang.System.getenv
import static java.lang.Thread.sleep
import static java.time.LocalDateTime.now
import static ru.art.task.deferred.executor.DeferredExecutorImplementation.builder as deferredExecutorBuilder
import static ru.art.test.specification.scheduler.operation.DeferredExecutorSpecOperations.*

@IgnoreIf({ getenv('TRAVIS') as boolean })
class DeferredExecutorSpec extends Specification {
    @Unroll
    "Should execute immediately #eventCount events"() {
        setup: "Create executor "
        def executor = deferredExecutorBuilder().withExceptionHandler { println "Exception: $it" }.build()
        def time = now()
        def triggerTimes = (0..eventCount - 1).collect { time }
        def expectedResults = createExpectedResultsQueue()
        triggerTimes.each { expectedResults.add createDeferredEventResult(it, expectedResults.size()) }
        def actualResults = new ArrayDeque<DeferredEventResult>()

        when:
        println "Executor isWorking with $eventCount events"
        runInJoinedThread { triggerTimes.collect { addEventToExecutor executor, it, actualResults }*.get() }

        then:
        actualResults != null
        actualResults.size() == expectedResults.size()
        println "Executor finished with ${actualResults.size()}  events"

        cleanup:
        executor.shutdown()

        where:
        eventCount = 2
    }

    @Unroll
    "Should execute deferred #eventCount events with second interval"() {
        setup:
        def executor = deferredExecutorBuilder().threadPoolCoreSize(10).withExceptionHandler { println "Exception: $it" }.build()
        def time = now()
        def triggerTimes = (0..eventCount - 1).collect { time.plusSeconds it }
        def expectedResults = createExpectedResultsQueue()
        triggerTimes.each { expectedResults.add createDeferredEventResult(it, expectedResults.size()) }
        def actualResults = new ArrayDeque<DeferredEventResult>()

        when:
        println "Executor isWorking with $eventCount events"
        runInJoinedThread { triggerTimes.collect { addEventToExecutor executor, it, actualResults }*.get() }

        then:
        actualResults != null
        actualResults.size() == expectedResults.size()
        while (!expectedResults.empty) assert expectedResults.poll() == actualResults.poll()
        println "Executor finished with ${actualResults.size()}  events"

        cleanup:
        executor.shutdown()

        where:
        eventCount = 2
    }

    @Unroll
    "Should execute immediately #eventCount events from two threads"() {
        setup:
        def executor = deferredExecutorBuilder().withExceptionHandler { println "Exception: $it" }.build()
        def time = now()
        def triggerTimes = (0..eventCount - 1).collect { time }
        def expectedResults = createExpectedResultsQueue()
        triggerTimes.each { expectedResults.add createDeferredEventResult(it, expectedResults.size()) }
        def actualResults = new ArrayDeque<DeferredEventResult>()

        when:
        println "Executor isWorking with $eventCount events"
        runInJoinedThread { triggerTimes.stream().limit(eventCount / 2 as long).map { addEventToExecutor executor, it, actualResults }*.get() }
        runInJoinedThread {
            triggerTimes.stream().skip(eventCount / 2 as long).limit(eventCount / 2 as long).map {
                addEventToExecutor executor, it, actualResults
            }*.get()
        }

        then:
        actualResults != null
        actualResults.size() == expectedResults.size()
        while (!expectedResults.empty) assert expectedResults.poll() == actualResults.poll()
        println "Executor finished with ${actualResults.size()}  events"

        cleanup:
        executor.shutdown()

        where:
        eventCount = 2
    }

    @Unroll
    "Should execute deferred #eventCount events with second interval from two threads"() {
        setup:
        def executor = deferredExecutorBuilder().withExceptionHandler { println "Exception: $it" }.build()
        def time = now()
        def triggerTimes = (0..eventCount - 1).collect { time.plusSeconds it }
        def expectedResults = createExpectedResultsQueue()
        triggerTimes.each { expectedResults.add createDeferredEventResult(it, expectedResults.size()) }
        def actualResults = new ArrayDeque<DeferredEventResult>()

        when:
        println "Executor isWorking with $eventCount events"
        runInJoinedThread { triggerTimes.stream().limit(eventCount / 2 as long).map { addEventToExecutor executor, it, actualResults }*.get() }
        runInJoinedThread {
            triggerTimes.stream().skip(eventCount / 2 as long).limit(eventCount / 2 as long).map {
                addEventToExecutor executor, it, actualResults
            }*.get()
        }

        then:
        actualResults != null
        actualResults.size() == expectedResults.size()
        while (!expectedResults.empty) assert expectedResults.poll() == actualResults.poll()
        println "Executor finished with ${actualResults.size()}  events"

        cleanup:
        executor.shutdown()

        where:
        eventCount = 4
    }

    @Unroll
    "Should execute deferred #eventCount events with max count overload: #maxCount"() {
        setup:
        def executor = deferredExecutorBuilder().eventsQueueMaxSize(maxCount).withExceptionHandler { println "Exception: $it" }.build()
        def time = now()
        def triggerTimes = (0..eventCount - 1).collect { time.plusSeconds it }
        def expectedResults = createExpectedResultsQueue()
        triggerTimes.each { expectedResults.add createDeferredEventResult(it, expectedResults.size()) }
        def actualResults = new ArrayDeque<DeferredEventResult>()

        when:
        println "Executor isWorking with $eventCount events"
        runInJoinedThread { triggerTimes.collect { addEventToExecutor executor, it, actualResults }*.get() }

        then:
        actualResults != null
        actualResults.size() == expectedResults.size()
        println "Executor finished with ${actualResults.size()}  events"

        cleanup:
        executor.shutdown()

        where:
        eventCount || maxCount
        4          || 2
    }

    @Unroll
    "Should execute deferred #eventCount events with max count overload: #maxCount and two writer threads"() {
        setup:
        def executor = deferredExecutorBuilder().eventsQueueMaxSize(maxCount).withExceptionHandler { println "Exception: $it" }.build()
        def time = now()
        def triggerTimes = (0..eventCount - 1).collect { time.plusSeconds it }
        def expectedResults = createExpectedResultsQueue()
        triggerTimes.each { expectedResults.add createDeferredEventResult(it, expectedResults.size()) }
        def actualResults = new ArrayDeque<DeferredEventResult>()

        when:
        println "Executor isWorking with ${triggerTimes.size()} events"
        runInJoinedThread { triggerTimes.stream().limit(eventCount / 2 as long).map { addEventToExecutor executor, it, actualResults }*.get() }
        runInJoinedThread {
            triggerTimes.stream().skip(eventCount / 2 as long).limit(eventCount / 2 as long).map {
                addEventToExecutor executor, it, actualResults
            }*.get()
        }

        then:
        actualResults != null
        actualResults.size() == expectedResults.size()
        println "Executor finished with ${actualResults.size()}  events"

        cleanup:
        executor.shutdown()

        where:
        eventCount || maxCount
        4          || 2
    }

    @Unroll
    "Should execute #infinityCount infinity deferred events and #longCount long deferred events"() {
        setup:
        def executor = deferredExecutorBuilder().withExceptionHandler { println "Exception: $it" }.build()
        def time = now()
        def infinityEventsTriggerTimes = (0..infinityCount - 1).collect { time.plusSeconds it }
        def longEventsTriggerTimes = (0..longCount - 1).collect { time.plusSeconds 1 + it }
        def expectedResults = createExpectedResultsQueue()
        infinityEventsTriggerTimes.each { expectedResults.add createDeferredEventResult(it, expectedResults.size()) }
        longEventsTriggerTimes.each { expectedResults.add createDeferredEventResult(it, expectedResults.size()) }
        def actualResults = new ArrayDeque<DeferredEventResult>()

        when:
        println "Executor isWorking with $infinityCount infinity events and $longCount long events"
        runInJoinedThread {
            infinityEventsTriggerTimes.collect {
                executor.submit createInfinityDeferredEventTask(it, {
                    addEventResult actualResults;
                    actualResults.size() - 1
                }), it
            }
        }
        runInJoinedThread {
            longEventsTriggerTimes.collect {
                executor.submit createLongDeferredEventTask(it, {
                    addEventResult actualResults;
                    actualResults.size() - 1
                }), it
            }
        }
        sleep 5000


        then:
        actualResults != null
        actualResults.size() == expectedResults.size()
        println "Executor finished with ${actualResults.size()}  events"

        cleanup:
        executor.shutdown()

        where:
        infinityCount || longCount
        1             || 1
    }

    @Unroll
    "Should execute deferred #eventCount events with returning values #expectedValues"() {
        setup:
        def executor = deferredExecutorBuilder().threadPoolCoreSize(10).withExceptionHandler { println "Exception: $it" }.build()
        def time = now()
        def triggerTimes = (0..eventCount - 1).collect { time.plusSeconds it }
        def expectedResults = createExpectedResultsQueue()
        def expectedValuesCloned = expectedValues.clone()
        triggerTimes.each { expectedResults.add createDeferredEventResult(it, expectedResults.size(), expectedValuesCloned.poll()) }
        def actualResults = new ArrayDeque<DeferredEventResult>()

        when:
        println "Executor isWorking with $eventCount events"
        runInJoinedThread {
            triggerTimes.collect { addEventToExecutorWithReturningValue executor, it, actualResults, expectedValues.poll() }*.get()
        }

        then:
        actualResults != null
        actualResults.size() == expectedResults.size()
        while (!expectedResults.empty) assert expectedResults.poll() == actualResults.poll()
        println "Executor finished with ${actualResults.size()}  events"

        cleanup:
        executor.shutdown()

        where:
        eventCount || expectedValues
        2          || (1..2).collect { "Value_$it" } as ArrayDeque<String>

    }
}