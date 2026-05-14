package com.portfolio.notificationservice.repository;

import com.portfolio.notificationservice.entity.FailedNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FailedNotificationRepository extends JpaRepository<FailedNotification, UUID> {
}
