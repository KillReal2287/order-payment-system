package dev.bronnikov.domain;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {

    @Query(value = """
            SELECT * FROM tasks
            WHERE status = :newStatus
                    OR (status = :retryableStatus AND next_attempt_at <= :nowDate)
                        for update skip locked
            """, nativeQuery = true)
    List<TaskEntity> findByStatuses(@Param(value = "newStatus") String newStatus,
                                    @Param(value = "retryableStatus") String retryableStatus,
                                    @Param(value = "nowDate") OffsetDateTime now);
}
