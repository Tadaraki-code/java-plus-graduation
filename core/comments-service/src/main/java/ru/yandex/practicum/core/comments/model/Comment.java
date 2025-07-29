package ru.yandex.practicum.core.comments.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.core.interaction.comments.enums.CommentStatus;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "comments")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "text", nullable = false)
    @Size(min = 5, max = 255)
    String text;

    @Column(name = "created_on", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdOn;

    @ToString.Exclude
    @Column(name = "author_id")
    Long authorId;

    @Column(name = "author_name", nullable = false)
    String authorName;

    @ToString.Exclude
    @Column(name = "event_id")
    Long eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    CommentStatus status = CommentStatus.PENDING;
}
