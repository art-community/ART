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

package ru.art.config.extensions.metrics;

import com.codahale.metrics.*;
import io.github.mweirauch.micrometer.jvm.extras.*;
import io.micrometer.core.instrument.binder.*;
import io.micrometer.core.instrument.binder.jvm.*;
import io.micrometer.core.instrument.binder.system.*;
import io.micrometer.prometheus.*;
import lombok.*;
import ru.art.core.context.*;
import ru.art.metrics.configuration.MetricModuleConfiguration.*;
import ru.art.metrics.configurator.*;
import static io.micrometer.prometheus.PrometheusConfig.DEFAULT;
import static ru.art.core.context.Context.contextConfiguration;
import static ru.art.core.factory.CollectionsFactory.setOf;
import static ru.art.metrics.configurator.PrometheusRegistryConfigurator.*;
import static ru.art.metrics.configurator.PrometheusRegistryConfigurator.prometheusRegistryForApplication;
import static ru.art.metrics.factory.DropwizardMetricRegistryFactory.createDefaultDropwizardMetricRegistry;
import java.util.*;

@Getter
public class MetricsAgileConfiguration extends MetricModuleDefaultConfiguration {
    private Set<MeterBinder> meterBinders = super.getMeterBinders();
    private PrometheusMeterRegistry prometheusMeterRegistry = super.getPrometheusMeterRegistry();
    private MetricRegistry dropwizardMetricRegistry = super.getDropwizardMetricRegistry();

    @Override
    public void refresh() {
        prometheusMeterRegistry = prometheusRegistryForApplication(contextConfiguration().getMainModuleId(), dropwizardMetricRegistry);
        meterBinders.forEach(meter -> meter.bindTo(prometheusMeterRegistry));
    }
}
