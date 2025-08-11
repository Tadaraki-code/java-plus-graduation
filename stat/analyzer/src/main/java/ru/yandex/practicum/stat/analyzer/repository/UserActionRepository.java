package ru.yandex.practicum.stat.analyzer.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.stat.analyzer.model.UserAction;

import java.util.List;
import java.util.Set;

@Repository
public interface UserActionRepository extends JpaRepository<UserAction, UserAction.UserActionId> {
    List<UserAction> findAllByEventIdIn(List<Long> eventIds);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    @Query("SELECT ua.eventId FROM UserAction ua WHERE ua.userId = :userId")
    Set<Long> findEventIdsByUserId(@Param("userId") Long userId);

    List<UserAction> findAllByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);
}
