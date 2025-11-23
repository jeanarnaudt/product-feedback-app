package com.feedback.repository;

import com.feedback.domain.Reply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

    @Query("select count(r) from Reply r where r.comment.id = ?1")
    long countByCommentId(Long commentId);

    @Modifying
    @Transactional
    @Query("delete from Reply r where r.comment.id = ?1")
    void deleteByCommentId(Long commentId);
}
