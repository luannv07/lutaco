package vn.id.luannv.lutaco.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String username;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    String userAgent;

    @Column(name = "client_ip", length = 45)
    String clientIp;

    @Column(name = "request_uri")
    String requestUri;

    @Column(name = "method_name")
    String methodName;

    @Column(name = "execution_time_ms")
    Long executionTimeMs;

    @Column(name = "param_content")
    String paramContent;

    @Column(name = "created_date", nullable = false, updatable = false)
    @CreationTimestamp
    LocalDateTime createdDate;
}

