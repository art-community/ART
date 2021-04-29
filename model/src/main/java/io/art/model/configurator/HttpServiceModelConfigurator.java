package io.art.model.configurator;

import io.art.model.modeling.server.*;
import io.art.server.specification.ServiceMethodSpecification.*;
import lombok.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

import static io.art.core.collection.ImmutableMap.*;
import static io.art.core.constants.StringConstants.SLASH;
import static io.art.core.factory.MapFactory.*;
import static io.art.http.constants.HttpModuleConstants.*;
import static io.art.value.constants.ValueModuleConstants.*;
import static lombok.AccessLevel.*;

@Getter(value = PACKAGE)
public class HttpServiceModelConfigurator {
    private final Class<?> serviceClass;
    private final Map<String, HttpServiceMethodModelConfigurator> methods = map();
    private BiFunction<String, ServiceMethodSpecificationBuilder, ServiceMethodSpecificationBuilder> decorator = (id, builder) -> builder;
    private boolean logging;
    private DataFormat defaultDataFormat;

    public HttpServiceModelConfigurator(Class<?> serviceClass){
        this.serviceClass = serviceClass;
    }

    private HttpServiceModelConfigurator method(String methodName, UnaryOperator<HttpServiceMethodModelConfigurator> configurator) {
        HttpServiceMethodModelConfigurator newMethod = new HttpServiceMethodModelConfigurator(methodName)
                .logging(logging)
                .defaultDataFormat(defaultDataFormat);
        methods.putIfAbsent(methodName, configurator.apply(newMethod));
        return this;
    }

    public HttpServiceModelConfigurator get(String methodName){
        return method(methodName, method -> method.httpMethod(HttpMethodType.GET));
    }

    public HttpServiceModelConfigurator post(String methodName){
        return method(methodName, method -> method.httpMethod(HttpMethodType.POST));
    }

    public HttpServiceModelConfigurator put(String methodName){
        return method(methodName, method -> method.httpMethod(HttpMethodType.PUT));
    }

    public HttpServiceModelConfigurator delete(String methodName){
        return method(methodName, method -> method.httpMethod(HttpMethodType.DELETE));
    }

    public HttpServiceModelConfigurator options(String methodName){
        return method(methodName, method -> method.httpMethod(HttpMethodType.OPTIONS));
    }

    public HttpServiceModelConfigurator head(String methodName){
        return method(methodName, method -> method.httpMethod(HttpMethodType.HEAD));
    }

    public HttpServiceModelConfigurator websocket(String methodName){
        return method(methodName, method -> method.httpMethod(HttpMethodType.WEBSOCKET));
    }

    public HttpServiceModelConfigurator file(String httpPath, String filePath){
        return method(httpPath, method -> method
                .httpMethod(HttpMethodType.FILE)
                .filePath(filePath));
    }

    public HttpServiceModelConfigurator directory(String httpPath, String directory){
        return method(
                httpPath.endsWith(SLASH) ?
                        httpPath :
                        httpPath + SLASH, method ->
                method
                        .httpMethod(HttpMethodType.DIRECTORY)
                        .filePath(directory.endsWith(File.separator) ? directory : directory + File.separator));
    }

    public HttpServiceModelConfigurator directory(String httpPath, String directory, String defaultFileName){
        return method(
                httpPath.endsWith(SLASH) ?
                        httpPath :
                        httpPath + SLASH,
                method -> method
                        .httpMethod(HttpMethodType.DIRECTORY)
                        .filePath(directory.endsWith(File.separator) ? directory : directory + File.separator)
                        .directoryDefaultFileName(defaultFileName));
    }

    public HttpServiceModelConfigurator get(String methodName, UnaryOperator<HttpServiceMethodModelConfigurator> configurator){
        return method(methodName, method -> configurator.apply(method.httpMethod(HttpMethodType.GET)));
    }

    public HttpServiceModelConfigurator post(String methodName, UnaryOperator<HttpServiceMethodModelConfigurator> configurator){
        return method(methodName, method -> configurator.apply(method.httpMethod(HttpMethodType.POST)));
    }

    public HttpServiceModelConfigurator put(String methodName, UnaryOperator<HttpServiceMethodModelConfigurator> configurator){
        return method(methodName, method -> configurator.apply(method.httpMethod(HttpMethodType.PUT)));
    }

    public HttpServiceModelConfigurator delete(String methodName, UnaryOperator<HttpServiceMethodModelConfigurator> configurator){
        return method(methodName, method -> configurator.apply(method.httpMethod(HttpMethodType.DELETE)));
    }

    public HttpServiceModelConfigurator options(String methodName, UnaryOperator<HttpServiceMethodModelConfigurator> configurator){
        return method(methodName, method -> configurator.apply(method.httpMethod(HttpMethodType.OPTIONS)));
    }

    public HttpServiceModelConfigurator head(String methodName, UnaryOperator<HttpServiceMethodModelConfigurator> configurator){
        return method(methodName, method -> configurator.apply(method.httpMethod(HttpMethodType.HEAD)));
    }

    public HttpServiceModelConfigurator websocket(String methodName, UnaryOperator<HttpServiceMethodModelConfigurator> configurator){
        return method(methodName, method -> configurator.apply(method.httpMethod(HttpMethodType.WEBSOCKET)));
    }

    public HttpServiceModelConfigurator file(String httpPath, String filePath, UnaryOperator<HttpServiceMethodModelConfigurator> configurator){
        return method(httpPath, method -> configurator.apply(method
                .httpMethod(HttpMethodType.FILE)
                .filePath(filePath)));
    }

    public HttpServiceModelConfigurator directory(String httpPath, String directory, UnaryOperator<HttpServiceMethodModelConfigurator> configurator){
        return method(
                httpPath.endsWith(SLASH) ?
                        httpPath :
                        httpPath + SLASH,
                method -> configurator.apply(method
                        .httpMethod(HttpMethodType.DIRECTORY)
                        .filePath(directory.endsWith(File.separator) ? directory : directory + File.separator)));
    }

    public HttpServiceModelConfigurator directory(String httpPath, String directory, String defaultFileName, UnaryOperator<HttpServiceMethodModelConfigurator> configurator){
        return method(
                httpPath.endsWith(SLASH) ?
                        httpPath :
                        httpPath + SLASH,
                method -> configurator.apply(method
                        .httpMethod(HttpMethodType.DIRECTORY)
                        .filePath(directory.endsWith(File.separator) ? directory : directory + File.separator)
                        .directoryDefaultFileName(defaultFileName)));
    }





    public HttpServiceModelConfigurator decorate(BiFunction<String, ServiceMethodSpecificationBuilder, ServiceMethodSpecificationBuilder> decorator) {
        BiFunction<String, ServiceMethodSpecificationBuilder, ServiceMethodSpecificationBuilder> current = this.decorator;
        this.decorator = (method, builder) -> {
            builder = current.apply(method, builder);
            return decorator.apply(method, builder);
        };
        return this;
    }

    public HttpServiceModelConfigurator logging(boolean isLogging) {
        this.logging = isLogging;
        return this;
    }

    public HttpServiceModelConfigurator defaultDataFormat(DataFormat format) {
        defaultDataFormat = format;
        return this;
    }

    protected String getId(){
        return serviceClass.getSimpleName();
    }

    protected HttpServiceModel configure(String path){
        return HttpServiceModel.builder()
                .id(getId())
                .path(path)
                .logging(logging)
                .serviceClass(serviceClass)
                .methods(methods
                        .entrySet()
                        .stream()
                        .collect(immutableMapCollector(entry -> entry.getValue().getId(), entry -> entry.getValue().configure())))
                .decorator(decorator)
                .defaultDataFormat(defaultDataFormat)
                .build();
    }
}
