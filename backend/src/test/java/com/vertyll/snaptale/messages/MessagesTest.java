package com.vertyll.snaptale.messages;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import jakarta.validation.Constraint;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.ClassPathResource;

import com.vertyll.snaptale.common.MessageKeys;

import com.ibm.icu.text.MessagePattern;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class MessagesTest {

    @Test
    void everyMessageKeyHasPolishText() throws IOException, IllegalAccessException {
        Set<String> keys = new TreeSet<>();
        for (Field field : MessageKeys.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == String.class
                    && !"HTTP_STATUS_PREFIX".equals(field.getName())) {
                keys.add((String) field.get(null));
            }
        }

        assertThat(messages().keySet()).containsAll(keys);
    }

    @Test
    void everyConstraintMessageIsAMessageKey() throws IOException {
        Set<String> constraintMessages = new TreeSet<>();
        for (JavaClass javaClass : new ClassFileImporter().withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("com.vertyll.snaptale")) {
            if (isConfigurationProperties(javaClass)) {
                continue;
            }
            for (Field field : javaClass.reflect().getDeclaredFields()) {
                for (Annotation annotation : annotations(field).toList()) {
                    if (annotation.annotationType().isAnnotationPresent(Constraint.class)) {
                        constraintMessages.add((String) AnnotationUtils.getValue(annotation, "message"));
                    }
                }
            }
        }

        assertThat(constraintMessages).contains(MessageKeys.REQUIRED).isSubsetOf(messages().keySet());
    }

    @Test
    void everyTextIsValidIcu() throws IOException {
        messages()
            .forEach((key, text) -> assertThatCode(() -> new MessagePattern(text)).as(key).doesNotThrowAnyException());
    }

    private static boolean isConfigurationProperties(JavaClass javaClass) {
        return javaClass.isAnnotatedWith(ConfigurationProperties.class)
                || javaClass.getEnclosingClass().map(MessagesTest::isConfigurationProperties).orElse(false);
    }

    private static Stream<Annotation> annotations(Field field) {
        return Stream.concat(Arrays.stream(field.getAnnotations()), annotations(field.getAnnotatedType()));
    }

    private static Stream<Annotation> annotations(AnnotatedType type) {
        Stream<AnnotatedType> nested = type instanceof AnnotatedParameterizedType parameterized
                ? Arrays.stream(parameterized.getAnnotatedActualTypeArguments()) : Stream.empty();
        return Stream.concat(Arrays.stream(type.getAnnotations()), nested.flatMap(MessagesTest::annotations));
    }

    private static Map<String, String> messages() throws IOException {
        try (InputStream input = new ClassPathResource(MessagesController.RESOURCE).getInputStream()) {
            return JsonMapper.builder().build().readValue(input, new TypeReference<>() {
            });
        }
    }
}
