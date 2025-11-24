package com.feedback.repository;

import com.feedback.domain.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // Use native query to compare against enum column stored as string in DB
    @Query(value = "select * from feedback where status = ?1", nativeQuery = true)
    List<Feedback> findByStatus(String status);

    // Methods for filtering and sorting can be added later as needed

    List<Feedback> findByAuthorId(Long authorId);
}
