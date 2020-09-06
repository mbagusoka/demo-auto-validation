package com.demo.demoautovalidation.validation.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "api_requests")
@Getter
@Setter
public class ApiRequest implements Serializable {

    private static final long serialVersionUID = 9097835382687479092L;

    @Id
    @Column(name = "request_id")
    private String requestId;

    @Column(name = "endpoint")
    private String endpoint;

    @Column(name = "internal_record_id")
    private String internalRecordId;

    @Column(name = "request_data", columnDefinition = "TEXT")
    private String requestData;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ApiRequestStatus status;

    @Column
    @Version
    private Long version;

    @Column(name = "created_date")
    @CreatedDate
    private LocalDateTime createdDate;

    @Column(name = "created_by")
    @CreatedBy
    private String createdBy;

    @Column(name = "last_modified_date")
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    @Column(name = "last_modified_by")
    @LastModifiedBy
    private String lastModifiedBy;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiRequest that = (ApiRequest) o;
        return requestId.equals(that.requestId);
    }

    @Override
    public int hashCode() {
        return requestId.hashCode();
    }

    public enum ApiRequestStatus {

        IN_PROGRESS, SUCCESS, FAILED;
    }
}
