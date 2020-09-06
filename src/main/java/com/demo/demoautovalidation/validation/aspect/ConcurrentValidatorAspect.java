package com.demo.demoautovalidation.validation.aspect;

import com.demo.demoautovalidation.validation.annotation.ConcurrentValidation;
import com.demo.demoautovalidation.validation.annotation.ConcurrentValidationKey;
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

    @Before(value = "@annotation(concurrentValidation)")
    public void validateEntry(JoinPoint joinPoint, ConcurrentValidation concurrentValidation) {
        Object arg = joinPoint.getArgs()[0];
        String id = getRequestId(arg);
        String requestData = jsonify(arg);
        ValidateEntryRequest request = new ValidateEntryRequest(id, concurrentValidation.url(), requestData);
        validateEntryUseCase.validate(request);
    }

    @AfterReturning(value = "@annotation(com.demo.demoautovalidation.validation.annotation.ConcurrentValidation)")
    public void successEntry(JoinPoint joinPoint) {
        Object arg = joinPoint.getArgs()[0];
        String id = getRequestId(arg);
        UpdateEntryRequest request = new UpdateEntryRequest(id, ApiRequestStatus.SUCCESS);
        updateEntryUseCase.update(request);
    }

    @AfterThrowing(value = "@annotation(com.demo.demoautovalidation.validation.annotation.ConcurrentValidation)")
    public void failedEntry(JoinPoint joinPoint) {
        Object arg = joinPoint.getArgs()[0];
        String id = getRequestId(arg);
        UpdateEntryRequest request = new UpdateEntryRequest(id, ApiRequestStatus.FAILED);
        updateEntryUseCase.update(request);
    }

    private String getRequestId(Object arg) {
        String annotatedValues = Arrays.stream(arg.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ConcurrentValidationKey.class))
                .map(field -> stringify(arg, field))
                .collect(Collectors.joining());
        byte[] valueBytes = annotatedValues.getBytes();
        Checksum checksum = new CRC32();
        checksum.update(valueBytes, 0, valueBytes.length);
        return String.valueOf(checksum.getValue());
    }

    private String jsonify(Object arg) {
        try {
            return new ObjectMapper().writeValueAsString(arg);
        } catch (JsonProcessingException ignored) {
            return "";
        }
    }

    @SuppressWarnings("squid:S3011")
    private String stringify(Object arg, Field field) {
        try {
            field.setAccessible(true);
            return String.valueOf(field.get(arg));
        } catch (IllegalAccessException ignored) {
            return "";
        }
    }
}
