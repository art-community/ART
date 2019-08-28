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

package ru.art.generator.spec.common.operations;

import com.squareup.javapoet.*;
import ru.art.generator.spec.common.constants.*;
import ru.art.generator.spec.common.exception.*;
import ru.art.generator.spec.http.proxyspec.model.*;
import ru.art.generator.spec.http.proxyspec.operations.*;
import ru.art.generator.spec.http.servicespec.model.*;
import ru.art.generator.spec.http.servicespec.operations.*;
import ru.art.service.*;
import ru.art.service.exception.*;
import static com.squareup.javapoet.MethodSpec.*;
import static com.squareup.javapoet.TypeVariableName.*;
import static java.text.MessageFormat.*;
import static javax.lang.model.element.Modifier.*;
import static ru.art.core.checker.CheckerForEmptiness.*;
import static ru.art.generator.common.operations.CommonOperations.*;
import static ru.art.generator.spec.common.constants.CommonSpecGeneratorConstants.ExecuteMethodConstants.*;
import static ru.art.generator.spec.common.constants.SpecExceptionConstants.DefinitionExceptions.*;
import static ru.art.generator.spec.http.common.operations.HttpAnnotationsChecker.*;
import java.lang.reflect.*;

/**
 * Interface for executeMethod's generation for each type of specification.
 */
public interface ExecuteMethodGenerator {

    /**
     * Method generates "executeMethod" for specification.
     *
     * @param service  - class of service.
     * @param specType - generating type of specification.
     * @return MethodSpec representing signature and logic of "executeMethod" method.
     * @throws ExecuteMethodGenerationException when it'a unable to find "executeMethod" in implemented class
     */
    static MethodSpec generateExecuteMethod(Class<?> service, SpecificationType specType) throws ExecuteMethodGenerationException {
        Method method;
        try {
            method = Specification.class.getMethod(EXECUTE_METHOD, String.class, Object.class);
        } catch (NoSuchMethodException e) {
            throw new ExecuteMethodGenerationException(format(UNABLE_TO_DEFINE_METHOD, EXECUTE_METHOD, Specification.class.getSimpleName()));
        }
        if (isEmpty(method))
            throw new ExecuteMethodGenerationException(format(UNABLE_TO_DEFINE_METHOD, EXECUTE_METHOD, Specification.class.getSimpleName()));
        MethodSpec.Builder methodBuilder = methodBuilder(EXECUTE_METHOD)
                .returns(method.getGenericReturnType())
                .addTypeVariable(get(TYPE_P))
                .addTypeVariable(get(TYPE_R))
                .addAnnotation(Override.class)
                .addParameter(ParameterSpec.builder(String.class, METHOD_ID).build())
                .addParameter(ParameterSpec.builder(method.getParameters()[1].getParameterizedType(), REQ).build())
                .addModifiers(PUBLIC)
                .beginControlFlow(SWITCH_BY_METHOD_ID);
        switch (specType) {
            case httpServiceSpec:
                createHttpServiceExecuteMethod(service, methodBuilder);
                break;
            case httpProxySpec:
                createHttpProxyExecuteMethod(service, methodBuilder);
                break;
            case soapServiceSpec:
                //TODO
                break;
            case soapProxySpec:
                //TODO
                break;
            case grpcServiceSpec:
                //TODO
                break;
            case grpcProxySpec:
                //TODO
                break;
            default:
                printError(format(UNABLE_TO_DEFINE_SPECIFICATION_TYPE, specType));
        }


        methodBuilder.addStatement(DEFAULT_IN_EXEC_METHOD, UnknownServiceMethodException.class);
        methodBuilder.endControlFlow();

        return methodBuilder.build();
    }

    /**
     * Method generates body of executeMethod for http service specification.
     *
     * @param service       - class of service.
     * @param methodBuilder - builder for executeMethod method.
     */
    static void createHttpServiceExecuteMethod(Class<?> service, MethodSpec.Builder methodBuilder) {
        if (HttpServiceSpecificationClassGenerator.methodIds.size() > 0) {
            int methodIdIndex = 0;
            for (Method currentMethod : service.getDeclaredMethods()) {
                HttpServiceMethodsAnnotations hasAnnotations = HttpServiceSpecificationClassGenerator.methodAnnotations.get(currentMethod.getName());

                if (isEmpty(hasAnnotations) || amountOfHttpMethodsAnnotations(hasAnnotations) == 0
                        || serviceMethodHasSeveralHttpMethodsAnnotations(hasAnnotations))
                    continue;
                String methodId = HttpServiceSpecificationClassGenerator.methodIds.get(methodIdIndex);
                try {
                    methodBuilder = HttpServiceAuxiliaryOperations.addMethodStatementForExecute(currentMethod,
                            methodBuilder,
                            hasAnnotations,
                            methodId);
                } catch (MethodConsumesWithoutParamsException e) {
                    printError(e.getMessage());
                }
                methodIdIndex++;
            }
        }
    }

    /**
     * Method generates body of executeMethod for http proxy specification.
     *
     * @param service       - class of service.
     * @param methodBuilder - builder for executeMethod method.
     */
    static void createHttpProxyExecuteMethod(Class<?> service, MethodSpec.Builder methodBuilder) {
        if (HttpProxySpecificationClassGenerator.methodIds.size() > 0) {
            int methodIdIndex = 0;
            for (Method currentMethod : service.getDeclaredMethods()) {
                HttpProxyMethodsAnnotations hasAnnotations = HttpProxySpecificationClassGenerator.methodAnnotations.get(currentMethod.getName());

                if (isEmpty(hasAnnotations) || amountOfHttpMethodsAnnotations(hasAnnotations) == 0
                        || serviceMethodHasSeveralHttpMethodsAnnotations(hasAnnotations))
                    continue;
                String methodId = HttpProxySpecificationClassGenerator.methodIds.get(methodIdIndex);
                try {
                    methodBuilder = HttpProxyAuxiliaryOperations.addMethodStatementForExecute(currentMethod, methodBuilder, hasAnnotations, methodId);
                } catch (MethodConsumesWithoutParamsException e) {
                    printError(e.getMessage());
                }
                methodIdIndex++;
            }
        }
    }
}
