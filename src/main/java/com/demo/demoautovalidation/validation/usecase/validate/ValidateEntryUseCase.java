package com.demo.demoautovalidation.validation.usecase.validate;

import com.demo.demoautovalidation.common.OptionalConsumer;
import com.demo.demoautovalidation.validation.entity.ApiRequest;
import com.demo.demoautovalidation.validation.repository.ApiRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ValidateEntryUseCase implements ValidateEntryInputBoundary {

    private final ApiRequestRepository repository;

    @Override
    public void validate(ValidateEntryRequest request) {
        OptionalConsumer.of(repository.findById(request.getId()))
                .ifPresent(this::throwException)
                .ifNotPresent(() -> saveApiRequest(request));
    }

    private void throwException(ApiRequest apiRequest) {
        throw new IllegalArgumentException(
                String.format("Request with ID [%s] already exist", apiRequest.getRequestId())
        );
    }

    private void saveApiRequest(ValidateEntryRequest request) {
        ApiRequest apiRequest = new ApiRequest();
        apiRequest.setRequestId(request.getId());
        apiRequest.setEndpoint(request.getEndPoint());
        apiRequest.setInternalRecordId(UUID.randomUUID().toString());
        apiRequest.setRequestData(request.getRequestData());
        apiRequest.setStatus(ApiRequest.ApiRequestStatus.IN_PROGRESS);
        repository.save(apiRequest);
    }
}
