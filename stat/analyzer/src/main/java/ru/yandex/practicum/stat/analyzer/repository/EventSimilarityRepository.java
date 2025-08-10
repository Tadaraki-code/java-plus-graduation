package ru.yandex.practicum.stat.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.stat.analyzer.model.EventSimilarity;

import java.util.List;
import java.util.Set;

@Repository
public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, EventSimilarity.EventSimilarityId> {
    @Query("SELECT e FROM EventSimilarity e WHERE e.eventAId = :eventId OR e.eventBId = :eventId")
    List<EventSimilarity> findByEventAIdOrEventBId(@Param("eventId") Long eventId);

    @Query("SELECT e FROM EventSimilarity e WHERE e.eventAId IN :eventIds OR e.eventBId IN :eventIds")
    List<EventSimilarity> findByEventAIdOrEventBIdIn(@Param("eventIds") Set<Long> eventIds);
}
