package com.fonda.quizmaster.quiz;

import com.fonda.quizmaster.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "quizzes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false, length = 150)
    private String title;

    @Setter
    @Column(length = 1000)
    private String description;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuizStatus status;

    @Setter
    @Column(name = "time_limit_seconds")
    private Integer timeLimitSeconds;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Quiz(String title, String description, User creator, Integer timeLimitSeconds) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        this.title = title;
        this.description = description;
        this.creator = Objects.requireNonNull(creator, "Creator cannot be null");
        this.status = QuizStatus.DRAFT;
        this.timeLimitSeconds = timeLimitSeconds;
    }

    public void publish() {
        this.status = QuizStatus.PUBLISHED;
    }

    public void archive() {
        this.status = QuizStatus.ARCHIVED;
    }

    public void draft() {
        this.status = QuizStatus.DRAFT;
    }

    @PrePersist
    protected void onCreate() {
        var now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quiz other = (Quiz) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Quiz{id=" + id + ", title='" + title + "', status=" + status + ", timeLimitSeconds=" + timeLimitSeconds + "}";
    }
}
