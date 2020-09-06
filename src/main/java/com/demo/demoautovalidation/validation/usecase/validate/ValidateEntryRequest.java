package com.demo.demoautovalidation.validation.usecase.validate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ValidateEntryRequest {

    private final String id;
    private final String endPoint;
    private final String requestData;
}
