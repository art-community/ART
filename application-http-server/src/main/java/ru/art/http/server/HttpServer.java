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

package ru.art.http.server;

import lombok.*;
import org.apache.catalina.*;
import org.apache.catalina.connector.*;
import org.apache.catalina.core.*;
import org.apache.catalina.servlets.*;
import org.apache.catalina.startup.*;
import org.apache.coyote.http2.*;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.descriptor.web.*;
import org.zalando.logbook.servlet.*;
import ru.art.core.constants.*;
import ru.art.http.constants.*;
import ru.art.http.server.exception.*;
import ru.art.http.server.interceptor.*;
import ru.art.http.server.model.*;
import ru.art.http.server.path.*;
import ru.art.http.server.specification.*;
import static java.lang.System.*;
import static java.text.MessageFormat.*;
import static java.util.Objects.*;
import static java.util.logging.LogManager.*;
import static java.util.stream.Collectors.*;
import static lombok.AccessLevel.*;
import static ru.art.core.checker.CheckerForEmptiness.*;
import static ru.art.core.constants.InterceptionStrategy.*;
import static ru.art.core.constants.NetworkConstants.*;
import static ru.art.core.constants.StringConstants.*;
import static ru.art.core.context.Context.*;
import static ru.art.core.extension.EqualsCheckingExtensions.*;
import static ru.art.core.factory.CollectionsFactory.*;
import static ru.art.http.constants.HttpCommonConstants.*;
import static ru.art.http.constants.HttpInterceptorType.*;
import static ru.art.http.server.HttpMetricsBinder.*;
import static ru.art.http.server.builder.HttpPathBuilder.*;
import static ru.art.http.server.builder.HttpUrlBuilder.*;
import static ru.art.http.server.constants.HttpServerExceptionMessages.*;
import static ru.art.http.server.constants.HttpServerLoggingMessages.*;
import static ru.art.http.server.constants.HttpServerModuleConstants.*;
import static ru.art.http.server.model.HttpService.*;
import static ru.art.http.server.module.HttpServerModule.*;
import static ru.art.logging.LoggingModule.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;

@SuppressWarnings("Duplicates")
public class HttpServer {
    private final Map<String, InterceptionStrategy> requestInterceptionsResult = concurrentHashMap();
    private final Map<String, InterceptionStrategy> responseInterceptionsResult = concurrentHashMap();
    private final Deque<HttpServerPathInterceptor> requestInterceptors;
    private final Deque<HttpServerPathInterceptor> responseInterceptors;
    private final Set<String> cancelablePaths = setOf();
    private final Tomcat tomcat;
    @Getter(lazy = true, value = PRIVATE)
    private final static Logger logger = loggingModule().getLogger(HttpServer.class);

    private HttpServer(Tomcat tomcat) {
        this.tomcat = tomcat;
        tomcat.setBaseDir(getProperty(TEMP_DIR_KEY));
        requestInterceptors = dequeOf();
        responseInterceptors = dequeOf();
        httpServerModuleState().setServer(this);
    }

    public static HttpServer httpServer() {
        try {
            return new HttpServer(new Tomcat());
        } catch (Throwable throwable) {
            throw new HttpServerException(TOMCAT_INITIALIZATION_FAILED, throwable);
        }
    }

    public static HttpServer startHttpServer() {
        try {
            HttpServer httpServer = httpServer();
            httpServer.startup();
            return httpServer;
        } catch (Throwable throwable) {
            throw new HttpServerException(TOMCAT_INITIALIZATION_FAILED, throwable);
        }
    }

    private static Map<String, HttpMethodGroup> groupHttpMethods(HttpService httpService) {
        return httpService
                .getHttpMethods()
                .stream()
                .collect(groupingBy(method -> method.getPath().getContextPath()))
                .entrySet()
                .stream()
                .collect(toMap(Map.Entry::getKey, entry -> HttpMethodGroup.builder()
                        .methods(entry.getValue().stream().collect(toMap(HttpMethod::getMethodType, method -> method)))
                        .build()));
    }

    public void await() {
        tomcat.getServer().await();
    }

    public boolean isWorking() {
        return tomcat.getServer().getState().isAvailable();
    }

    public void restart() {
        long millis = currentTimeMillis();
        try {
            tomcat.stop();
            startHttpServer();
            getLogger().info(format(TOMCAT_RESTARTED_MESSAGE, currentTimeMillis() - millis));
        } catch (Throwable throwable) {
            getLogger().error(TOMCAT_RESTARTING_FAILED);
        }
    }

    public void stop() {
        long millis = currentTimeMillis();
        try {
            tomcat.stop();
            getLogger().info(format(TOMCAT_STOPPED_MESSAGE, currentTimeMillis() - millis));
        } catch (Throwable throwable) {
            getLogger().error(TOMCAT_STOPPING_FAILED);
        }
    }

    private Context createContext() {
        StandardContext context = (StandardContext) tomcat.addContext(EMPTY_STRING, getProperty(TEMP_DIR_KEY));
        context.setAllowCasualMultipartParsing(httpServerModule().isAllowCasualMultipartParsing());
        if (cancelablePaths.contains(SLASH)) {
            getLogger().info(HTTP_SERVICES_CANCELED);
            return context;
        }
        context.setClearReferencesObjectStreamClassCaches(false);
        context.setClearReferencesRmiTargets(false);
        context.setClearReferencesThreadLocals(false);
        registerHttpServices(context);
        return context;
    }

    private void registerHttpServices(Context ctx) {
        httpServices().forEach(service -> registerService(service.getServiceId(), service, ctx));
    }

    private void registerService(String serviceId, HttpServiceSpecification serviceSpec, Context ctx) {
        HttpService httpService = serviceSpec.getHttpService();
        if (cancelablePaths.contains(httpService.getPath())) {
            getLogger().info(format(HTTP_SERVICE_CANCELED, serviceId, httpService.getPath()));
            return;
        }

        Set<String> registeredServletPaths = setOf();
        Map<String, HttpMethodGroup> methodsGroup = groupHttpMethods(httpService);
        for (HttpMethod method : httpService.getHttpMethods()) {
            if (registeredServletPaths.contains(method.getPath().getContextPath())) {
                continue;
            }
            MimeToContentTypeMapper consumesMimeType = method.getConsumesMimeType();
            MimeToContentTypeMapper producesMimeType = method.getProducesMimeType();
            if (nonNull(consumesMimeType) && !httpServerModule().getContentMappers().containsKey(consumesMimeType.getMimeType())) {
                throw new HttpServerException(format(CONSUMES_CONTENT_TYPE_NOT_SUPPORTED, consumesMimeType));
            }
            if (nonNull(producesMimeType) && !httpServerModule().getContentMappers().containsKey(producesMimeType.getMimeType())) {
                throw new HttpServerException(format(PRODUCES_CONTENT_TYPE_NOT_SUPPORTED, producesMimeType));
            }
            HttpPath path = buildHttpPath(extractHttpServicePath(serviceSpec), method.getPath());
            if (cancelablePaths.contains(path.toString())) {
                getLogger().info(format(HTTP_SERVICE_METHOD_CANCELED, serviceId, method.getMethodId(), httpService.getPath()));
                continue;
            }
            Wrapper defaultServlet = ctx.createWrapper();
            String servletName = DEFAULT_SERVLET + path.getContextPath();
            StringBuilder servletPath = new StringBuilder().append(path.getContextPath());
            if (!path.getParameters().isEmpty()) {
                servletPath.append(SLASH).append(WILDCARD);
            }
            defaultServlet.setName(servletName);
            defaultServlet.setServletClass(DefaultServlet.class.getName());
            Map<HttpMethodType, HttpMethod> httpMethods = methodsGroup.get(method.getPath().getContextPath()).getMethods();
            defaultServlet.setServlet(createHttpServlet(httpMethods, path, serviceId));
            ctx.addChild(defaultServlet);
            ctx.addServletMappingDecoded(servletPath.toString(), servletName);
            logHttpServiceRegistration(serviceSpec, httpMethods, path);
            registeredServletPaths.add(path.getContextPath());
        }

    }

    private void logHttpServiceRegistration(HttpServiceSpecification serviceSpec, Map<HttpMethodType, HttpMethod> methods, HttpPath path) {
        try {
            UrlInfo urlInfo = UrlInfo.builder()
                    .port(httpServerModule().getPort())
                    .serverName(BROADCAST_IP_ADDRESS.equals(httpServerModule().getHost())
                            ? contextConfiguration().getIpAddress()
                            : httpServerModule().getHost())
                    .scheme(HTTP_SCHEME)
                    .uri(path.toString())
                    .build();
            getLogger().info(format(HTTP_SERVICE_REGISTERING_MESSAGE, buildUrl(urlInfo), serviceSpec.getServiceId(), methods.keySet()));
        } catch (Throwable throwable) {
            throw new HttpServerException(throwable);
        }

    }

    private HttpServiceServlet createHttpServlet(Map<HttpMethodType, HttpMethod> methods, HttpPath path, String serviceId) {
        return new HttpServiceServlet(methods.entrySet()
                .stream()
                .collect(toMap(Map.Entry::getKey, methodEntry -> HttpServletCommand.builder()
                        .path(path.getContextPath())
                        .consumesMimeType(methodEntry.getValue().getConsumesMimeType())
                        .producesMimeType(methodEntry.getValue().getProducesMimeType())
                        .ignoreRequestAcceptType(methodEntry.getValue().isIgnoreRequestAcceptType())
                        .ignoreRequestContentType(methodEntry.getValue().isIgnoreRequestContentType())
                        .serviceId(serviceId)
                        .httpMethod(methodEntry.getValue())
                        .build())));
    }

    private String extractHttpServicePath(HttpServiceSpecification serviceSpec) {
        return serviceSpec.getHttpService().getPath();
    }

    private void startup() throws LifecycleException {
        final long timestamp = currentTimeMillis();
        getLogManager().reset();
        configureConnector();
        initializeInterceptors();
        Context context = createContext();
        registerInterceptors(context);
        if (httpServerModule().isEnableMetrics()) {
            bindHttpMetrics(context.getManager());
        }
        tomcat.start();
        getLogger().info(format(TOMCAT_STARTED_MESSAGE, currentTimeMillis() - timestamp));
    }

    private void configureConnector() {
        Connector connector = new Connector();
        connector.setAttribute(ADDRESS_ATTRIBUTE, httpServerModule().getHost());
        connector.setAttribute(MAX_THREADS_ATTRIBUTE, httpServerModule().getMaxThreadsCount());
        connector.setAttribute(MIN_SPARE_THREADS_ATTRIBUTE, httpServerModule().getMinSpareThreadsCount());
        connector.setAttribute(MAX_IDLE_TIME_ATTRIBUTE, httpServerModule().getMaxIdleTime());
        connector.setAttribute(PRESTART_MIN_SPARE_THREADS_ATTRIBUTE, httpServerModule().isPrestartMinSpareThreads());
        connector.addUpgradeProtocol(new Http2Protocol());
        connector.setPort(httpServerModule().getPort());
        tomcat.getService().addConnector(connector);
        tomcat.setConnector(connector);
    }

    private void initializeInterceptors() {
        requestInterceptors.addAll(httpServerModule()
                .getRequestInterceptors()
                .stream()
                .map(interceptor -> new HttpServerPathInterceptor(interceptor, SLASH))
                .collect(toList()));
        responseInterceptors.addAll(httpServerModule()
                .getResponseInterceptors()
                .stream()
                .map(interceptor -> new HttpServerPathInterceptor(interceptor, SLASH))
                .collect(toList()));
        addServicesInterceptors();
    }

    private void registerInterceptors(Context context) {
        int order = 0;
        while (!requestInterceptors.isEmpty()) {
            HttpServerPathInterceptor interceptor = requestInterceptors.removeFirst();
            registerBeforeInterceptor(context, interceptor, interceptor + UNDERSCORE + BEFORE_REQUEST_HANDLING + UNDERSCORE + order);
            order++;
            if (interceptor.getInterceptor().getStrategy() == PROCESS_HANDLING) break;
            if (interceptor.getInterceptor().getStrategy() == STOP_HANDLING) {
                cancelablePaths.add(interceptor.getPath());
                addLoggingFilter(context);
                return;
            }
        }
        addLoggingFilter(context);
        order = 0;
        while (!responseInterceptors.isEmpty()) {
            HttpServerPathInterceptor interceptor = responseInterceptors.removeLast();
            registerAfterInterceptor(context, interceptor, interceptor + UNDERSCORE + AFTER_REQUEST_HANDLING + UNDERSCORE + order);
            order++;
            if (interceptor.getInterceptor().getStrategy() == PROCESS_HANDLING) break;
            if (interceptor.getInterceptor().getStrategy() == STOP_HANDLING) return;
        }
    }

    private void addLoggingFilter(Context context) {
        FilterDef def = new FilterDef();
        def.setFilterName(LogbookFilter.class.getSimpleName());
        def.setFilter(new LogbookFilter(httpServerModule().getLogbook()));
        FilterMap map = new FilterMap();
        map.setFilterName(LogbookFilter.class.getSimpleName());
        map.addURLPattern(WILDCARD);
        context.addFilterDef(def);
        context.addFilterMap(map);
    }

    private void addServicesInterceptors() {
        httpServices().stream()
                .map(HttpServiceSpecification::getHttpService)
                .forEach(this::addServiceInterceptors);
    }

    private void addServiceInterceptors(HttpService httpService) {
        requestInterceptors.addAll(httpService
                .getRequestInterceptors()
                .stream()
                .map(interceptor -> new HttpServerPathInterceptor(interceptor, httpService.getPath()))
                .collect(toList()));
        responseInterceptors.addAll(httpService
                .getResponseInterceptors()
                .stream()
                .map(interceptor -> new HttpServerPathInterceptor(interceptor, httpService.getPath()))
                .collect(toList()));
        httpService.getHttpMethods().forEach(method -> addMethodInterceptors(httpService.getPath(), method));
    }

    private void addMethodInterceptors(String servicePath, HttpMethod httpMethod) {
        requestInterceptors.addAll(httpMethod.getRequestInterceptors()
                .stream()
                .map(interceptor -> createHttpInterceptor(servicePath, httpMethod, interceptor))
                .collect(toList()));
        responseInterceptors.addAll(httpMethod.getResponseInterceptors()
                .stream()
                .map(interceptor -> createHttpInterceptor(servicePath, httpMethod, interceptor))
                .collect(toList()));
    }

    private HttpServerPathInterceptor createHttpInterceptor(String servicePath, HttpMethod httpMethod, HttpServerInterceptor interceptor) {
        String path = ifEmpty(buildHttpPath(servicePath, httpMethod.getPath()).getContextPath(), SLASH);
        return new HttpServerPathInterceptor(interceptor, path);
    }

    private void registerBeforeInterceptor(Context context, HttpServerPathInterceptor serverPathInterceptor, String filterName) {
        String urlPattern = ifNotEquals(serverPathInterceptor.getPath(), SLASH, serverPathInterceptor.getPath() + SLASH) + WILDCARD;
        getLogger().info(format(REGISTERING_HTTP_INTERCEPTOR, filterName, urlPattern));
        FilterDef def = new FilterDef();
        def.setFilterName(filterName);
        Filter filter = (request, response, chain) -> {
            InterceptionStrategy strategy = requestInterceptionsResult.get(filterName);
            if (isNull(strategy) || strategy == NEXT_INTERCEPTOR) {
                InterceptionStrategy nextInterceptionStrategy = serverPathInterceptor.getInterceptor().intercept((HttpServletRequest) request, (HttpServletResponse) response);
                requestInterceptionsResult.put(filterName, nextInterceptionStrategy);
                chain.doFilter(request, response);
                return;
            }
            if (strategy == PROCESS_HANDLING) {
                chain.doFilter(request, response);
            }
        };
        def.setFilter(filter);
        FilterMap map = new FilterMap();
        map.setFilterName(filterName);
        map.addURLPattern(urlPattern);
        context.addFilterDef(def);
        context.addFilterMap(map);
    }

    private void registerAfterInterceptor(Context context, HttpServerPathInterceptor serverInterceptor, String filterName) {
        String urlPattern = ifNotEquals(serverInterceptor.getPath(), SLASH, serverInterceptor.getPath() + SLASH) + WILDCARD;
        getLogger().info(format(REGISTERING_HTTP_INTERCEPTOR, filterName, urlPattern));
        FilterDef def = new FilterDef();
        def.setFilterName(filterName);
        def.setFilter((request, response, chain) -> {
            InterceptionStrategy strategy = responseInterceptionsResult.get(filterName);
            if (isNull(strategy) || strategy == NEXT_INTERCEPTOR) {
                chain.doFilter(request, response);
                InterceptionStrategy nextInterceptionStrategy = serverInterceptor.getInterceptor().intercept((HttpServletRequest) request, (HttpServletResponse) response);
                responseInterceptionsResult.put(filterName,  nextInterceptionStrategy);
                return;
            }
            if (strategy == PROCESS_HANDLING) {
                chain.doFilter(request, response);
            }
        });
        FilterMap map = new FilterMap();
        map.setFilterName(filterName);
        map.addURLPattern(urlPattern);
        context.addFilterDef(def);
        context.addFilterMap(map);
    }
}
