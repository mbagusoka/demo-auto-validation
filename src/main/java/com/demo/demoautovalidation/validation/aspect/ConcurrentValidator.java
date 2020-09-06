package com.demo.demoautovalidation.validation.aspect;

import com.demo.demoautovalidation.common.OptionalConsumer;
import com.demo.demoautovalidation.validation.annotation.ConcurrentValidation;
import com.demo.demoautovalidation.validation.annotation.ConcurrentValidationKey;
import com.demo.demoautovalidation.validation.entity.ApiRequest;
import com.demo.demoautovalidation.validation.repository.ApiRequestRepository;
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
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import static com.demo.demoautovalidation.validation.entity.ApiRequest.ApiRequestStatus;

@Component
@Aspect
@RequiredArgsConstructor
public class ConcurrentValidator {

    private final ApiRequestRepository apiRequestRepository;

    @Before(value = "@annotation(concurrentValidation)")
    public void validateEntry(JoinPoint joinPoint, ConcurrentValidation concurrentValidation) {
        Object arg = joinPoint.getArgs()[0];
        String id = getRequestId(arg);
        OptionalConsumer.of(apiRequestRepository.findById(id))
                .ifPresent(this::throwException)
                .ifNotPresent(() -> saveApiRequest(concurrentValidation, arg, id));
    }

    @AfterReturning(value = "@annotation(concurrentValidation)")
    public void successEntry(JoinPoint joinPoint, ConcurrentValidation concurrentValidation) {
        Object arg = joinPoint.getArgs()[0];
        String id = getRequestId(arg);
        apiRequestRepository.findById(id)
                .ifPresent(this::updateSuccess);
    }

    @AfterThrowing(value = "@annotation(concurrentValidation)")
    public void failedEntry(JoinPoint joinPoint, ConcurrentValidation concurrentValidation) {
        Object arg = joinPoint.getArgs()[0];
        String id = getRequestId(arg);
        apiRequestRepository.findById(id)
                .ifPresent(this::updateError);
    }

    private void updateSuccess(ApiRequest apiRequest) {
        apiRequest.setStatus(ApiRequestStatus.SUCCESS);
        apiRequestRepository.save(apiRequest);
    }

    private void updateError(ApiRequest apiRequest) {
        apiRequest.setStatus(ApiRequestStatus.FAILED);
        apiRequestRepository.save(apiRequest);
    }

    private void throwException(ApiRequest apiRequest) {
        throw new IllegalArgumentException(
                String.format("Request with ID [%s] already exist", apiRequest.getRequestId())
        );
    }

    private void saveApiRequest(ConcurrentValidation concurrentValidation, Object arg, String id) {
        ApiRequest apiRequest = new ApiRequest();
        apiRequest.setRequestId(id);
        apiRequest.setEndpoint(concurrentValidation.url());
        apiRequest.setInternalRecordId(UUID.randomUUID().toString());
        apiRequest.setRequestData(jsonify(arg));
        apiRequest.setStatus(ApiRequestStatus.IN_PROGRESS);
        apiRequestRepository.save(apiRequest);
    }

    private String jsonify(Object arg) {
        try {
            return new ObjectMapper().writeValueAsString(arg);
        } catch (JsonProcessingException ignored) {
            return "";
        }
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
