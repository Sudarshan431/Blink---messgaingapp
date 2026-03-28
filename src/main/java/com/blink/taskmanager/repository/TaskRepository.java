package com.blink.taskmanager.repository;

import com.blink.taskmanager.model.Task;
import com.blink.taskmanager.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("""
            SELECT t FROM Task t
            WHERE (:status IS NULL OR t.status = :status)
              AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId)
              AND (:keyword IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Task> search(@Param("status") TaskStatus status,
                      @Param("assigneeId") Long assigneeId,
                      @Param("keyword") String keyword,
                      Pageable pageable);
}
