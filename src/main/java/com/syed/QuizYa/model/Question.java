package com.syed.QuizYa.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private QuestionBank bank;

    @Column(name = "question_type", length = 20, nullable = false)
    private String questionType;

    @Column(name = "prompt_text", columnDefinition = "TEXT", nullable = false)
    private String promptText;

    // Optional media asset — attached via Cloudinary upload (Week 3)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_asset_id")
    private MediaAsset mediaAsset;


    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "time_limit_seconds")
    private Integer timeLimitSeconds;

    @Column(name = "explanation_text", columnDefinition = "TEXT")
    private String explanationText;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public QuestionBank getBank() { return bank; }
    public void setBank(QuestionBank bank) { this.bank = bank; }
    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }
    public String getPromptText() { return promptText; }
    public void setPromptText(String promptText) { this.promptText = promptText; }
    public MediaAsset getMediaAsset() { return mediaAsset; }
    public void setMediaAsset(MediaAsset mediaAsset) { this.mediaAsset = mediaAsset; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getTimeLimitSeconds() { return timeLimitSeconds; }
    public void setTimeLimitSeconds(Integer timeLimitSeconds) { this.timeLimitSeconds = timeLimitSeconds; }
    public String getExplanationText() { return explanationText; }
    public void setExplanationText(String explanationText) { this.explanationText = explanationText; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
