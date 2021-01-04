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

package ru.art.http.server.service;

import com.mitchellbosecke.pebble.*;
import com.mitchellbosecke.pebble.loader.*;
import lombok.experimental.*;
import ru.art.entity.*;
import ru.art.http.server.HttpServerModuleConfiguration.*;
import static java.util.Objects.*;
import static ru.art.core.caster.Caster.*;
import static ru.art.core.checker.CheckerForEmptiness.*;
import static ru.art.core.constants.ArrayConstants.*;
import static ru.art.core.constants.StringConstants.*;
import static ru.art.core.context.Context.*;
import static ru.art.core.extension.InputStreamExtensions.*;
import static ru.art.core.extension.NullCheckingExtensions.*;
import static ru.art.core.factory.CollectionsFactory.*;
import static ru.art.entity.CollectionValuesFactory.*;
import static ru.art.entity.PrimitivesFactory.*;
import static ru.art.http.server.constants.HttpServerExceptionMessages.*;
import static ru.art.http.server.constants.HttpServerModuleConstants.HttpResourceServiceConstants.HttpResourceType.*;
import static ru.art.http.server.module.HttpServerModule.*;
import static ru.art.logging.LoggingModule.*;
import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;

@UtilityClass
public class HttpResourceService {
    public static Value getHttpResource(String resource) {
        return getHttpResource(resource, contextConfiguration().getCharset(), httpServerModule().getResourceConfiguration());
    }

    public static Value getHttpResource(String resource, HttpResourceConfiguration resourceConfiguration) {
        return getHttpResource(resource, contextConfiguration().getCharset(), resourceConfiguration);
    }

    public static Value getHttpResource(String request, Charset charset, HttpResourceConfiguration resourceConfiguration) {
        String resourcePath = ifEmpty(cast(request), resourceConfiguration.getDefaultResource().getPath());
        Map<String, HttpResourceExtensionMapping> extensionMappings = resourceConfiguration.getResourceExtensionMappings();
        Map<String, HttpResource> resourcePathMappings = resourceConfiguration.getResourcePathMappings();

        HttpResource resource;
        if (nonNull(resource = resourcePathMappings.get(resourcePath))) {
            return cast(resource.getType() == STRING
                    ? stringPrimitive(getStringResource(resource.getPath(), getOrElse(resource.getCharset(), charset), resourceConfiguration))
                    : byteCollection(getBinaryResource(resource.getPath(), resourceConfiguration)));
        }

        HttpResourceExtensionMapping resourceExtensionMapping;
        if (resourcePath.contains(DOT) && nonNull(resourceExtensionMapping = extensionMappings.get(resourcePath
                .substring(resourcePath.lastIndexOf(DOT))
                .toLowerCase()))) {
            HttpResource customResource;
            if (nonNull(customResource = resourceExtensionMapping.getCustomHttpResource())) {
                return cast(customResource.getType() == STRING
                        ? stringPrimitive(getStringResource(customResource.getPath(), getOrElse(customResource.getCharset(), charset), resourceConfiguration))
                        : byteCollection(getBinaryResource(customResource.getPath(), resourceConfiguration)));
            }
            return cast(resourceExtensionMapping.getResourceType() == STRING
                    ? stringPrimitive(getStringResource(resourcePath, getOrElse(resourceExtensionMapping.getMimeType().getCharset(), charset), resourceConfiguration))
                    : byteCollection(getBinaryResource(resourcePath, resourceConfiguration)));
        }
        HttpResource defaultResource = resourceConfiguration.getDefaultResource();
        return cast(defaultResource.getType() == STRING
                ? stringPrimitive(getStringResource(defaultResource.getPath(), getOrElse(defaultResource.getCharset(), charset), resourceConfiguration))
                : byteCollection(getBinaryResource(defaultResource.getPath(), resourceConfiguration)));

    }


    public static String getStringResource(String resource) {
        return getStringResource(resource, contextConfiguration().getCharset(), httpServerModule().getResourceConfiguration());
    }

    public static String getStringResource(String resource, HttpResourceConfiguration resourceConfiguration) {
        return getStringResource(resource, contextConfiguration().getCharset(), resourceConfiguration);
    }

    public static String getStringResource(String resource, Charset charset, HttpResourceConfiguration resourceConfiguration) {
        if (resourceConfiguration.getResourceExtensionMappings().keySet().stream().noneMatch(resource::endsWith)) {
            return EMPTY_STRING;
        }
        URL resourceUrl = mapResourceUrl(resource, resourceConfiguration);
        if (isNull(resourceUrl)) {
            return EMPTY_STRING;
        }
        try (InputStream pageStream = resourceUrl.openStream()) {
            String resourceContent = resolveResourceContent(pageStream, charset, resourceConfiguration);
            for (String extension : resourceConfiguration.getTemplatingResourceExtensions()) {
                if (resource.endsWith(extension)) {
                    Map<String, Object> templateContext = mapOf();
                    resourceConfiguration.getTemplateResourceVariables().forEach(templateContext::put);
                    StringLoader templateLoader = new StringLoader();
                    templateLoader.setCharset(charset.name());
                    StringWriter templateWriter = new StringWriter();
                    new PebbleEngine.Builder()
                            .loader(templateLoader)
                            .autoEscaping(false)
                            .cacheActive(false)
                            .build()
                            .getTemplate(resourceContent)
                            .evaluate(templateWriter, templateContext);
                    return templateWriter.toString();
                }
            }
            return resourceContent;
        } catch (Throwable throwable) {
            loggingModule()
                    .getLogger(HttpResourceService.class)
                    .error(RESOURCE_ERROR, throwable);
        }
        return EMPTY_STRING;
    }


    public static byte[] getBinaryResource(String resource) {
        return getBinaryResource(resource, httpServerModule().getResourceConfiguration());
    }

    public static byte[] getBinaryResource(String resource, HttpResourceConfiguration resourceConfiguration) {
        if (resourceConfiguration.getResourceExtensionMappings().keySet().stream().noneMatch(resource::endsWith)) {
            return EMPTY_BYTES;
        }
        URL resourceUrl = mapResourceUrl(resource, resourceConfiguration);
        return getBinaryResourceContent(resourceUrl, resourceConfiguration);
    }


    private static byte[] getBinaryResourceContent(URL resourceUrl, HttpResourceConfiguration resourceConfiguration) {
        try (InputStream pageStream = resourceUrl.openStream()) {
            return resolveResourceBinaryContent(pageStream, resourceConfiguration);
        } catch (Throwable throwable) {
            loggingModule()
                    .getLogger(HttpResourceService.class)
                    .error(RESOURCE_ERROR, throwable);
        }
        return EMPTY_BYTES;
    }

    private static String resolveResourceContent(InputStream pageStream, Charset charset, HttpResourceConfiguration resourceConfiguration) {
        return new String(resolveResourceBinaryContent(pageStream, resourceConfiguration), charset);
    }

    private static byte[] resolveResourceBinaryContent(InputStream pageStream, HttpResourceConfiguration resourceConfiguration) {
        return toByteArray(pageStream, resourceConfiguration.getResourceBufferSize());
    }

    private static URL mapResourceUrl(String resource, HttpResourceConfiguration resourceConfiguration) {
        HttpResource resourcePathMapping = resourceConfiguration.getResourcePathMappings().get(resource);
        if (isNotEmpty(resourcePathMapping)) {
            resource = resourcePathMapping.getPath();
        }
        return HttpResourceService.class.getClassLoader().getResource(resource);
    }
}
