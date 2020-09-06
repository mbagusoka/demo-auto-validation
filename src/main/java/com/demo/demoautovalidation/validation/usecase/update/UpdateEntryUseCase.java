package com.demo.demoautovalidation.validation.usecase.update;

import com.demo.demoautovalidation.validation.entity.ApiRequest;
import com.demo.demoautovalidation.validation.repository.ApiRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateEntryUseCase implements UpdateEntryInputBoundary {

    private final ApiRequestRepository repository;

    @Override
    public void update(UpdateEntryRequest request) {
        repository.findById(request.getId())
                .ifPresent(apiRequest -> update(apiRequest, request));
    }

    private void update(ApiRequest apiRequest, UpdateEntryRequest request) {
        apiRequest.setStatus(request.getStatus());
        repository.save(apiRequest);
    }
}
