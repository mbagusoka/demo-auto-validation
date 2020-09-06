package com.demo.demoautovalidation.validation.repository;

import com.demo.demoautovalidation.validation.entity.ApiRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiRequestRepository extends JpaRepository<ApiRequest, String> {
}
