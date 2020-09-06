package com.demo.demoautovalidation.service.impl;

import com.demo.demoautovalidation.service.DummyService;
import com.demo.demoautovalidation.validation.annotation.ConcurrentValidationKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DefaultDummyService implements DummyService {

    @Override
    public void doSuccess(DummyRequest request) {
      log.info("SUCCESS");
    }

    @Override
    public void doError(DummyRequest request) {
        throw new RuntimeException("ERROR");
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DummyRequest {

        @ConcurrentValidationKey
        private String requestString;

        @ConcurrentValidationKey
        private int requestClient;
    }
}
