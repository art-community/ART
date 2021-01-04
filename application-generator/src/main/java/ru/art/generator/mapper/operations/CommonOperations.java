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

package ru.art.generator.mapper.operations;

import com.squareup.javapoet.ClassName;
import ru.art.generator.mapper.exception.InnerClassGenerationException;
import ru.art.generator.mapper.models.GenerationPackageModel;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import static java.io.File.separator;
import static java.text.MessageFormat.format;
import static ru.art.core.checker.CheckerForEmptiness.isEmpty;
import static ru.art.core.checker.CheckerForEmptiness.isNotEmpty;
import static ru.art.core.constants.StringConstants.*;
import static ru.art.generator.mapper.constants.Constants.MAPPER;
import static ru.art.generator.mapper.constants.Constants.PathAndPackageConstants.DOT_MAPPING_DOT;
import static ru.art.generator.mapper.constants.Constants.PathAndPackageConstants.DOT_MODEL_DOT;
import static ru.art.generator.mapper.constants.ExceptionConstants.MapperGeneratorExceptions.UNABLE_TO_CREATE_INNER_CLASS_MAPPER;
import static ru.art.generator.mapper.operations.AnalyzingOperations.isClassHasIgnoreGenerationAnnotation;
import static ru.art.generator.mapper.operations.GeneratorOperations.createMapperClass;
import static ru.art.generator.mapper.operations.GeneratorOperations.generatedFiles;

/**
 * Class containing common static methods which can be used in other operations
 */
public final class CommonOperations {
    private CommonOperations() {
    }

    /**
     * Wrap of System.out.println.
     *
     * @param message - text to print.
     */
    public static void printMessage(String message) {
        System.out.println(message);
    }

    /**
     * Wrap of System.err.println.
     *
     * @param errorText - error to print.
     */
    public static void printError(String errorText) {
        System.err.println(errorText);
    }

    /**
     * Wrap of System.err.println.
     *
     * @param errorCause - error cause.
     * @param errorMessage - error message.
     */
    public static void printError(String errorCause, String errorMessage) {
        if (isEmpty(errorCause) && isEmpty(errorMessage)) return;
        System.err.println(isEmpty(errorCause) && isNotEmpty(errorMessage)
                ? errorMessage
                : isEmpty(errorMessage)
                    ? errorCause
                    : errorCause + SPACE + errorMessage);
    }

    /**
     * Generate mapper for class, if it wasn't generated earlier.
     *
     * @param genClass      - class of mapper's model.
     * @param generationInfo - information about packages and path for generated class.
     * @return ClassName of new generated class.
     */
    public static ClassName createMapperForInnerClassIfNeeded(Class<?> genClass, GenerationPackageModel generationInfo) {
        if (genClass.isEnum()) return ClassName.get(genClass);
        // if inner class is in current class's package, we don't need to add package name to path, so it's empty
        String packageName = genClass.getPackage().getName().equals(generationInfo.getModelPackage())
                ? EMPTY_STRING
                : genClass.getPackage().getName().replace(genClass.getPackage().getName() + DOT, EMPTY_STRING);

        if (!isClassHasIgnoreGenerationAnnotation(genClass)) {
            if (!generatedFiles.contains(genClass.getName())) {
                String startPackage = isEmpty(packageName) ? generationInfo.getModelPackage() : packageName.substring(0, packageName.lastIndexOf(DOT));
                String genPackage = isEmpty(packageName) ? generationInfo.getGenPackage() : packageName.replaceFirst(DOT_MODEL_DOT, DOT_MAPPING_DOT);

                GenerationPackageModel generationInnerClassInfo = GenerationPackageModel.builder()
                        .startPackage(startPackage)
                        .startPackagePath(generationInfo.getPathToSrcMainJava() + startPackage.replace(DOT, separator))
                        .startPackagePathCompiled(generationInfo.getJarPathToMain() + startPackage.replace(DOT, separator))
                        .modelPackage(packageName)
                        .modelPackagePath(isEmpty(packageName)
                                ? generationInfo.getModelPackagePath()
                                : generationInfo.getPathToSrcMainJava() + packageName.replace(DOT, separator))
                        .modelPackagePathCompiled(isEmpty(packageName)
                                ? generationInfo.getModelPackagePathCompiled()
                                : generationInfo.getJarPathToMain() + packageName.replace(DOT, separator))
                        .genPackage(isEmpty(packageName) ? generationInfo.getGenPackage() : genPackage)
                        .genPackagePath(isEmpty(packageName)
                                ? generationInfo.getGenPackagePath()
                                : generationInfo.getPathToSrcMainJava() + genPackage.replace(DOT, separator))
                        .genPackagePathCompiled(isEmpty(packageName)
                                ? generationInfo.getGenPackagePathCompiled()
                                : generationInfo.getJarPathToMain() + genPackage.replace(DOT, separator))
                        .jarPathToMain(generationInfo.getJarPathToMain())
                        .pathToSrcMainJava(generationInfo.getPathToSrcMainJava())
                        .build();
                createMapperClass(genClass, generationInnerClassInfo);
                return ClassName.get(generationInnerClassInfo.getGenPackage(), genClass.getSimpleName() + MAPPER);
            }
            String newPackageName = isEmpty(packageName) ? genClass.getPackage().getName().replaceFirst(DOT_MODEL_DOT, DOT_MAPPING_DOT) : packageName.replaceFirst(DOT_MODEL_DOT, DOT_MAPPING_DOT);
            return ClassName.get(newPackageName, genClass.getSimpleName() + MAPPER);
        }
        throw new InnerClassGenerationException(format(UNABLE_TO_CREATE_INNER_CLASS_MAPPER, genClass));
    }

    /**
     * Gets class by field.
     * @param field - field which type is a model for new mapper.
     * @return class from field
     */
    public static Class<?> getClassFromField (Field field) {
        ParameterizedType type = (ParameterizedType) field.getGenericType();
        return (Class) type.getActualTypeArguments()[0];
    }
}
