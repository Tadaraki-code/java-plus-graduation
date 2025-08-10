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
@Table(name = "user_action")
@FieldDefaults(level = AccessLevel.PRIVATE)
@IdClass(UserAction.UserActionId.class)
public class UserAction {

    @Id
    @Column(name = "user_id", nullable = false)
    Long userId;
    @Id
    @Column(name = "event_id", nullable = false)
    Long eventId;
    @Column(name = "weight", nullable = false)
    Double weight;
    @Column(name = "timestamp", nullable = false)
    LocalDateTime timestamp;

    public static class UserActionId implements Serializable {
        Long userId;
        Long eventId;

        public UserActionId() {}
        public UserActionId(Long userId, Long eventId) {
            this.userId = userId;
            this.eventId = eventId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserActionId that = (UserActionId) o;
            return userId.equals(that.userId) && eventId.equals(that.eventId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, eventId);
        }

    }
}
