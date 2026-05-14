package com.portfolio.notificationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

//Records permanently failed notification events that exhausted all retry attempts.

@Entity
@Table(name = "failed_notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FailedNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String eventId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, length = 2000)
    private String payload;

    @Column(nullable = false, length = 1000)
    private String failureReason;

    @Column(nullable = false)
    private int retryCount;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant failedAt;
}
