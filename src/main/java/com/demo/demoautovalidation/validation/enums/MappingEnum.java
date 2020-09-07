package com.demo.demoautovalidation.validation.enums;

import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.Annotation;
import java.util.stream.Stream;

@Getter
public enum MappingEnum {

    DEFAULT(RequestMapping.class) {
        @Override
        String get(Annotation annotation) {
            RequestMapping mapping = (RequestMapping) annotation;
            if (mapping.value().length > 0) {
                return mapping.value()[0];
            } else if (mapping.path().length > 0) {
                return mapping.path()[0];
            }
            return "";
        }
    },
    POST(PostMapping.class) {
        @Override
        String get(Annotation annotation) {
            PostMapping mapping = (PostMapping) annotation;
            if (mapping.value().length > 0) {
                return mapping.value()[0];
            } else if (mapping.path().length > 0) {
                return mapping.path()[0];
            }
            return "";
        }
    },
    GET(GetMapping.class) {
        @Override
        String get(Annotation annotation) {
            GetMapping mapping = (GetMapping) annotation;
            if (mapping.value().length > 0) {
                return mapping.value()[0];
            } else if (mapping.path().length > 0) {
                return mapping.path()[0];
            }
            return "";
        }
    };

    private final Class<?> klass;

    MappingEnum(Class<?> klass) {
        this.klass = klass;
    }

    abstract String get(Annotation annotation);

    public static String getPath(Annotation annotation) {
        return Stream.of(MappingEnum.values())
                .filter(mapping -> mapping.getKlass().equals(annotation.annotationType()))
                .findFirst()
                .map(mapping -> mapping.get(annotation))
                .orElseThrow(() -> new IllegalArgumentException("Mapping not available"));
    }
}
