package ru.yandex.practicum.core.event.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.core.interaction.event.enums.EventPublishState;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "annotation", nullable = false)
    @Size(min = 20, max = 2000)
    String annotation;

    @ToString.Exclude
    @Column(name = "category_id")
    Long categoryId;

    @Column(name = "description", nullable = false)
    @Size(min = 20, max = 7000)
    String description;

    @Column(name = "event_date", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;

    @Column(name = "location_lat", nullable = false)
    Float locationLat;
    @Column(name = "location_lon", nullable = false)
    Float locationLon;

    @Column(name = "paid", nullable = false)
    Boolean paid;

    @Column(name = "participant_limit", nullable = false)
    @Min(0)
    Integer participantLimit;

    @Column(name = "request_moderation", nullable = false)
    Boolean requestModeration;

    @Column(name = "title", nullable = false)
    @Size(min = 3, max = 120)
    String title;

    @Column(name = "created_on", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdOn;

    @ToString.Exclude
    @Column(name = "initiator_id")
    Long initiatorId;

    @Column(name = "published_on")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime publishedOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "state")
    @Builder.Default
    EventPublishState eventPublishState = EventPublishState.PENDING;
}
