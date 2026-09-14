package com.fonda.quizmaster.question;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByQuizIdOrderByPositionAsc(Long quizId);

    Page<Question> findByQuizId(Long quizId, Pageable pageable);

    long countByQuizId(Long quizId);

    @Query("SELECT DISTINCT q FROM Question q LEFT JOIN FETCH q.options WHERE q.quiz.id = :quizId ORDER BY q.position ASC")
    List<Question> findAllWithOptionsByQuizId(@Param("quizId") Long quizId);

    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.options WHERE q.id = :id")
    Optional<Question> findByIdWithOptions(@Param("id") Long id);
}
