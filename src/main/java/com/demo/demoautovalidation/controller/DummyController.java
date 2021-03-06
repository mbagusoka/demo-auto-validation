package com.demo.demoautovalidation.controller;

import com.demo.demoautovalidation.service.DummyService;
import com.demo.demoautovalidation.validation.annotation.ConcurrentValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.demo.demoautovalidation.service.impl.DefaultDummyService.DummyRequest;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class DummyController {

    private final DummyService dummyService;

    @GetMapping("/success")
    @ConcurrentValidation
    public void success(DummyRequest dummyRequest) {
        dummyService.doSuccess(dummyRequest);
    }

    @GetMapping("/error")
    @ConcurrentValidation
    public void error(DummyRequest dummyRequest) {
        dummyService.doError(dummyRequest);
    }
}
