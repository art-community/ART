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

import com.squareup.javapoet.*;
import ru.art.core.checker.CheckerForEmptiness;
import ru.art.core.extension.StringExtensions;
import ru.art.entity.Entity;
import ru.art.entity.mapper.ValueFromModelMapper;
import ru.art.entity.mapper.ValueToModelMapper;
import ru.art.generator.mapper.annotation.GenerationException;
import ru.art.generator.mapper.exception.DefinitionException;
import ru.art.generator.mapper.exception.InnerClassGenerationException;
import ru.art.generator.mapper.exception.MappingGeneratorException;
import ru.art.generator.mapper.models.GenerationPackageModel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.squareup.javapoet.CodeBlock.join;
import static com.squareup.javapoet.CodeBlock.of;
import static com.squareup.javapoet.TypeSpec.interfaceBuilder;
import static java.io.File.separator;
import static java.text.MessageFormat.format;
import static java.util.Objects.nonNull;
import static javax.lang.model.element.Modifier.*;
import static ru.art.core.constants.StringConstants.*;
import static ru.art.core.factory.CollectionsFactory.dynamicArrayOf;
import static ru.art.generator.mapper.constants.Constants.*;
import static ru.art.generator.mapper.constants.Constants.PathAndPackageConstants.*;
import static ru.art.generator.mapper.constants.Constants.SupportedJavaClasses.*;
import static ru.art.generator.mapper.constants.Constants.SymbolsAndFormatting.STRING_PATTERN;
import static ru.art.generator.mapper.constants.ExceptionConstants.MapperGeneratorExceptions.*;
import static ru.art.generator.mapper.constants.FromModelConstants.*;
import static ru.art.generator.mapper.constants.ToModelConstants.*;
import static ru.art.generator.mapper.operations.AnalyzingOperations.isClassHasIgnoreGenerationAnnotation;
import static ru.art.generator.mapper.operations.CollectionGeneratorOperations.*;
import static ru.art.generator.mapper.operations.CommonOperations.*;

/**
 * Class containing static methods for creating mapper classes,
 * generation of ToModel and FromModel methods.
 */
public final class GeneratorOperations {
    private GeneratorOperations() {
    }

    static Set<String> generatedFiles = new HashSet<>();

    public static Set<String> getGeneratedFiles() {
        return generatedFiles;
    }

    /**
     * Generate block ValueToModelMapper and ValueFromModelMapper for class.
     *
     * @param clazz         - class of model for mappers generating.
     * @param generationInfo - information about packages and path for generated class.
     * @return TypeSpec containing ToModel and FromModel mappers.
     * @throws MappingGeneratorException is thrown when any other exception happens in attempt to create mapper.
     */
    public static TypeSpec generateMappers(Class<?> clazz, GenerationPackageModel generationInfo)
            throws MappingGeneratorException {
        try {
            return interfaceBuilder(clazz.getSimpleName() + MAPPER)
                    .addModifiers(PUBLIC, STATIC)
                    .addFields(generateConstantsBlock(clazz))
                    .addField(generateToModelBlock(clazz, generationInfo))
                    .addField(generateFromModelBlock(clazz, generationInfo))
                    .build();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new MappingGeneratorException(format(UNABLE_TO_GENERATE_INTERFACE, clazz.getSimpleName(), throwable.getClass().getSimpleName()), throwable);
        }
    }

    /**
     * Creating ordinary mapping class.
     *
     * @param clazz          - class of model for mappers generating.
     * @param generationInfo - information about packages and path for generated class.
     * @throws MappingGeneratorException is thrown when IOException or NullPointerException
     *                                   happens while writing to file.
     */
    public static void createMapperClass(Class<?> clazz, GenerationPackageModel generationInfo)
            throws MappingGeneratorException {
        if (generatedFiles.contains(clazz.getName()))
            return;
        printMessage(format(START_GENERATING, clazz.getSimpleName() + MAPPER));

        String genPackage = generationInfo.getGenPackage();
        String jarPathToMain = generationInfo.getJarPathToMain();
        createMapperClasses(clazz, generationInfo);

        TypeSpec mapperType = generateMappers(clazz, generationInfo);

        JavaFile javaFile = JavaFile.builder(genPackage, mapperType)
                .indent(TABULATION)
                .addStaticImport(CheckerForEmptiness.class, IS_NOT_EMPTY)
                .build();
        try {
            URL location = clazz.getProtectionDomain().getCodeSource().getLocation();
            StringBuilder classJarPath = new StringBuilder();
            if (nonNull(location) && (classJarPath.append(location.getPath()).toString().contains(DOT_JAR))) {
                String temp = jarPathToMain.substring(0, jarPathToMain.substring(0, jarPathToMain.lastIndexOf(BUILD)).lastIndexOf(separator));
                classJarPath.replace(0, classJarPath.length(), temp.substring(0, temp.lastIndexOf(separator)))
                        .append(separator)
                        .append(BUILD)
                        .append(separator);
            }
            if (classJarPath.length() != 0) {
                javaFile.writeTo(new File(classJarPath.subSequence(0, classJarPath.indexOf(BUILD)).toString() + SRC_MAIN_JAVA));
                generatedFiles.add(clazz.getName());
                printMessage(format(GENERATED_SUCCESSFULLY, clazz.getSimpleName() + MAPPER));
                return;
            }
            javaFile.writeTo(new File(jarPathToMain.subSequence(0, jarPathToMain.indexOf(BUILD)).toString() + SRC_MAIN_JAVA));
            generatedFiles.add(clazz.getName());
            printMessage(format(GENERATED_SUCCESSFULLY, clazz.getSimpleName() + MAPPER));
        } catch (StringIndexOutOfBoundsException throwable) {
            throw new MappingGeneratorException(format(UNABLE_TO_PARSE_JAR_PATH, clazz.getSimpleName()), throwable);
        } catch (IOException ioException) {
            throw new MappingGeneratorException(format(UNABLE_TO_CREATE_MAPPER, clazz.getSimpleName() + MAPPER), ioException);
        } catch (NullPointerException throwable) {
            throw new MappingGeneratorException(format(UNABLE_TO_FIND_A_PATH_FOR_CLASS, clazz.getSimpleName()), throwable);
        } catch (Throwable throwable) {
            throw new MappingGeneratorException(format(UNABLE_TO_CREATE_MAPPER_UNKNOWN_ERROR, clazz.getSimpleName()), throwable);
        }
    }

    /**
     * Creating unit mapping class for both request and response model.
     *
     * @param request        - class of request model.
     * @param response       - class of response model.
     * @param generationInfo - information about packages and path for generated class.
     * @throws MappingGeneratorException is thrown when IOException or NullPointerException
     *                                   happens while writing to file.
     */
    public static void createRequestResponseMapperClass(Class<?> request, Class<?> response, GenerationPackageModel generationInfo)
            throws MappingGeneratorException {
        if (generatedFiles.contains(request.getName()) && generatedFiles.contains(response.getName()))
            printMessage(format(START_GENERATING, request.getSimpleName().replace(REQUEST, EMPTY_STRING) + REQUEST + RESPONSE + MAPPER));
        String jarPathToMain = generationInfo.getJarPathToMain();

        if (!generatedFiles.contains(request.getName()))
            createMapperClasses(request, generationInfo);
        if (!generatedFiles.contains(response.getName()))
            createMapperClasses(response, generationInfo);

        String newClassName = request.getSimpleName().replace(REQUEST, EMPTY_STRING) + REQUEST + RESPONSE + MAPPER;
        TypeSpec mapper = interfaceBuilder(newClassName)
                .addModifiers(PUBLIC, STATIC)
                .build();
        TypeSpec requestMapperType = generateMappers(request, generationInfo);
        TypeSpec responseMapperType = generateMappers(response, generationInfo);

        mapper = mapper.toBuilder().addType(requestMapperType).addType(responseMapperType).build();
        JavaFile javaFile = JavaFile.builder(generationInfo.getGenPackage(), mapper)
                .indent(TABULATION)
                .addStaticImport(CheckerForEmptiness.class, IS_NOT_EMPTY)
                .build();
        try {
            StringBuilder requestJarPath = new StringBuilder(request.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath());

            if (requestJarPath.toString().contains(DOT_JAR)) {
                String temp = jarPathToMain.substring(0, jarPathToMain.substring(0, jarPathToMain.lastIndexOf(BUILD)).lastIndexOf(separator));
                requestJarPath.replace(0, requestJarPath.length(), temp.substring(0, temp.lastIndexOf(separator)))
                        .append(separator)
                        .append(BUILD)
                        .append(separator);
            }

            javaFile.writeTo(new File(requestJarPath.subSequence(0, requestJarPath.indexOf(BUILD)).toString() + SRC_MAIN_JAVA));
            generatedFiles.add(request.getName());
            generatedFiles.add(response.getName());
            printMessage(format(GENERATED_SUCCESSFULLY, request.getSimpleName().replace(REQUEST, EMPTY_STRING)
                    + REQUEST
                    + RESPONSE
                    + MAPPER));
        } catch (StringIndexOutOfBoundsException throwable) {
            throw new MappingGeneratorException(format(UNABLE_TO_PARSE_JAR_PATH, request.getSimpleName()), throwable);
        } catch (IOException ioException) {
            throw new MappingGeneratorException(format(UNABLE_TO_CREATE_REQ_RES_MAPPER, newClassName), ioException);
        } catch (NullPointerException throwable) {
            throw new MappingGeneratorException(format(UNABLE_TO_FIND_A_PATH_FOR_CLASS, request.getSimpleName()), throwable);
        } catch (Throwable throwable) {
            throw new MappingGeneratorException(format(UNABLE_TO_CREATE_MAPPER_UNKNOWN_ERROR, request.getSimpleName()), throwable);
        }
    }

    /**
     *
     * @param request        - class of request model.
     * @param generationInfo - information about packages and path for generated class.
     */
    public static void createMapperClasses(Class<?> request, GenerationPackageModel generationInfo) {
        for (int i = 0; i < request.getClasses().length; i++) {
            if (!request.getClasses()[i].getSimpleName().equals(request.getSimpleName() + BUILDER) &&
                    !generatedFiles.contains(request.getName()) &&
                    !isClassHasIgnoreGenerationAnnotation(request.getClasses()[i]) &&
                    !request.isEnum()) {
                createMapperClass(request.getClasses()[i], generationInfo);
            }
        }
    }

    /**
     * Generate builder for ToModel block of ValueToModelMapper.
     * Example:
     * ValueToModelMapper<Model, Entity> toModel = entity -> Model.builder()
     * .build();
     *
     * @param clazz          - class of model for mappers generating.
     * @param generationInfo - information about packages and path for generated class.
     * @return FieldSpec containing ToModel block.
     */
    public static FieldSpec generateToModelBlock(Class<?> clazz, GenerationPackageModel generationInfo) {
        List<CodeBlock> codeBlocks = dynamicArrayOf(of(ENTITY_TO_MODEL_LAMBDA, clazz));
        List<String> notGeneratedFields = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            switch (field.getType().getName()) {
                case CLASS_STRING:
                    codeBlocks.add(getBuilderLineForField(GET_ENTITY_STRING, field.getName(), field.getName()));
                    break;
                case CLASS_INTEGER:
                case CLASS_INTEGER_UNBOX:
                    codeBlocks.add(getBuilderLineForField(GET_ENTITY_INT, field.getName(), field.getName()));
                    break;
                case CLASS_DOUBLE:
                case CLASS_DOUBLE_UNBOX:
                    codeBlocks.add(getBuilderLineForField(GET_ENTITY_DOUBLE, field.getName(), field.getName()));
                    break;
                case CLASS_LONG:
                case CLASS_LONG_UNBOX:
                    codeBlocks.add(getBuilderLineForField(GET_ENTITY_LONG, field.getName(), field.getName()));
                    break;
                case CLASS_BYTE:
                case CLASS_BYTE_UNBOX:
                    codeBlocks.add(getBuilderLineForField(GET_ENTITY_BYTE, field.getName(), field.getName()));
                    break;
                case CLASS_BOOLEAN:
                case CLASS_BOOLEAN_UNBOX:
                    codeBlocks.add(getBuilderLineForField(GET_ENTITY_BOOL, field.getName(), field.getName()));
                    break;
                case CLASS_FLOAT:
                case CLASS_FLOAT_UNBOX:
                    codeBlocks.add(getBuilderLineForField(GET_ENTITY_FLOAT, field.getName(), field.getName()));
                    break;
                case CLASS_DATE:
                    codeBlocks.add(getBuilderLineForField(GET_ENTITY_DATE, field.getName(), field.getName()));
                    break;
                case CLASS_ENTITY:
                    codeBlocks.add(getBuilderLineForField(GET_ENTITY, field.getName(), field.getName()));
                    break;
                case CLASS_LIST:
                    try {
                        codeBlocks.add(generateToModelForList(field, generationInfo));
                    } catch (DefinitionException | InnerClassGenerationException throwable) {
                        notGeneratedFields.add(field.getName());
                        printError(String.valueOf(throwable.getCause()), throwable.getMessage());
                    }
                    break;
                case CLASS_SET:
                    try {
                        codeBlocks.add(generateToModelForSet(field, generationInfo));
                    } catch (DefinitionException | InnerClassGenerationException throwable) {
                        notGeneratedFields.add(field.getName());
                        printError(String.valueOf(throwable.getCause()), throwable.getMessage());
                    }
                    break;
                case CLASS_MAP:
                    try {
                        codeBlocks.add(generateToModelForMap(field, generationInfo));
                    } catch (DefinitionException | InnerClassGenerationException throwable) {
                        notGeneratedFields.add(field.getName());
                        printError(String.valueOf(throwable.getCause()), throwable.getMessage());
                    }
                    break;
                case CLASS_QUEUE:
                    try {
                        codeBlocks.add(generateToModelForQueue(field, generationInfo));
                    } catch (DefinitionException | InnerClassGenerationException throwable) {
                        notGeneratedFields.add(field.getName());
                        printError(String.valueOf(throwable.getCause()), throwable.getMessage());
                    }
                    break;
                default:
                    if (field.getType().getName().equals(clazz.getName())) {
                        notGeneratedFields.add(field.getName());
                    } else if (clazz.isEnum())
                        try {
                            codeBlocks.add(of(DOUBLE_TABULATION + GET_ENUM_VALUE, field.getName(), clazz, field.getName()));
                        } catch (DefinitionException | InnerClassGenerationException throwable) {
                            notGeneratedFields.add(field.getName());
                            printError(String.valueOf(throwable.getCause()), throwable.getMessage());
                        }
                    else
                        try {
                            codeBlocks.add(generateFromEntityFieldMapperCodeBlock(field.getType(), field.getName(), generationInfo));
                        } catch (DefinitionException | InnerClassGenerationException throwable) {
                            notGeneratedFields.add(field.getName());
                            printError(String.valueOf(throwable.getCause()), throwable.getMessage());
                        }

            }
        }
        codeBlocks.add(of(DOUBLE_TABULATION + DEFAULT_ENTITY_BUILDER));
        if (notGeneratedFields.isEmpty())
            return FieldSpec.builder(ParameterizedTypeName.get(ValueToModelMapper.class, clazz, Entity.class), TO_MODEL + clazz.getSimpleName(), PUBLIC, STATIC, FINAL)
                    .initializer(join(codeBlocks, NEW_LINE))
                    .build();
        else
            return FieldSpec.builder(ParameterizedTypeName.get(ValueToModelMapper.class, clazz, Entity.class), TO_MODEL + clazz.getSimpleName(), PUBLIC, STATIC, FINAL)
                    .initializer(join(codeBlocks, NEW_LINE))
                    .addAnnotation(AnnotationSpec.builder(GenerationException.class).addMember(NOT_GENERATED_FIELDS, STRING_PATTERN, notGeneratedFields).build())
                    .build();
    }

    /**
     * Generate builder for FromModel block of ValueFromModelMapper.
     * Example:
     * ValueFromModelMapper<Model, Entity> fromModel = model -> Entity.entityBuilder()
     * .build();
     *
     * @param clazz         - class of model for mappers generating.
     * @param generationInfo - information about packages and path for generated class.
     * @return FieldSpec containing FromModel block.
     */
    public static FieldSpec generateFromModelBlock(Class<?> clazz, GenerationPackageModel generationInfo) {
        List<CodeBlock> codeBlocks = dynamicArrayOf(of(MODEL_TO_ENTITY_LAMBDA, Entity.class));
        List<String> notGeneratedFields = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            String getField;
            if (field.getType().getName().equals(CLASS_BOOLEAN_UNBOX))
                getField = IS + String.valueOf(field.getName().charAt(0)).toUpperCase() + field.getName().substring(1);
            else
                getField = GET + String.valueOf(field.getName().charAt(0)).toUpperCase() + field.getName().substring(1);

            switch (field.getType().getName()) {
                case CLASS_STRING:
                    codeBlocks.add(getBuilderLineForField(STRING_FIELD, field.getName(), getField));
                    break;
                case CLASS_INTEGER:
                case CLASS_INTEGER_UNBOX:
                    codeBlocks.add(getBuilderLineForField(INT_FIELD, field.getName(), getField));
                    break;
                case CLASS_DOUBLE:
                case CLASS_DOUBLE_UNBOX:
                    codeBlocks.add(getBuilderLineForField(DOUBLE_FIELD, field.getName(), getField));
                    break;
                case CLASS_LONG:
                case CLASS_LONG_UNBOX:
                    codeBlocks.add(getBuilderLineForField(LONG_FIELD, field.getName(), getField));
                    break;
                case CLASS_BYTE:
                case CLASS_BYTE_UNBOX:
                    codeBlocks.add(getBuilderLineForField(BYTE_FIELD, field.getName(), getField));
                    break;
                case CLASS_BOOLEAN:
                case CLASS_BOOLEAN_UNBOX:
                    codeBlocks.add(getBuilderLineForField(BOOL_FIELD, field.getName(), getField));
                    break;
                case CLASS_FLOAT:
                case CLASS_FLOAT_UNBOX:
                    codeBlocks.add(getBuilderLineForField(FLOAT_FIELD, field.getName(), getField));
                    break;
                case CLASS_DATE:
                    codeBlocks.add(getBuilderLineForField(DATE_FIELD, field.getName(), getField));
                    break;
                case CLASS_ENTITY:
                    codeBlocks.add(of(DOUBLE_TABULATION + ENTITY_FIELD_FROM_ENTITY, field.getName(), getField));
                    break;
                case CLASS_LIST:
                case CLASS_SET:
                case CLASS_QUEUE:
                    try {
                        codeBlocks.add(generateFromModelForCollection(field, generationInfo));
                    } catch (DefinitionException | InnerClassGenerationException throwable) {
                        notGeneratedFields.add(field.getName());
                        printError(String.valueOf(throwable.getCause()), throwable.getMessage());
                    }
                    break;
                case CLASS_MAP:
                    try {
                        codeBlocks.add(generateFromModelForMap(field, generationInfo));
                    } catch (DefinitionException | InnerClassGenerationException throwable) {
                        notGeneratedFields.add(field.getName());
                        printError(String.valueOf(throwable.getCause()), throwable.getMessage());
                    }
                    break;
                default:
                    if (field.getType().getName().equals(clazz.getName())) {
                        notGeneratedFields.add(field.getName());
                    } else if (clazz.isEnum())
                        try {
                            codeBlocks.add(of(DOUBLE_TABULATION + ENUM_FILED, field.getName(), StringExtensions.class, EMPTY_IF_NULL, getField));
                        } catch (DefinitionException | InnerClassGenerationException throwable) {
                            notGeneratedFields.add(field.getName());
                            printError(String.valueOf(throwable.getCause()), throwable.getMessage());
                        }
                    else
                        try {
                            codeBlocks.add(generateToEntityFieldMapperCodeBlock(field.getType(), field.getName(), getField, generationInfo));
                        } catch (DefinitionException | InnerClassGenerationException throwable) {
                            notGeneratedFields.add(field.getName());
                            printError(String.valueOf(throwable.getCause()), throwable.getMessage());
                        }
            }
        }

        codeBlocks.add(of(DOUBLE_TABULATION + DEFAULT_MODEL_BUILDER));
        if (notGeneratedFields.isEmpty())
            return FieldSpec.builder(ParameterizedTypeName.get(ValueFromModelMapper.class, clazz, Entity.class), FROM_MODEL + clazz.getSimpleName(), PUBLIC, STATIC, FINAL)
                    .initializer(join(codeBlocks, NEW_LINE))
                    .build();
        else
            return FieldSpec.builder(ParameterizedTypeName.get(ValueFromModelMapper.class, clazz, Entity.class), FROM_MODEL + clazz.getSimpleName(), PUBLIC, STATIC, FINAL)
                    .initializer(join(codeBlocks, NEW_LINE))
                    .addAnnotation(AnnotationSpec.builder(GenerationException.class).addMember(NOT_GENERATED_FIELDS, STRING_PATTERN, notGeneratedFields).build())
                    .build();
    }

    /**
     * Create new ordinary mapper class for processing field and
     * generate codeblock for "from entity to model" string in ToModel builder.
     *
     * @param clazz         - processing field type.
     * @param fieldName     - name of processing field.
     * @param generationInfo - information about packages and path for generated class.
     * @return CodeBlock for entity of ToModel mapper.
     * @throws MappingGeneratorException can be thrown by createMapperClass.
     */
    public static CodeBlock generateFromEntityFieldMapperCodeBlock(Class<?> clazz, String fieldName, GenerationPackageModel generationInfo)
            throws InnerClassGenerationException {
        ClassName className = createMapperForInnerClassIfNeeded(clazz, generationInfo);
        if (clazz.isEnum())
            return CodeBlock.builder()
                    .add(of(DOUBLE_TABULATION + GET_ENUM_VALUE, fieldName, clazz, fieldName))
                    .build();
        return CodeBlock.builder()
                .add(of(DOUBLE_TABULATION + GET_VALUE, fieldName, fieldName, className, TO_MODEL + clazz.getSimpleName()))
                .build();
    }

    /**
     * Create new ordinary mapper class for processing field and
     * generate codeblock for "to entity from model" string in FromModel builder.
     *
     * @param clazz         - processing field type.
     * @param fieldName     - name of processing field.
     * @param getField      - string representing "get" + name of processing field.
     * @param generationInfo - information about packages and path for generated class.
     * @return CodeBlock for entity of FromModel mapper.
     * @throws MappingGeneratorException can be thrown by createMapperClass.
     */
    public static CodeBlock generateToEntityFieldMapperCodeBlock(Class<?> clazz, String fieldName, String getField, GenerationPackageModel generationInfo)
            throws InnerClassGenerationException {
        ClassName className = createMapperForInnerClassIfNeeded(clazz, generationInfo);
        if (clazz.isEnum())
            return CodeBlock.builder()
                    .add(of(DOUBLE_TABULATION + ENUM_FILED, fieldName, StringExtensions.class, EMPTY_IF_NULL, getField))
                    .build();
        return CodeBlock.builder()
                .add(of(DOUBLE_TABULATION + ENTITY_FIELD, fieldName, getField, className, FROM_MODEL + clazz.getSimpleName()))
                .build();
    }

    /**
     * Returning codeblock of generating string
     *
     * @param pattern        - pattern of string in builder.
     *                       Example: ".stringField($S, model.$L())"
     * @param fieldName      - name of processing field.
     * @param fieldNameToGet - either name of processing field or "get" + name of processing field.
     * @return CodeBlock of generating string.
     */
    public static CodeBlock getBuilderLineForField(String pattern, String fieldName, String fieldNameToGet) {
        return of(DOUBLE_TABULATION + pattern, fieldName, fieldNameToGet);
    }

    /**
     * Method generating block of fields' names constants for each mapper.
     *
     * @param clazz - class of model for mappers generating.
     * @return fields' names as string constants.
     */
    public static Iterable<FieldSpec> generateConstantsBlock(Class<?> clazz) {
        return Stream.of(clazz.getDeclaredFields())
                .map(declaredFields -> FieldSpec.builder(String.class, declaredFields.getName())
                        .addModifiers(PUBLIC, STATIC, FINAL)
                        .initializer(STRING_PATTERN, declaredFields.getName())
                        .build())
                .collect(Collectors.toList());
    }
}
