package com.demo.demoautovalidation.validation.aspect;

import com.demo.demoautovalidation.validation.annotation.ConcurrentValidationKey;
import com.demo.demoautovalidation.validation.helper.URLPathResolver;
import com.demo.demoautovalidation.validation.usecase.update.UpdateEntryInputBoundary;
import com.demo.demoautovalidation.validation.usecase.update.UpdateEntryRequest;
import com.demo.demoautovalidation.validation.usecase.validate.ValidateEntryInputBoundary;
import com.demo.demoautovalidation.validation.usecase.validate.ValidateEntryRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import static com.demo.demoautovalidation.validation.entity.ApiRequest.ApiRequestStatus;

@Component
@Aspect
@RequiredArgsConstructor
public class ConcurrentValidatorAspect {

    private final ValidateEntryInputBoundary validateEntryUseCase;
    private final UpdateEntryInputBoundary updateEntryUseCase;

    private final Map<String, Field[]> classFieldMap = new ConcurrentHashMap<>();

    @Before(value = "@annotation(com.demo.demoautovalidation.validation.annotation.ConcurrentValidation)")
    public void validateEntry(JoinPoint joinPoint) {
        Object arg = joinPoint.getArgs()[0];
        String id = getRequestId(arg);
        String requestData = jsonify(arg);
        String url = URLPathResolver.get(joinPoint);
        ValidateEntryRequest request = new ValidateEntryRequest(id, url, requestData);
        validateEntryUseCase.validate(request);
    }

    @AfterReturning(value = "@annotation(com.demo.demoautovalidation.validation.annotation.ConcurrentValidation)")
    public void successEntry(JoinPoint joinPoint) {
        Object arg = joinPoint.getArgs()[0];
        String id = getRequestId(arg);
        UpdateEntryRequest request = new UpdateEntryRequest(id, ApiRequestStatus.SUCCESS, null);
        updateEntryUseCase.update(request);
    }

    @AfterThrowing(
            value = "@annotation(com.demo.demoautovalidation.validation.annotation.ConcurrentValidation)",
            throwing = "e"
    )
    public void failedEntry(JoinPoint joinPoint, Throwable e) {
        Object arg = joinPoint.getArgs()[0];
        String id = getRequestId(arg);
        UpdateEntryRequest request = new UpdateEntryRequest(id, ApiRequestStatus.FAILED, e.getMessage());
        updateEntryUseCase.update(request);
    }

    private String getRequestId(Object arg) {
        Field[] fields = getAnnotatedFields(arg);
        String totalValues = getKey(arg, fields);
        byte[] valueBytes = totalValues.getBytes();
        Checksum checksum = new CRC32();
        checksum.update(valueBytes, 0, valueBytes.length);
        return String.valueOf(checksum.getValue());
    }

    private String getKey(Object arg, Field[] fields) {
        String key = Arrays.stream(fields)
                .map(field -> stringify(arg, field))
                .collect(Collectors.joining());
        if (key.isEmpty()) {
            throw new IllegalArgumentException("There is no Concurrent Key exist");
        }
        return key;
    }

    @SuppressWarnings({"squid:S3864", "squid:S3011"})
    private Field[] getAnnotatedFields(Object arg) {
        return classFieldMap.computeIfAbsent(
                arg.getClass().getSimpleName(),
                key -> Arrays.stream(arg.getClass().getDeclaredFields())
                        .filter(field -> field.isAnnotationPresent(ConcurrentValidationKey.class))
                        .peek(field -> field.setAccessible(true))
                        .toArray(Field[]::new));
    }

    private String jsonify(Object arg) {
        try {
            return new ObjectMapper().writeValueAsString(arg);
        } catch (JsonProcessingException ignored) {
            return "";
        }
    }

    private String stringify(Object arg, Field field) {
        try {
            return String.valueOf(field.get(arg));
        } catch (IllegalAccessException ignored) {
            return "";
        }
    }
}
