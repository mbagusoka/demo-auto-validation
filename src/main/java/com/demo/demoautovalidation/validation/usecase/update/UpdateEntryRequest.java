package com.demo.demoautovalidation.validation.usecase.update;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.demo.demoautovalidation.validation.entity.ApiRequest.ApiRequestStatus;

@Getter
@RequiredArgsConstructor
public class UpdateEntryRequest {

    private final String id;
    private final ApiRequestStatus status;
}
