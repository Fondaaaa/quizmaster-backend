package com.fonda.quizmaster.question;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "question_options")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Setter
    @Column(name = "option_text", nullable = false, length = 500)
    private String text;

    @Setter
    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Setter
    @Column(nullable = false)
    private int position;

    public QuestionOption(String text, boolean correct, int position) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Option text cannot be empty");
        }
        this.text = text;
        this.correct = correct;
        this.position = position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestionOption other = (QuestionOption) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "QuestionOption{id=" + id + ", text='" + text + "', correct=" + correct + ", position=" + position + "}";
    }
}
