/*
 *    Copyright 2019 ART
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package ru.art.config.extensions.kafka;

import lombok.experimental.*;
import ru.art.config.*;
import ru.art.core.annotation.*;
import static ru.art.config.extensions.ConfigExtensions.*;
import static ru.art.config.extensions.kafka.KafkaConfigKeys.*;

@PublicApi
@UtilityClass
public class KafkaConfigProvider {
    public static Config kafkaBrokerConfig() {
        return configInner(KAFKA_BROKER_SECTION_ID);
    }

    public static Config kafkaConsumerConfig() {
        return configInner(KAFKA_CONSUMER_SECTION_ID);
    }

    public static Config kafkaProducerConfig() {
        return configInner(KAFKA_PRODUCERS_SECTION_ID);
    }
}
