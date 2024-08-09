package com.example.omg_project.domain.joinpost.repository;

import com.example.omg_project.domain.joinpost.entity.JoinPostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JoinPostCommentRepository extends JpaRepository<JoinPostComment, Long> {
    // 특정 게시글에 대한 모든 댓글 조회
    List<JoinPostComment> findAllByJoinPostId(Long joinPostId);
}