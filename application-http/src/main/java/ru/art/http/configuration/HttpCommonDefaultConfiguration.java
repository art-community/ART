
package ru.art.http.configuration;

import lombok.*;
import org.zalando.logbook.*;
import ru.art.core.mime.*;
import ru.art.entity.Value;
import ru.art.entity.interceptor.*;
import ru.art.http.constants.*;
import ru.art.http.logger.*;
import ru.art.http.mapper.*;
import ru.art.logging.*;
import static ru.art.core.factory.CollectionsFactory.*;
import static ru.art.http.constants.HttpMimeTypes.*;
import static ru.art.http.constants.MimeToContentTypeMapper.*;
import java.util.*;

public class HttpCommonDefaultConfiguration implements HttpModuleConfiguration {
    @Getter
    private final boolean enableRawDataTracing = false;

    @Getter
    private final boolean enableValueTracing = false;

    @Getter
    private final boolean enableMetricsMonitoring = true;

    @Getter
    private final MimeToContentTypeMapper consumesMimeTypeMapper = all();

    @Getter
    private final MimeToContentTypeMapper producesMimeTypeMapper = all();

    @Getter
    private final HttpTextPlainMapper textPlainMapper = new HttpTextPlainMapper();

    @Getter(lazy = true)
    private final Map<MimeType, HttpContentMapper> contentMappers = getMimeTypeHttpContentMapperMapBuilder();

    @Getter(lazy = true)
    private final List<ValueInterceptor<Value, Value>> requestValueInterceptors = getValueInterceptors();

    @Getter(lazy = true)
    private final List<ValueInterceptor<Value, Value>> responseValueInterceptors = getValueInterceptors();

    @Getter(lazy = true)
    private final Logbook logbook = Logbook.builder().writer(new ZalangoLogbookLogWriter(this::isEnableRawDataTracing)).build();

    private List<ValueInterceptor<Value, Value>> initializeValueInterceptors() {
        return linkedListOf(new LoggingValueInterceptor<>(this::isEnableValueTracing));
    }

    private List<ValueInterceptor<Value, Value>> getValueInterceptors() {
        return initializeValueInterceptors();
    }

    private Map<MimeType, HttpContentMapper> getMimeTypeHttpContentMapperMapBuilder() {
        return mapOf(ALL, new HttpContentMapper(getTextPlainMapper(), getTextPlainMapper()));
    }
}
