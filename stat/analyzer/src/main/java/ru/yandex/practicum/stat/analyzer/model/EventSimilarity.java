package ru.yandex.practicum.stat.analyzer.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;


@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "event_similarity")
@FieldDefaults(level = AccessLevel.PRIVATE)
@IdClass(EventSimilarity.EventSimilarityId.class)
public class EventSimilarity {

    @Id
    @Column(name = "eventA_id", nullable = false)
    Long eventAId;
    @Id
    @Column(name = "eventB_id", nullable = false)
    Long eventBId;
    @Column(name = "score", nullable = false)
    Double score;
    @Column(name = "timestamp", nullable = false)
    LocalDateTime timestamp;

    public static class EventSimilarityId implements Serializable {
        Long eventAId;
        Long eventBId;

        public EventSimilarityId() {}
        public EventSimilarityId(Long eventAId, Long eventBId) {
            this.eventAId = eventAId;
            this.eventBId = eventBId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EventSimilarityId that = (EventSimilarityId) o;
            return eventAId.equals(that.eventAId) && eventBId.equals(that.eventBId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(eventAId, eventBId);
        }

    }
}
