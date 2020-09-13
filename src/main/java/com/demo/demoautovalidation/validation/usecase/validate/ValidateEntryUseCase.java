package com.demo.demoautovalidation.validation.usecase.validate;

import com.demo.demoautovalidation.common.OptionalConsumer;
import com.demo.demoautovalidation.validation.entity.ApiRequest;
import com.demo.demoautovalidation.validation.exception.ConcurrentRequestException;
import com.demo.demoautovalidation.validation.repository.ApiRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ValidateEntryUseCase implements ValidateEntryInputBoundary {

    private final ApiRequestRepository repository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void validate(ValidateEntryRequest request) {
        OptionalConsumer.of(repository.findById(request.getId()))
                .ifPresent(this::throwException)
                .ifNotPresent(() -> saveApiRequest(request));
    }

    private void throwException(ApiRequest apiRequest) {
        throw new ConcurrentRequestException(
                String.format("Request with ID [%s] already exist", apiRequest.getRequestId())
        );
    }

    private void saveApiRequest(ValidateEntryRequest request) {
        ApiRequest apiRequest = new ApiRequest();
        apiRequest.setRequestId(request.getId());
        apiRequest.setEndpoint(request.getEndPoint());
        apiRequest.setRequestData(request.getRequestData());
        apiRequest.setStatus(ApiRequest.ApiRequestStatus.IN_PROGRESS);
        repository.save(apiRequest);
    }
}
