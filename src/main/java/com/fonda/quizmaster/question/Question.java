package com.fonda.quizmaster.question;

import com.fonda.quizmaster.quiz.Quiz;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Setter
    @Column(nullable = false, length = 1000)
    private String prompt;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuestionType type;

    @Setter
    @Column(nullable = false)
    private int points;

    @Setter
    @Column(nullable = false)
    private int position;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<QuestionOption> options = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Question(Quiz quiz, String prompt, QuestionType type, int points, int position) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt cannot be empty");
        }
        if (points < 0) {
            throw new IllegalArgumentException("Points cannot be negative");
        }
        this.quiz = Objects.requireNonNull(quiz, "Quiz cannot be null");
        this.prompt = prompt;
        this.type = Objects.requireNonNullElse(type, QuestionType.MULTIPLE_CHOICE);
        this.points = points;
        this.position = position;
    }

    public void addOption(QuestionOption option) {
        this.options.add(option);
        option.setQuestion(this);
    }

    public void removeOption(QuestionOption option) {
        this.options.remove(option);
        option.setQuestion(null);
    }

    public void clearOptions() {
        for (QuestionOption option : this.options) {
            option.setQuestion(null);
        }
        this.options.clear();
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
        Question other = (Question) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Question{id=" + id + ", prompt='" + prompt + "', type=" + type + ", points=" + points + ", position=" + position + "}";
    }
}
