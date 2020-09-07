package com.demo.demoautovalidation.validation.helper;

import com.demo.demoautovalidation.validation.enums.RequestMappingType;
import lombok.SneakyThrows;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.stream.Stream;

public class URLPathResolver {

    private URLPathResolver() {
    }

    public static String get(JoinPoint joinPoint) {
        Class<?> klass = joinPoint.getSignature().getDeclaringType();
        String classPath = getClassPath(klass);
        String methodPath = getMethodPath(joinPoint, klass);
        return classPath + methodPath;
    }

    private static String getClassPath(Class<?> klass) {
        RequestMapping classMapping = klass.getDeclaredAnnotation(RequestMapping.class);
        if (null != classMapping) {
            if (classMapping.value().length > 0) {
                return classMapping.value()[0];
            } else if (classMapping.path().length > 0) {
                return classMapping.path()[0];
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    @SneakyThrows(value = NoSuchMethodException.class)
    private static String getMethodPath(JoinPoint joinPoint, Class<?> klass) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        Class<?>[] parameterTypes = signature.getMethod().getParameterTypes();
        Annotation[] annotations = klass.getMethod(methodName, parameterTypes).getAnnotations();
        Annotation methodMapping = Arrays.stream(annotations)
                .filter(URLPathResolver::isRequestMapping)
                .findFirst()
                .orElse(null);
        if (null == methodMapping) {
            return "";
        } else {
            return RequestMappingType.getPath(methodMapping);
        }
    }

    private static boolean isRequestMapping(Annotation annotation) {
        return Stream.of(RequestMappingType.values())
                .map(RequestMappingType::getKlass)
                .anyMatch(klass -> klass.equals(annotation.annotationType()));
    }
}
