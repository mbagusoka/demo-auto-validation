package com.demo.demoautovalidation.service;

import static com.demo.demoautovalidation.service.impl.DefaultDummyService.DummyRequest;

public interface DummyService {

    void doSuccess(DummyRequest request);

    void doError(DummyRequest request);
}
