package com.fonda.quizmaster.quiz;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findByCreatorId(Long creatorId);

    Page<Quiz> findByCreatorId(Long creatorId, Pageable pageable);

    Page<Quiz> findByStatus(QuizStatus status, Pageable pageable);

    Optional<Quiz> findByIdAndCreatorId(Long id, Long creatorId);

    boolean existsByIdAndCreatorId(Long id, Long creatorId);
}
