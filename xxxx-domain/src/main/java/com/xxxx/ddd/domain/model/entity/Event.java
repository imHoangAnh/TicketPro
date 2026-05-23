package com.xxxx.ddd.domain.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 190)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "venue", nullable = false, length = 190)
    private String venue;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static Event create(String title, String description, String venue, LocalDateTime startAt, LocalDateTime endAt, boolean active) {
        return new Event()
                .setTitle(title)
                .setDescription(description)
                .setVenue(venue)
                .setStartAt(startAt)
                .setEndAt(endAt)
                .setActive(active);
    }

    @PrePersist
    void onCreate() {
        validate();
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        validate();
        updatedAt = LocalDateTime.now();
    }

    public void validate() {
        if (isBlank(title)) {
            throw new IllegalArgumentException("event title is required");
        }
        if (isBlank(venue)) {
            throw new IllegalArgumentException("event venue is required");
        }
        if (startAt == null || endAt == null || !endAt.isAfter(startAt)) {
            throw new IllegalArgumentException("event endAt must be after startAt");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
