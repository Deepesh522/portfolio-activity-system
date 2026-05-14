package com.portfolio.notificationservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

//User-level notification preferences

@Entity
@Table(name = "notification_preferences", uniqueConstraints = {
        @UniqueConstraint(name = "uk_prefs_user_id", columnNames = "userId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    @Builder.Default
    private boolean emailEnabled = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean smsEnabled = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean webhookEnabled = false;

    // Comma-separated list of stock symbols the user wants alerts for
    @Column(length = 1000)
    private String watchedSymbols;

    // Threshold for high-value transaction alerts
    @Column(nullable = false)
    @Builder.Default
    private double highValueThreshold = 10000.0;
}
